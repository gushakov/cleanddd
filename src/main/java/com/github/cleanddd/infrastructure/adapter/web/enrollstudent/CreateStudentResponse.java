package com.github.cleanddd.infrastructure.adapter.web.enrollstudent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateStudentResponse {

    Boolean existsAlready;
    Integer studentId;

}
