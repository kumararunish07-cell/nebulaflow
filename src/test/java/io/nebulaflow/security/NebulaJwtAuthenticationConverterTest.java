package io.nebulaflow.security;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NebulaJwtAuthenticationConverterTest {
  @Test void mapsRolesScopesAndPrincipal() {
    var properties = new SecurityProperties();
    properties.getOidc().setRolesClaim("groups");
    properties.getOidc().setPrincipalClaim("email");
    var converter = new NebulaJwtAuthenticationConverter(properties);
    Jwt jwt = Jwt.withTokenValue("token")
        .header("alg", "none")
        .subject("subject-1")
        .claim("email", "alice@example.com")
        .claim("groups", List.of("ADMIN", "ROLE_OPERATOR"))
        .claim("scope", "workflow:read workflow:run")
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(300))
        .build();

    var authentication = (JwtAuthenticationToken) converter.convert(jwt);
    assertEquals("alice@example.com", authentication.getName());
    assertTrue(authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    assertTrue(authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_OPERATOR")));
    assertTrue(authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("SCOPE_WORKFLOW:READ")));
  }
}
