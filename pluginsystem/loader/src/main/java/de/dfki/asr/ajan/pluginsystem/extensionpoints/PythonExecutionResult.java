package de.dfki.asr.ajan.pluginsystem.extensionpoints;

import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PythonExecutionResult {
  private final boolean success;
  private final String output;
  private final String error;
  private final Map<String, Object> results;
  private final long executionTimeMs;
  private final Exception exception;

  public static PythonExecutionResult success(
      String output, Map<String, Object> results, long executionTimeMs) {
    return PythonExecutionResult.builder()
        .success(true)
        .output(output)
        .results(results)
        .executionTimeMs(executionTimeMs)
        .build();
  }

  public static PythonExecutionResult failed(
      String error, Exception exception, long executionTimeMs) {
    return PythonExecutionResult.builder()
        .success(false)
        .error(error)
        .exception(exception)
        .executionTimeMs(executionTimeMs)
        .build();
  }
}
