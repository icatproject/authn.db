package org.icatproject.authn_db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.icatproject.authentication.AuthnException;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class TestAuthenticate {

	@Inject
	DB_Authenticator authn;

	@Inject
	@ConfigProperty(name = "mechanism", defaultValue = "db")
	String mechanism;

	@Test
	public void testValidLoginUser() throws AuthnException {
		String jsonString = "{\"credentials\":[{\"username\":\"user1\"},{\"password\":\"sunshine\"}]}";
		String result = authn.authenticate(jsonString);
		assertEquals(Response.Status.OK.getStatusCode(), 200);
		// the test should return whatever mechanism is in the config
		String expectedResponse = String.format("{\"username\":\"user1\",\"mechanism\":\"%s\"}", mechanism);
		assertEquals(expectedResponse, result);
	}

	@Test
	public void testInvalidUsername() {
		String jsonString = "{\"credentials\":[{\"username\":\"invaliduser\"},{\"password\":\"sunshine\"}]}";
		AuthnException exception = assertThrows(AuthnException.class, () -> authn.authenticate(jsonString));
		assertEquals(Response.Status.FORBIDDEN.getStatusCode(), exception.getHttpStatusCode());
		assertEquals("(403) : The username and password do not match", exception.getMessage());
	}

	@Test
	public void testInvalidPassword() {
		String jsonString = "{\"credentials\":[{\"username\":\"user1\"},{\"password\":\"trainspotting\"}]}";
		AuthnException exception = assertThrows(AuthnException.class, () -> authn.authenticate(jsonString));
		assertEquals(Response.Status.FORBIDDEN.getStatusCode(), exception.getHttpStatusCode());
		assertEquals("(403) : The username and password do not match", exception.getMessage());
	}

}

