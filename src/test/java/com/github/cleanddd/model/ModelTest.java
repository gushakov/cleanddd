package com.github.cleanddd.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ModelTest {

    @Test
    void testStudentBuilder_ErrorIfBuildWithEmptyFullName() {

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> Student.builder().build());
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> Student.builder()
                        .fullName(null)
                        .build());
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> Student.builder()
                        .fullName("")
                        .build());
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> Student.builder()
                        .fullName(" ")
                        .build());
    }

    @Test
    void testStudentIsImmutable() {
        final Student studentBefore = Student.builder()
                .id(1)
                .fullName("Brad Pitt")
                .build();

        final EnrollResult enrollResult = studentBefore.enrollInCourse(1);
        final Student studentAfter = enrollResult.getStudent();

        assertThat(studentBefore.getCoursesIds())
                .isEmpty();
        assertThat(studentAfter.getCoursesIds())
                .containsOnly(1);

    }
}
