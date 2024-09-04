package org.icatproject.authn_db;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonGenerator;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.icatproject.authentication.AuthnException;
import org.icatproject.authentication.PasswordChecker;
import org.icatproject.utils.AddressChecker;
import org.icatproject.utils.AddressCheckerException;
import org.jboss.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.util.Optional;

@Path("/authn.db")
@ApplicationScoped
public class DB_Authenticator {

	@Inject
	EntityManager manager;

	AddressChecker addressChecker;

	@Inject
	@ConfigProperty(name = "quarkus.application.version")
	String projectVersion;

	@Inject
	@ConfigProperty(name = "mechanism", defaultValue = "db")
	String mechanism;

	@Inject
	@ConfigProperty(name = "ip")
	Optional<String> ipAddresses;

	private static final Logger logger = Logger.getLogger(DB_Authenticator.class);

	@PostConstruct
	void init() {
		ipAddresses.ifPresentOrElse(ip -> {
			try {
				logger.info("Initialising AddressChecker with IP: " + ip);
				// If ipAddresses is present, create an AddressChecker
				addressChecker = new AddressChecker(ip);
			} catch (Exception e) {
				logger.error("Problem creating AddressChecker with IP: " + ip, e);
				throw new IllegalStateException("Invalid IP configuration", e);
			}
		}, () -> logger.info("No IP configured, AddressChecker will not be initialized."));

		logger.info("Initialised DB_Authenticator");
	}

	@GET
	@Path("version")
	@Produces(MediaType.APPLICATION_JSON)
	public String getVersion() {
		JsonObject versionJson = Json.createObjectBuilder()
				.add("version", projectVersion)
				.build();
		return versionJson.toString();
	}

	@POST
	@Path("authenticate")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public String authenticate(@FormParam("json") String jsonString) throws AuthnException {

		ByteArrayInputStream s = new ByteArrayInputStream(jsonString.getBytes());

		String username = null;
		String password = null;
		String ip = null;
		try (JsonReader r = Json.createReader(s)) {
			JsonObject o = r.readObject();
			for (JsonValue c : o.getJsonArray("credentials")) {
				JsonObject credential = (JsonObject) c;
				if (credential.containsKey("username")) {
					username = credential.getString("username");
				} else if (credential.containsKey("password")) {
					password = credential.getString("password");
				}
			}
			if (o.containsKey("ip")) {
				ip = o.getString("ip");
			}

		}

		logger.debug("Login request by: " + username);

		if (username == null || username.isEmpty()) {
			throw new AuthnException(HttpURLConnection.HTTP_FORBIDDEN, "username cannot be null or empty.");
		}

		if (password == null || password.isEmpty()) {
			throw new AuthnException(HttpURLConnection.HTTP_FORBIDDEN, "password cannot be null or empty.");
		}

		if (addressChecker != null) {
			try {
				if (ip==null) {
					throw new AuthnException(HttpURLConnection.HTTP_BAD_REQUEST,
							"An Ip address must be provided");
				}
				if (!addressChecker.check(ip)) {
					throw new AuthnException(HttpURLConnection.HTTP_FORBIDDEN,
							"authn_db does not allow log in from your IP address " + ip);
				}
			} catch (AddressCheckerException e) {
				throw new AuthnException(HttpURLConnection.HTTP_INTERNAL_ERROR, e.getClass() + " " + e.getMessage());
			}
		}

		logger.debug("Checking password against database");

		Passwd passwd = this.manager.find(Passwd.class, username);
		if (passwd == null) {
			throw new AuthnException(HttpURLConnection.HTTP_FORBIDDEN, "The username and password do not match");
		}

		if (!PasswordChecker.verify(password, passwd.getEncodedPassword())) {
			throw new AuthnException(HttpURLConnection.HTTP_FORBIDDEN, "The username and password do not match");
		}

		logger.info(username + " logged in succesfully" + (mechanism != null ? " by " + mechanism : ""));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartObject().write("username", username);
			if (mechanism != null) {
				gen.write("mechanism", mechanism);
			}
			gen.writeEnd();
		}
		return baos.toString();
	}

	@GET
	@Path("description")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public String getDescription() {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartObject().writeStartArray("keys");
			gen.writeStartObject().write("name", "username").writeEnd();
			gen.writeStartObject().write("name", "password").write("hide", true).writeEnd();
			gen.writeEnd().writeEnd();
		}
		return baos.toString();
	}

}
