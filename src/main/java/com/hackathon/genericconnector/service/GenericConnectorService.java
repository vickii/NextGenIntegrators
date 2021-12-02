package com.hackathon.genericconnector.service;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.risksense.genericconnector.workflow.ExecuteWorkFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class GenericConnectorService {

  public void saveWorkFlowFile(String genericRequest){

    if (writeRequestToFile(genericRequest)) {
      ObjectMapper mapper = new ObjectMapper();
      Map<String, Object> jsonMap = null;
      try {
        jsonMap = mapper.readValue(Paths.get("request.json").toFile(), Map.class);
      } catch (IOException e) {
        e.printStackTrace();
      }
      ExecuteWorkFlow workFlow = new ExecuteWorkFlow();
      try {
        workFlow.executeAllWorkFlows(jsonMap);
      } catch (JsonProcessingException e) {
        e.printStackTrace();
      }
    }
  }

  private Boolean writeRequestToFile(String genericRequest) {
    try (
      Writer writer = new BufferedWriter(new OutputStreamWriter(
        new FileOutputStream("request.json"), "utf-8"))) {
      writer.write(genericRequest);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return true;
  }
}
