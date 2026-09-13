package io.nebulaflow.security;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class NebulaJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
  private final SecurityProperties properties;

  public NebulaJwtAuthenticationConverter(SecurityProperties properties) {
    this.properties = properties;
  }

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    Set<GrantedAuthority> authorities = new LinkedHashSet<>();
    addAuthorities(authorities, jwt.getClaim(properties.getOidc().getRolesClaim()), "ROLE_");
    addAuthorities(authorities, jwt.getClaim("scope"), "SCOPE_");
    addAuthorities(authorities, jwt.getClaim("scp"), "SCOPE_");
    String principal = jwt.getClaimAsString(properties.getOidc().getPrincipalClaim());
    if (principal == null || principal.isBlank()) principal = jwt.getSubject();
    return new JwtAuthenticationToken(jwt, authorities, principal);
  }

  private static void addAuthorities(Set<GrantedAuthority> authorities, Object claim, String prefix) {
    if (claim instanceof Collection<?> values) {
      values.forEach(value -> addAuthority(authorities, value, prefix));
    } else if (claim instanceof String text) {
      for (String value : text.split("[ ,]+")) addAuthority(authorities, value, prefix);
    }
  }

  private static void addAuthority(Set<GrantedAuthority> authorities, Object value, String prefix) {
    if (value == null || String.valueOf(value).isBlank()) return;
    String authority = String.valueOf(value).trim().toUpperCase(Locale.ROOT);
    if (!authority.startsWith(prefix)) authority = prefix + authority;
    authorities.add(new SimpleGrantedAuthority(authority));
  }
}
