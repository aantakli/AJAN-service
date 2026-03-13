package de.dfki.asr.ajan.pluginsystem.pythonplugin.exceptions;

import lombok.Getter;

@Getter
public class PythonExecutionException extends RuntimeException {
  private String scriptName;
  private int lineNumber = -1;
  private String pythonStackTrace;

  public PythonExecutionException(String message) {
    super(message);
  }

  public PythonExecutionException(String message, Throwable cause) {
    super(message, cause);
  }

  public PythonExecutionException(
      String message, String scriptName, int lineNumber, String pythonStackTrace) {
    super(message);
    this.scriptName = scriptName;
    this.lineNumber = lineNumber;
    this.pythonStackTrace = pythonStackTrace;
  }

  public PythonExecutionException(
      String message, String scriptName, int lineNumber, String pythonStackTrace, Throwable cause) {
    super(message, cause);
    this.scriptName = scriptName;
    this.lineNumber = lineNumber;
    this.pythonStackTrace = pythonStackTrace;
  }
}
