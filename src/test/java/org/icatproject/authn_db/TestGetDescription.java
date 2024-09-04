package org.icatproject.authn_db;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class TestGetDescription {

	@Inject
	DB_Authenticator authn;

	@Test
	public void getDescription() {
		assertEquals("{\"keys\":[{\"name\":\"username\"},{\"name\":\"password\",\"hide\":true}]}",
				authn.getDescription());
	}

}