package io.nebulaflow.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nebulaflow.security")
public class SecurityProperties {
  private String mode = "API_KEY";
  private Oidc oidc = new Oidc();

  public boolean isApiKeyEnabled() {
    return securityMode() == SecurityMode.API_KEY || securityMode() == SecurityMode.HYBRID;
  }

  public boolean isJwtEnabled() {
    return securityMode() == SecurityMode.OIDC || securityMode() == SecurityMode.HYBRID;
  }

  public SecurityMode securityMode() {
    return SecurityMode.parse(mode);
  }

  public String getMode() { return mode; }
  public void setMode(String mode) { this.mode = mode; }
  public Oidc getOidc() { return oidc; }
  public void setOidc(Oidc oidc) { this.oidc = oidc; }

  public enum SecurityMode {
    API_KEY, OIDC, HYBRID;

    static SecurityMode parse(String value) {
      if (value == null || value.isBlank()) return API_KEY;
      String normalized = value.trim().replace('-', '_').toUpperCase(java.util.Locale.ROOT);
      if (normalized.equals("APIKEY")) return API_KEY;
      try { return SecurityMode.valueOf(normalized); }
      catch (IllegalArgumentException ex) {
        throw new IllegalArgumentException("Unsupported NEBULAFLOW_SECURITY_MODE: " + value, ex);
      }
    }
  }

  public static class Oidc {
    private String issuerUri = "";
    private String jwkSetUri = "";
    private String audience = "";
    private String rolesClaim = "roles";
    private String tenantClaim = "tenants";
    private String principalClaim = "sub";

    public String getIssuerUri() { return issuerUri; }
    public void setIssuerUri(String issuerUri) { this.issuerUri = issuerUri; }
    public String getJwkSetUri() { return jwkSetUri; }
    public void setJwkSetUri(String jwkSetUri) { this.jwkSetUri = jwkSetUri; }
    public String getAudience() { return audience; }
    public void setAudience(String audience) { this.audience = audience; }
    public String getRolesClaim() { return rolesClaim; }
    public void setRolesClaim(String rolesClaim) { this.rolesClaim = rolesClaim; }
    public String getTenantClaim() { return tenantClaim; }
    public void setTenantClaim(String tenantClaim) { this.tenantClaim = tenantClaim; }
    public String getPrincipalClaim() { return principalClaim; }
    public void setPrincipalClaim(String principalClaim) { this.principalClaim = principalClaim; }
  }
}
