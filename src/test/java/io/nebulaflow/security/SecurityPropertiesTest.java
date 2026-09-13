package io.nebulaflow.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityPropertiesTest {
  @Test void supportsApiKeyOidcAndHybridModes() {
    var properties = new SecurityProperties();
    assertTrue(properties.isApiKeyEnabled());
    assertTrue(!properties.isJwtEnabled());

    properties.setMode("oidc");
    assertEquals(SecurityProperties.SecurityMode.OIDC, properties.securityMode());
    assertTrue(!properties.isApiKeyEnabled());
    assertTrue(properties.isJwtEnabled());

    properties.setMode("HYBRID");
    assertTrue(properties.isApiKeyEnabled());
    assertTrue(properties.isJwtEnabled());
  }

  @Test void rejectsUnknownMode() {
    var properties = new SecurityProperties();
    properties.setMode("unknown");
    assertThrows(IllegalArgumentException.class, properties::securityMode);
  }
}
