package io.nebulaflow.tenant;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class TenantFilter implements Filter {
  private static final Set<String> PUBLIC_PATHS = Set.of("/actuator/health", "/swagger-ui.html");

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) req;
    if (isPublic(request)) { chain.doFilter(req, res); return; }
    String tenant = request.getHeader("X-Tenant-Id");
    if (tenant == null || tenant.isBlank()) {
      ((HttpServletResponse) res).sendError(400, "X-Tenant-Id header is required");
      return;
    }
    TenantContext.set(tenant.trim());
    try { chain.doFilter(req, res); }
    finally { TenantContext.clear(); }
  }

  private static boolean isPublic(HttpServletRequest request) {
    String path = request.getRequestURI();
    return "OPTIONS".equalsIgnoreCase(request.getMethod())
        || PUBLIC_PATHS.contains(path)
        || path.startsWith("/swagger-ui/")
        || path.startsWith("/v3/api-docs/")
        || "/v3/api-docs".equals(path);
  }
}
