package com.github.cleanddd.model;

import com.github.cleanddd.core.model.InvalidDomainEntityError;
import com.github.cleanddd.core.model.enrollment.EnrollResult;
import com.github.cleanddd.core.model.student.Student;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ModelTest {

    @Test
    void testStudentBuilder_ErrorIfBuildWithEmptyFullName() {

        Assertions.assertThrows(InvalidDomainEntityError.class,
                () -> Student.builder().build());
        Assertions.assertThrows(InvalidDomainEntityError.class,
                () -> Student.builder()
                        .fullName(null)
                        .build());
        Assertions.assertThrows(InvalidDomainEntityError.class,
                () -> Student.builder()
                        .fullName("")
                        .build());
        Assertions.assertThrows(InvalidDomainEntityError.class,
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
