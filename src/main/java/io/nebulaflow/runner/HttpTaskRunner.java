package io.nebulaflow.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nebulaflow.engine.DagPlanner;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HttpTaskRunner implements TaskRunner {
  private static final Set<String> METHODS = Set.of("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD");
  private final ObjectMapper mapper;
  private final Duration timeout;
  private final Set<String> allowedHosts;
  private final long maxResponseBytes;
  private final HttpClient client;

  public HttpTaskRunner(
      ObjectMapper mapper,
      @Value("${nebulaflow.runners.http.default-timeout:10s}") Duration timeout,
      @Value("${nebulaflow.runners.http.allowed-hosts:}") String allowedHosts,
      @Value("${nebulaflow.runners.http.max-response-bytes:1048576}") long maxResponseBytes) {
    this.mapper = mapper;
    this.timeout = timeout;
    this.allowedHosts = parseHosts(allowedHosts);
    this.maxResponseBytes = Math.max(1, maxResponseBytes);
    this.client = HttpClient.newBuilder().connectTimeout(timeout).followRedirects(HttpClient.Redirect.NEVER).build();
  }

  @Override public String type() { return "HTTP"; }

  @Override public Object run(DagPlanner.Node node, Map<String, Object> context, TaskContext taskContext) throws Exception {
    String rawUrl = required(node.config(), "url");
    URI uri;
    try { uri = URI.create(rawUrl); }
    catch (IllegalArgumentException e) { throw new IllegalArgumentException("HTTP url is invalid", e); }
    if (!Set.of("http", "https").contains(uri.getScheme())) throw new IllegalArgumentException("HTTP url must use http or https");
    if (uri.getHost() == null || !isAllowed(uri.getHost())) throw new SecurityException("HTTP host is not allowlisted: " + uri.getHost());

    String method = String.valueOf(node.config().getOrDefault("method", "GET")).toUpperCase(Locale.ROOT);
    if (!METHODS.contains(method)) throw new IllegalArgumentException("unsupported HTTP method: " + method);
    HttpRequest.Builder builder = HttpRequest.newBuilder(uri).timeout(timeout);
    Map<String, Object> headers = asMap(node.config().get("headers"));
    headers.forEach((name, value) -> {
      if (Set.of("host", "content-length", "connection").contains(name.toLowerCase(Locale.ROOT)))
        throw new IllegalArgumentException("HTTP header is managed by the runner: " + name);
      builder.header(name, String.valueOf(value));
    });

    Object body = node.config().get("body");
    if (body == null || method.equals("GET") || method.equals("HEAD")) builder.method(method, HttpRequest.BodyPublishers.noBody());
    else builder.method(method, HttpRequest.BodyPublishers.ofString(body instanceof String s ? s : mapper.writeValueAsString(body)));

    HttpResponse<byte[]> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
    if (response.body().length > maxResponseBytes) throw new IllegalStateException("HTTP response exceeds configured size limit");
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("status", response.statusCode());
    result.put("success", response.statusCode() >= 200 && response.statusCode() < 300);
    result.put("body", new String(response.body(), java.nio.charset.StandardCharsets.UTF_8));
    response.headers().firstValue("content-type").ifPresent(value -> result.put("contentType", value));
    return result;
  }

  private boolean isAllowed(String host) {
    String normalized = host.toLowerCase(Locale.ROOT);
    return allowedHosts.stream().anyMatch(pattern -> pattern.equals(normalized)
        || (pattern.startsWith("*.") && normalized.endsWith(pattern.substring(1)) && !normalized.equals(pattern.substring(2))));
  }

  private static Set<String> parseHosts(String raw) {
    return Arrays.stream((raw == null ? "" : raw).split(",")).map(String::trim)
        .map(value -> value.toLowerCase(Locale.ROOT)).filter(value -> !value.isBlank())
        .collect(Collectors.toUnmodifiableSet());
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> asMap(Object value) {
    if (value == null) return Map.of();
    if (!(value instanceof Map<?, ?> map)) throw new IllegalArgumentException("HTTP headers must be an object");
    Map<String, Object> output = new LinkedHashMap<>();
    map.forEach((key, item) -> output.put(String.valueOf(key), item));
    return output;
  }

  private static String required(Map<String, Object> config, String key) {
    Object value = config.get(key);
    if (value == null || String.valueOf(value).isBlank()) throw new IllegalArgumentException("HTTP config requires " + key);
    return String.valueOf(value);
  }
}
