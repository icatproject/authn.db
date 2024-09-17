package org.icatproject.authn_db;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.ws.rs.core.Response;
import org.icatproject.authn_db.TestProfiles.MechanismTestProfile;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusIntegrationTest
@TestProfile(MechanismTestProfile.class)
public class MechanismIT {

	@Test
	public void testMechanism() {
		// Standard valid login with no issues
		String jsonString = "{\"credentials\":[{\"username\":\"user1\"},{\"password\":\"sunshine\"}]}";

		given()
				.header("Content-Type", "application/x-www-form-urlencoded")
				.formParam("json", jsonString)
				.when()
				.post("/authn.db/authenticate")
				.then()
				.statusCode(Response.Status.OK.getStatusCode())
				.body("username", equalTo("user1"))
				.body("mechanism", equalTo("someValue"));
	}
}