package de.dfki.asr.ajan.pluginsystem.rmlmappingplugin.apicaller;

import de.dfki.asr.ajan.pluginsystem.rmlmappingplugin.exception.APICallerExpetion;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.StringJoiner;
import org.json.JSONObject;

public class OLLamaCaller extends APICaller {

  private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(600);
  private static final String DEFAULT_MODEL = "qwen3";

  private final HttpClient httpClient;
  private final String model;

  public OLLamaCaller() {
    this("http://128.140.102.144:11434", null, DEFAULT_MODEL);
  }

  public OLLamaCaller(String apiEndpoint, String apiKey, String model) {
    super("OLLamaCaller", apiEndpoint, apiKey);
    this.model = Objects.requireNonNullElse(model, DEFAULT_MODEL);
    this.httpClient = HttpClient.newBuilder().connectTimeout(REQUEST_TIMEOUT).build();
  }

  @Override
  public String callAPI(String... payload) throws APICallerExpetion {
    try {
      StringJoiner messages = new StringJoiner(",\n");
      for (String part : payload) {
        messages.add("{\"role\":\"user\",\"content\":\"" + escapePayloadPart(part) + "\"}");
      }
      String combinedPayload = messages.toString();
      if (combinedPayload.isEmpty()) {
        throw new APICallerExpetion("Ollama payload cannot be empty");
      }
      HttpRequest request = buildRequest(combinedPayload);
      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
      if (response.statusCode() >= 400) {
        throw new APICallerExpetion(
            "Ollama call failed with status " + response.statusCode() + ": " + response.body());
      }
      return parseResponse(response.body());
    } catch (IOException e) {
      throw new APICallerExpetion("Ollama call failed: " + e.getMessage());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new APICallerExpetion("Ollama call interrupted");
    }
  }

  private HttpRequest buildRequest(String payload) throws APICallerExpetion {
    try {
      String endpoint = buildEndpoint();
      String body = buildRequestBody(payload);
      HttpRequest.Builder builder =
          HttpRequest.newBuilder()
              .uri(new URI(endpoint))
              .timeout(REQUEST_TIMEOUT)
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
      return builder.build();
    } catch (URISyntaxException e) {
      throw new APICallerExpetion("Invalid Ollama endpoint: " + e.getMessage());
    }
  }

  private String buildEndpoint() {
    String base = getApiEndpoint();
    if (base.endsWith("/")) {
      base = base.substring(0, base.length() - 1);
    }
    return base + "/api/chat";
  }

  private String buildRequestBody(String payload) throws APICallerExpetion {
    if (payload == null || payload.isBlank()) {
      throw new APICallerExpetion("Ollama payload cannot be empty");
    }
    return "{\"model\":\""
        + model
        + "\",\"messages\":["
        + payload
        + "],\"stream\":false, \"think\": false}";
  }

  private String parseResponse(String responseBody) {
    if (responseBody == null || responseBody.isBlank()) {
      return "";
    }
    return extractResponse(responseBody);
  }

  private String extractResponse(String responseBody) {
    JSONObject json = new JSONObject(responseBody);
    return json.getString("message.content");
  }

  private String escapePayloadPart(String part) {
    if (part == null) {
      return "";
    }
    String normalized = part.replace("\r\n", "\n").replace("\r", "\n");
    return normalized.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
  }
}
