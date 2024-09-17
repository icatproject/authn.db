package org.icatproject.authn_db;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import org.icatproject.authentication.AuthnException;
import org.jboss.logging.Logger;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;

/**
 * The {@code Request} class is responsible for extracting credentials from a
 * JSON string, including a token and an optional IP address.
 * <p>
 * This class provides methods to retrieve the extracted token and IP address,
 * and checks that the token is not null or empty.
 * </p>
 */
public class Request {

    private static final Logger logger = Logger.getLogger(Request.class);

    private String username;
    private String password;
    private String ips;

    public Request() {
        this.username = null;
        this.password = null;
        this.ips = null;
    }

    public String getUsername() {
        return this.username;
    }
    public String getPassword() {
        return this.password;
    }
    public String getIps() {
        return this.ips;
    }

    /** Method that pulls credentials out of json string */
    public void getCredentials(String jsonString) throws AuthnException {
        logger.info("Unpacking request to extract credentials");
        ByteArrayInputStream stream = new ByteArrayInputStream(jsonString.getBytes());
        try (JsonReader r = Json.createReader(stream)) {
            JsonObject o = r.readObject();
            for (JsonValue c : o.getJsonArray("credentials")) {
                JsonObject credential = (JsonObject) c;
                if (credential.containsKey("username")) {
                    this.username = credential.getString("username");
                } else if (credential.containsKey("password")) {
                    this.password = credential.getString("password");
                }
            }
            if (o.containsKey("ip")) {
                this.ips = o.getString("ip");
            }

        }
        if (this.username == null || this.username.isEmpty()) {
            throw new AuthnException(HttpURLConnection.HTTP_FORBIDDEN, "username cannot be null or empty.");
        }
        if (this.password == null || this.password.isEmpty()) {
            throw new AuthnException(HttpURLConnection.HTTP_FORBIDDEN, "password cannot be null or empty.");
        }
    }
}
