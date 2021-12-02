package com.hackathon.risksense.genericconnector.workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.risksense.genericconnector.restclient.RestClient;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExecuteWorkFlow {

  public void executeAllWorkFlows(Map<String, Object> jsonNode) throws JsonProcessingException {

    List<Map<String, Object>> workflows = (List<Map<String, Object>>) jsonNode.get("workflows");
    int workFlowId =0;
    for (Map<String, Object> workflow : workflows) {
      execute(jsonNode, workflow, workFlowId);
      workFlowId++;
    }
  }

  public void execute(
    Map<String, Object> jsonNode,
    Map<String, Object> workflow,
    int workFlowId) throws JsonProcessingException {
    RestClient client = new RestClient();
    List<String> urls = getUrls(jsonNode,workflow);
    for (String url : urls) {
      String requestType = (String) workflow.get("requestType");
      String request = new ObjectMapper().writeValueAsString(workflow.get("request"));
      final Map<String, Object> requestMap = new ObjectMapper().readValue(request, Map.class);
      String headers = new ObjectMapper().writeValueAsString(workflow.get("headers"));
      final Map<String, String> headersMap = new ObjectMapper().readValue(headers, Map.class);
      List<String> response = (List<String>) workflow.get("response");

      client.getData(
        url,
        requestType,
        requestMap,
        headersMap,
        response,
        jsonNode,
        workFlowId
      );
    }
  }

  private String sanitiseUrlParam(String urlParam) {
    return urlParam.replace("{$", "").replace("}", "");
  }

  private List<String> getUrls(Map<String, Object> jsonNode, Map<String, Object> workflow) {
    if (workflow.get("urlParam") != null) {
      String urlParam = (String) workflow.get("urlParam");
      String sanitisedURLParam = updatePullAfterTime(sanitiseUrlParam(urlParam), jsonNode);
      String url = (String) workflow.get("url");
      if (jsonNode.get(sanitisedURLParam) instanceof List) {
        List<String> Ids = (List<String>) jsonNode.get(sanitisedURLParam);
        return Ids.stream().map(e -> url.replace(urlParam, e)).collect(Collectors.toList());
      } else {
        String value = (String) jsonNode.get(sanitisedURLParam);
        return Arrays.asList(url.replace(urlParam, value));
      }
    } else {
      String url = updatePullAfterTime((String) workflow.get("url"), jsonNode);
      return Arrays.asList(url);
    }
  }

  public String updatePullAfterTime(String url, Map<String, Object>  jsonNode) {
    return url.replace("{$dateTime}", (String) jsonNode.getOrDefault(
      "pullAfter", LocalDate.now().toString() + "T" + LocalTime.now().minusHours(1) + "Z"));
  }

}
