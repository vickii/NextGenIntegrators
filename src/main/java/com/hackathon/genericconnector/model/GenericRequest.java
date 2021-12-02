package com.hackathon.genericconnector.model;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.validation.annotation.Validated;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Validated
public class GenericRequest implements Serializable {

  @NotNull
  @NotBlank
  @NotEmpty
  private JsonNode variables;

  @Valid
  private WorkFlow[] workflows;

  public JsonNode getVariables() {
    return variables;
  }

  public void setVariables(JsonNode variables) {
    this.variables = variables;
  }

  public WorkFlow[] getWorkflows() {
    return workflows;
  }

  public void setWorkflows(WorkFlow[] workflows) {
    this.workflows = workflows;
  }
}
