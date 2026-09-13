package io.nebulaflow.security;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;

@Configuration
public class OidcJwtConfiguration {
  @Bean(name = "oidcJwtDecoder")
  @ConditionalOnProperty(prefix = "nebulaflow.security", name = "mode", havingValue = "OIDC")
  JwtDecoder oidcJwtDecoder(SecurityProperties properties) {
    return buildDecoder(properties);
  }

  @Bean(name = "hybridJwtDecoder")
  @ConditionalOnProperty(prefix = "nebulaflow.security", name = "mode", havingValue = "HYBRID")
  JwtDecoder hybridJwtDecoder(SecurityProperties properties) {
    return buildDecoder(properties);
  }

  private static NimbusJwtDecoder buildDecoder(SecurityProperties properties) {
    SecurityProperties.Oidc oidc = properties.getOidc();
    String issuer = value(oidc.getIssuerUri());
    String jwkSet = value(oidc.getJwkSetUri());
    if (issuer.isBlank() && jwkSet.isBlank()) {
      throw new IllegalStateException("OIDC mode requires nebulaflow.security.oidc.issuer-uri or jwk-set-uri");
    }

    NimbusJwtDecoder decoder = issuer.isBlank()
        ? NimbusJwtDecoder.withJwkSetUri(jwkSet).build()
        : NimbusJwtDecoder.withIssuerLocation(issuer).build();
    List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
    validators.add(issuer.isBlank() ? JwtValidators.createDefault() : JwtValidators.createDefaultWithIssuer(issuer));
    String audience = value(oidc.getAudience());
    if (!audience.isBlank()) {
      OAuth2Error error = new OAuth2Error("invalid_token", "JWT audience is not allowed", null);
      validators.add(jwt -> jwt.getAudience().contains(audience)
          ? OAuth2TokenValidatorResult.success() : OAuth2TokenValidatorResult.failure(error));
    }
    decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(validators));
    return decoder;
  }

  private static String value(String value) { return value == null ? "" : value.trim(); }
}
