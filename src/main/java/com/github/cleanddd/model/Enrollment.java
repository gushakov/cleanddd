package com.github.cleanddd.model;


import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Enrollment {

    Integer courseId;

    String courseTitle;

}
