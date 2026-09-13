package io.nebulaflow.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
  private final ApiKeyCatalog catalog;
  private final SecurityProperties properties;

  public ApiKeyAuthenticationFilter(ApiKeyCatalog catalog, SecurityProperties properties) {
    this.catalog = catalog;
    this.properties = properties;
  }

  @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    if (!properties.isApiKeyEnabled()) { chain.doFilter(request, response); return; }
    String apiKey = request.getHeader("X-API-Key");
    if (apiKey == null || apiKey.isBlank()) { chain.doFilter(request, response); return; }
    ApiKeyCatalog.Identity identity = catalog.find(apiKey.trim());
    if (identity == null) { writeError(response, 401, "INVALID_API_KEY", "invalid API key"); return; }
    String tenant = request.getHeader("X-Tenant-Id");
    if (!identity.allowsTenant(tenant == null ? "" : tenant.trim())) {
      writeError(response, 403, "TENANT_FORBIDDEN", "API key is not authorized for this tenant"); return;
    }
    var authentication = new UsernamePasswordAuthenticationToken(identity.principal(), null, identity.authorities());
    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authentication);
    chain.doFilter(request, response);
  }

  private static void writeError(HttpServletResponse response, int status, String code, String message) throws IOException {
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().printf("{\"code\":\"%s\",\"message\":\"%s\"}", code, message);
  }
}
