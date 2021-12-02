package com.hackathon.genericconnector.model;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class WorkFlow implements Serializable {

  @NotNull
  @Valid
  private String url;

  @NotNull
  @Valid
  private Integer threads;

  @NotNull
  @Valid
  private JsonNode request;

  @NotNull
  @Valid
  private String requestMethod;

  @NotNull
  @Valid
  private JsonNode headers;

  @NotNull
  @Valid
  private String authToken;

  @NotNull
  @Valid
  private String[] responseTags;

  @NotNull
  @Valid
  private Boolean writeToFile;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Integer getThreads() {
    return threads;
  }

  public void setThreads(Integer threads) {
    this.threads = threads;
  }

  public JsonNode getRequest() {
    return request;
  }

  public void setRequest(JsonNode request) {
    this.request = request;
  }

  public String getRequestMethod() {
    return requestMethod;
  }

  public void setRequestMethod(String requestMethod) {
    this.requestMethod = requestMethod;
  }

  public JsonNode getHeaders() {
    return headers;
  }

  public void setHeaders(JsonNode headers) {
    this.headers = headers;
  }

  public String getAuthToken() {
    return authToken;
  }

  public void setAuthToken(String authToken) {
    this.authToken = authToken;
  }

  public String[] getResponseTags() {
    return responseTags;
  }

  public void setResponseTags(String[] responseTags) {
    this.responseTags = responseTags;
  }

  public Boolean getWriteToFile() {
    return writeToFile;
  }

  public void setWriteToFile(Boolean writeToFile) {
    this.writeToFile = writeToFile;
  }
}