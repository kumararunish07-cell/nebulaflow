package io.nebulaflow.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiKeyCatalogTest {
  @Test void parsesRolesAndTenants() {
    var catalog = new ApiKeyCatalog("secret=alice|ROLE_ADMIN|TENANTS:acme,lab");
    var identity = catalog.find("secret");
    assertNotNull(identity);
    assertEquals("alice", identity.principal());
    assertTrue(identity.authorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    assertTrue(identity.allowsTenant("acme"));
    assertFalse(identity.allowsTenant("other"));
  }
}
