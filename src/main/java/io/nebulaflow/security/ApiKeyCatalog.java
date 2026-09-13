package io.nebulaflow.security;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyCatalog {
  public record Identity(String principal, Set<SimpleGrantedAuthority> authorities, Set<String> tenants) {
    boolean allowsTenant(String tenant) { return tenants.contains("*") || tenants.contains(tenant); }
  }

  private final Map<String, Identity> identities;

  public ApiKeyCatalog(@Value("${nebulaflow.security.api-keys:}") String configuredKeys) {
    Map<String, Identity> parsed = new LinkedHashMap<>();
    for (String entry : configuredKeys.split(";")) {
      if (entry.isBlank() || !entry.contains("=")) continue;
      String[] assignment = entry.split("=", 2);
      String key = assignment[0].trim();
      String[] tokens = assignment[1].split("\\|");
      if (key.isBlank() || tokens.length == 0 || tokens[0].isBlank()) continue;
      Set<SimpleGrantedAuthority> authorities = Arrays.stream(tokens).skip(1)
          .filter(token -> token.startsWith("ROLE_")).map(String::trim).map(String::toUpperCase)
          .map(SimpleGrantedAuthority::new).collect(Collectors.toUnmodifiableSet());
      Set<String> tenants = Arrays.stream(tokens).skip(1).filter(token -> token.startsWith("TENANTS:"))
          .flatMap(token -> Arrays.stream(token.substring("TENANTS:".length()).split(",")))
          .map(String::trim).filter(value -> !value.isBlank()).collect(Collectors.toUnmodifiableSet());
      parsed.put(key, new Identity(tokens[0].trim(), authorities, tenants));
    }
    identities = Collections.unmodifiableMap(parsed);
  }

  public Identity find(String apiKey) { return apiKey == null ? null : identities.get(apiKey); }
}
