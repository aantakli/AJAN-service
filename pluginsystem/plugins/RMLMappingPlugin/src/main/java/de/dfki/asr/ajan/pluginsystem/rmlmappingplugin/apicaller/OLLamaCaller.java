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
    String contentObject = extractContentObject(responseBody);
    if (contentObject != null) {
      return contentObject;
    }
    return extractLegacyResponse(responseBody);
  }

  private String extractContentObject(String responseBody) {
    int contentIndex = responseBody.indexOf("\"content\":");
    if (contentIndex == -1) {
      return null;
    }
    int start = responseBody.indexOf('{', contentIndex);
    if (start == -1) {
      return null;
    }
    int depth = 0;
    boolean inString = false;
    for (int i = start; i < responseBody.length(); i++) {
      char current = responseBody.charAt(i);
      if (current == '"' && (i == 0 || responseBody.charAt(i - 1) != '\\')) {
        inString = !inString;
      }
      if (inString) {
        continue;
      }
      if (current == '{') {
        depth++;
      } else if (current == '}') {
        depth--;
        if (depth == 0) {
          return responseBody.substring(start, i + 1);
        }
      }
    }
    return null;
  }

  private String extractLegacyResponse(String responseBody) {
    int responseIndex = responseBody.indexOf("\"response\":");
    if (responseIndex == -1) {
      return responseBody;
    }
    int start = responseBody.indexOf('"', responseIndex + 12);
    int end = responseBody.indexOf('"', start + 1);
    if (start == -1 || end == -1 || end <= start) {
      return responseBody;
    }
    return responseBody
        .substring(start + 1, end)
        .replace("\\n", "\n")
        .replace("\\\"", "\"")
        .replace("\\\\", "\\");
  }

  private String escapePayloadPart(String part) {
    if (part == null) {
      return "";
    }
    String normalized = part.replace("\r\n", "\n").replace("\r", "\n");
    return normalized.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
  }
}
