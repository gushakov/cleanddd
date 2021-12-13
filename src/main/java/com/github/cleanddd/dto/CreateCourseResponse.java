package com.github.cleanddd.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

// omit null fields from: https://www.baeldung.com/jackson-ignore-null-fields

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateCourseResponse {

    Boolean existsAlready;
    Integer courseId;
}
