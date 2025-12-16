package de.dfki.asr.ajan.pluginsystem.rmlmappingplugin.apicaller;

import de.dfki.asr.ajan.pluginsystem.rmlmappingplugin.exception.APICallerExpetion;
import lombok.Getter;

public abstract class APICaller {

  private final String apiEndpoint;
  private final String apiKey;
  @Getter private final String name;

  public APICaller(String name, String apiEndpoint, String apiKey) {
    this.name = name;
    this.apiEndpoint = apiEndpoint;
    this.apiKey = apiKey;
  }

  protected String getApiEndpoint() {
    return apiEndpoint;
  }

  protected String getApiKey() {
    return apiKey;
  }

  public abstract String callAPI(String... payload) throws APICallerExpetion;
}
