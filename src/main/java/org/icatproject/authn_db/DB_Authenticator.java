package org.icatproject.authn_db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.stream.JsonGenerator;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.icatproject.authentication.AuthnException;
import org.jboss.logging.Logger;

import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;

@Path("/authn.db")
@ApplicationScoped
public class DB_Authenticator {

	private static final Logger logger = Logger.getLogger(DB_Authenticator.class);

	@Inject
	EntityManager manager;

	@Inject
	@ConfigProperty(name = "quarkus.application.version")
	String projectVersion;

	@Inject
	@ConfigProperty(name = "mechanism", defaultValue = "db")
	String mechanism;

	@Inject
	IPVerifier ipVerifier;

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

		// Extract the token and IP from the JSON input and store them in the class
		Request request = new Request();
		request.getCredentials(jsonString);

		// Perform IP address checking if required
		ipVerifier.CheckIPs(request.getIps());

		logger.debug("Login request by: " + request.getUsername());
		logger.debug("Checking password against database");

		Passwd passwd = this.manager.find(Passwd.class, request.getUsername());
		if (passwd == null) {
			throw new AuthnException(HttpURLConnection.HTTP_FORBIDDEN, "The username and password do not match");
		} else {
			passwd.checkPasswords(request.getPassword(), passwd.getPassword());
		}

		logger.info(request.getUsername() + " logged in succesfully" + (mechanism != null ? " by " + mechanism : ""));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (JsonGenerator gen = Json.createGenerator(baos)) {
			gen.writeStartObject().write("username", request.getUsername());
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
