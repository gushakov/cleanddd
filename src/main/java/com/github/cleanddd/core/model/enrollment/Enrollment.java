package com.github.cleanddd.core.model.enrollment;


import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Enrollment {

    Integer courseId;

    String courseTitle;

}
