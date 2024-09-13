package org.icatproject.authn_db;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusIntegrationTest
public class DescriptionIT {

	@Test
	public void getDescription() {
		given()
				.when().get("/authn.db/description")
				.then()
				.statusCode(200)
				.body(equalTo("{\"keys\":[{\"name\":\"username\"},{\"name\":\"password\",\"hide\":true}]}"));
	}
}