package de.dfki.asr.ajan.pluginsystem.pythonplugin;

/** No usable Python environment; the message names what is missing. */
public class PythonUnavailableException extends Exception {

  private static final long serialVersionUID = 1L;

  public PythonUnavailableException(String message) {
    super(message);
  }

  public PythonUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
