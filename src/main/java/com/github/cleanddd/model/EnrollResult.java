package com.github.cleanddd.model;

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
