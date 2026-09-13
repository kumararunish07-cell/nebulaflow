package io.nebulaflow.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
  @Bean ApiKeyAuthenticationFilter apiKeyAuthenticationFilter(ApiKeyCatalog catalog) {
    return new ApiKeyAuthenticationFilter(catalog);
  }

  @Bean SecurityFilterChain securityFilterChain(HttpSecurity http, ApiKeyAuthenticationFilter apiKeyFilter) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(errors -> errors
            .authenticationEntryPoint((request, response, exception) -> writeError(response, 401, "UNAUTHENTICATED", "X-API-Key is required"))
            .accessDeniedHandler((request, response, exception) -> writeError(response, 403, "FORBIDDEN", "insufficient role")))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/health", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/workflows", "/api/v1/runs/**").hasAnyRole("ADMIN", "OPERATOR", "VIEWER")
            .requestMatchers(HttpMethod.POST, "/api/v1/workflows").hasRole("ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/v1/workflows/*/runs").hasAnyRole("ADMIN", "OPERATOR")
            .anyRequest().authenticated())
        .addFilterBefore(apiKeyFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  private static void writeError(jakarta.servlet.http.HttpServletResponse response, int status, String code, String message)
      throws java.io.IOException {
    response.setStatus(status);
    response.setContentType("application/json");
    response.getWriter().printf("{\"code\":\"%s\",\"message\":\"%s\"}", code, message);
  }
}
