package org.icatproject.authn_db.TestProfiles;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class MechanismTestProfile implements QuarkusTestProfile {
	@Override
	public Map<String, String> getConfigOverrides() {
		return Map.of(
				"mechanism", "someValue" // Override config property and add ips
		);
	}
}
