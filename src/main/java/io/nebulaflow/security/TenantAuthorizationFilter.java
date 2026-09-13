package io.nebulaflow.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

public class TenantAuthorizationFilter extends OncePerRequestFilter {
  private final SecurityProperties properties;

  public TenantAuthorizationFilter(SecurityProperties properties) { this.properties = properties; }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof JwtAuthenticationToken jwtAuthentication
        && !allowsTenant(jwtAuthentication, request.getHeader("X-Tenant-Id"))) {
      writeError(response, 403, "TENANT_FORBIDDEN", "JWT is not authorized for this tenant");
      return;
    }
    chain.doFilter(request, response);
  }

  private boolean allowsTenant(JwtAuthenticationToken authentication, String tenantHeader) {
    if (tenantHeader == null || tenantHeader.isBlank()) return false;
    Object claim = authentication.getToken().getClaim(properties.getOidc().getTenantClaim());
    Set<String> tenants = new LinkedHashSet<>();
    if (claim instanceof Collection<?> values) values.forEach(value -> addTenant(tenants, value));
    else if (claim instanceof String value) for (String item : value.split("[ ,]+")) addTenant(tenants, item);
    return tenants.contains("*") || tenants.contains(tenantHeader.trim());
  }

  private static void addTenant(Set<String> tenants, Object value) {
    if (value != null && !String.valueOf(value).isBlank()) tenants.add(String.valueOf(value).trim());
  }

  private static void writeError(HttpServletResponse response, int status, String code, String message) throws IOException {
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().printf("{\"code\":\"%s\",\"message\":\"%s\"}", code, message);
  }
}
