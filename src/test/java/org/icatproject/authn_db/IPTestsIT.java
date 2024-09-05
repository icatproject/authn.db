package org.icatproject.authn_db;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusIntegrationTest
@TestProfile(IPTestProfile.class)
public class IPTestsIT {

	@Test
	public void testNoIpInRequest() {
		String jsonString = "{\"credentials\":[{\"username\":\"user1\"},{\"password\":\"sunshine\"}]}";

		// Perform an HTTP POST request without IP in the request body
		given()
				.header("Content-Type", "application/x-www-form-urlencoded")  // Set Content-Type for form-urlencoded
				.formParam("json", jsonString)  // Send the JSON string as a form parameter with the key 'json'
				.when()
				.post("/authn.db/authenticate")
				.then()
				.statusCode(Response.Status.BAD_REQUEST.getStatusCode())  // Expect 400 Bad Request
				.body("message", equalTo("An Ip address must be provided"));
	}

	@Test
	public void badIpInRequest() {
		String jsonString = "{\"credentials\":[{\"username\":\"user1\"},{\"password\":\"sunshine\"}], \"ip\":\"192.167.0.125\"}";

		// Perform an HTTP POST request with a bad IP address
		given()
				.header("Content-Type", "application/x-www-form-urlencoded")
				.formParam("json", jsonString)
				.when()
				.post("/authn.db/authenticate")
				.then()
				.statusCode(Response.Status.FORBIDDEN.getStatusCode())
				.body("message", equalTo("authn_db does not allow log in from your IP address 192.167.0.125"));
	}

	@Test
	public void goodIpInRequest() {
		String jsonString = "{\"credentials\":[{\"username\":\"user1\"},{\"password\":\"sunshine\"}], \"ip\":\"192.168.0.125\"}";

		// Perform an HTTP POST request with a valid IP address
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

}
