package org.icatproject.authn_db;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusIntegrationTest
public class AuthenticateIT {

	@Test
	public void testValidLoginUser() {
		// JSON string to be sent as form data
		String jsonString = "{\"credentials\":[{\"username\":\"user1\"},{\"password\":\"sunshine\"}]}";

		given()
				.header("Content-Type", "application/x-www-form-urlencoded")
				.formParam("json", jsonString)
				.when()
				.post("/authn.db/authenticate")
				.then()
				.statusCode(Response.Status.OK.getStatusCode())
				.body("username", equalTo("user1"))
				.body("mechanism", equalTo("db"));
	}
	@Test
	public void testInvalidUsername() {
		String jsonString = "{\"credentials\":[{\"username\":\"invaliduser\"},{\"password\":\"sunshine\"}]}";

		// Perform an HTTP POST with invalid username, sending the JSON as a form parameter
		given()
				.header("Content-Type", "application/x-www-form-urlencoded")  // Set Content-Type for form-urlencoded
				.formParam("json", jsonString)  // Send the JSON string as a form parameter with the key 'json'
				.when()
				.post("/authn.db/authenticate")
				.then()
				.statusCode(Response.Status.FORBIDDEN.getStatusCode())
				.body("message", equalTo("The username and password do not match"));
	}
	@Test
	public void testInvalidPassword() {
		String jsonString = "{\"credentials\":[{\"username\":\"user1\"},{\"password\":\"trainspotting\"}]}";

		// Perform an HTTP POST with invalid password, sending the JSON as a form parameter
		given()
				.header("Content-Type", "application/x-www-form-urlencoded")
				.formParam("json", jsonString)
				.when()
				.post("/authn.db/authenticate")
				.then()
				.statusCode(Response.Status.FORBIDDEN.getStatusCode())
				.body("message", equalTo("The username and password do not match"));
	}
}
