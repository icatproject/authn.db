package org.icatproject.authn_db;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.icatproject.authentication.AuthnException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
@TestProfile(IPTestProfile.class)
public class IPTests {

	@Inject
	DB_Authenticator authn;

	@Inject
	@ConfigProperty(name = "mechanism", defaultValue = "db")
	String mechanism;

	@Test
	public void testNoIpInRequest() {
		String jsonString = "{\"credentials\":[{\"username\":\"user1\"},{\"password\":\"sunshine\"}]}";
		AuthnException exception = assertThrows(AuthnException.class, () -> authn.authenticate(jsonString));
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), exception.getHttpStatusCode());
		assertEquals("(400) : An Ip address must be provided", exception.getMessage());
	}

	@Test
	public void badIpInRequest() {
		String jsonString = "{\"credentials\":[{\"username\":\"user1\"},{\"password\":\"sunshine\"}], \"ip\":\"192.167.0.125\"}";
		AuthnException exception = assertThrows(AuthnException.class, () -> authn.authenticate(jsonString));
		assertEquals(Response.Status.FORBIDDEN.getStatusCode(), exception.getHttpStatusCode());
		assertEquals("(403) : authn_db does not allow log in from your IP address 192.167.0.125", exception.getMessage());
	}

	@Test
	public void goodIpInRequest() throws AuthnException {
		String jsonString = "{\"credentials\":[{\"username\":\"user1\"},{\"password\":\"sunshine\"}], \"ip\":\"192.168.0.125\"}";
		String result = authn.authenticate(jsonString);
		assertEquals(Response.Status.OK.getStatusCode(), 200);
		String expectedResponse = String.format("{\"username\":\"user1\",\"mechanism\":\"%s\"}", mechanism);
		assertEquals(expectedResponse, result);
	}
}
