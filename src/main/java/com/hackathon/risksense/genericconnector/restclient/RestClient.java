package com.hackathon.risksense.genericconnector.restclient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.util.*;

public class RestClient {

  public Map<String, Object> getData(
    String url,
    String requestType,
    Map<String, Object> request,
    Map<String, String> headers,
    List<String> responseFields,
    Map<String, Object> workflows,
    int workFlowId) {

    if(requestType.equalsIgnoreCase("GET")) {
      return doGet(url, headers, responseFields, workflows, workFlowId );
    } else if(requestType.equalsIgnoreCase("POST")) {
      return doPost(url, headers, request, responseFields, workflows, workFlowId);
    }
    throw new RuntimeException("requestType "+requestType+" not supported");
  }

  private Map<String, Object> doPost(
    String url,
    Map<String, String> headers,
    Map<String, Object> request,
    List<String> responseFields,
    Map<String, Object> workflows,
    int workFlowId) {
    DefaultHttpClient httpClient = new DefaultHttpClient();
    try {
      String encodedURL = url.replace(" ","+");
      HttpPost postRequest = new HttpPost(encodedURL);
      if (headers.containsKey("Authorization") &&
        headers.getOrDefault("Authorization", "").contentEquals("Bearer")) {
        headers.put("Authorization", "Bearer "+workflows.get("Token"));
      }
      headers.forEach(postRequest::addHeader);
      addEnviromentVariablestoRequest(request, workflows);
      postRequest.setEntity(new StringEntity(new ObjectMapper().writeValueAsString(request)));
      HttpResponse response = httpClient.execute(postRequest);
      HttpEntity httpEntity = response.getEntity();
      final Map<String, Object> responseMap = new ObjectMapper().readValue(EntityUtils.toString(httpEntity), Map.class);
      responseFields.stream().forEach(e -> searchForFieldInResponse(responseMap, responseFields, workflows, workFlowId));
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
    return workflows;
  }

  private void addEnviromentVariablestoRequest(Map<String, Object> request, Map<String, Object> workflows) {
    for (String key : request.keySet()) {
      if(request.get(key) instanceof String && ((String) request.get(key)).startsWith("$")) {
        String value = ((String) request.get(key)).replace("$", "");
        request.put(key, workflows.get(value));
      }
    }
  }

  private Map<String, Object> doGet(String url, Map<String, String> headers, List<String> responseFields, Map<String, Object> workflows, int workFlowId) {
    DefaultHttpClient httpClient = new DefaultHttpClient();
    try {
      String encodedURL = url.replace(" ","+");
      HttpGet getRequest = new HttpGet(encodedURL);
      if (headers.containsKey("Authorization") &&
        headers.getOrDefault("Authorization", "").contentEquals("Bearer")) {
        headers.put("Authorization", "Bearer "+workflows.get("Token"));
      } else if (headers.containsKey("Authorization") &&
        headers.getOrDefault("Authorization", "").contentEquals("JWT")) {
        headers.put("Authorization", "JWT "+workflows.get("token"));
      }
      headers.forEach(getRequest::addHeader);
      HttpResponse response = httpClient.execute(getRequest);
      HttpEntity httpEntity = response.getEntity();
      String responseString = EntityUtils.toString(httpEntity);
      if(new ObjectMapper().readValue(responseString, JsonNode.class).isArray()) {
        final List<Map<String, Object>> responseMapList =
          new ObjectMapper().readValue(responseString , new TypeReference<List<Map<String, Object>>>(){});
        responseMapList.forEach(e -> searchForFieldInResponse(e, responseFields, workflows, workFlowId));
      } else {
        final Map<String, Object> responseMap =
          new ObjectMapper().readValue(responseString, Map.class);
        if (responseFields != null && responseFields.isEmpty()) {
          responseFields
            .stream()
            .forEach(e -> searchForFieldInResponse(responseMap, responseFields, workflows, workFlowId));
        } else {
          searchForFieldInResponse(responseMap, responseFields, workflows, workFlowId);
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      httpClient.getConnectionManager().shutdown();
    }
    return workflows;
  }

  private void searchForFieldInResponse(
    Map<String, Object> responseMap,
    List<String> responseFields,
    Map<String, Object> workflows,
    int workFlowId) {

    List<Map<String,Object>> workflowsList = (List<Map<String,Object>>) workflows.get("workflows");
    Map<String, Object> currentWorkFlow = workflowsList.get(workFlowId);
    Boolean writeToFile = (Boolean) currentWorkFlow.get("writeToFile");

    if (writeToFile!=null && writeToFile) {
      String name = (String) workflows.get("name");
      // deleteFiles(name);
      try (
        Writer writer = new BufferedWriter(new OutputStreamWriter(
        new FileOutputStream(name+"-"+System.currentTimeMillis()+".txt"), "utf-8"))) {
        writer.write(new ObjectMapper().writeValueAsString(responseMap));
        if(responseFields == null) {
          return;
        }
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    for(String response: responseFields) {
      Map<String,Object> object = searchForKeyInResponse(response, responseMap);
      // workflows.putAll(object);
      for(String key: object.keySet()) {
        if(workflows.containsKey(key)) {
          if(workflows.get(key) instanceof List) {
            List<Object> existingList = (List<Object>) workflows.get(key);
            existingList.add(object.get(key));
            workflows.put(key , existingList);
          } else {
            final List<Object> newList = new ArrayList<>();
            newList.add(workflows.get(key));
            newList.add(object.get(key));
            workflows.put(key , newList);
          }
        } else {
          workflows.putAll(object);
        }
      }
    }
  }

  private void deleteFiles(String name) {
    File directory = new File(System.getProperty("user.dir"));
    for (File f : directory.listFiles()) {
      if (f.getName().startsWith(name)) {
        f.delete();
      }
    }
  }

  private Map<String,Object> searchForKeyInResponse(String response, Map<String, Object> responseMap) {

    for (String key : responseMap.keySet()) {
      Object obj = responseMap.get(response);
      JsonNode node = new ObjectMapper().convertValue(obj, JsonNode. class);
      if(node.isNumber() || node.isTextual() || node.isBoolean()) {
        if(key.contentEquals(response)) {
          Map<String, Object> map = new HashMap();
          map.put(response, obj);
          return map;
        }
      } else if(node.isArray()) {
        if(key.contentEquals(response)) {
          Map<String, Object> map = new HashMap();
          map.put(response, response);
          return map;
        } else {
          Map<String, Object> innerMap = (Map<String, Object>) responseMap.get(key);
          return searchForKeyInResponse(key, innerMap);
        }
      } else {
        Map<String, Object> innerMap = (Map<String, Object>) responseMap.get(key);
        return searchForKeyInResponse(key, innerMap);
      }

    }
    return new HashMap<>();
  }

}
