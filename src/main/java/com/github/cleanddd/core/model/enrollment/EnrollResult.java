package com.github.cleanddd.core.model.enrollment;

import com.github.cleanddd.core.model.student.Student;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@Builder
@EqualsAndHashCode
public class EnrollResult {

    Student student;
    boolean courseAdded;

}
