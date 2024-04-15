package com.github.cleanddd.core.model.course;

import com.github.cleanddd.core.model.InvalidDomainEntityError;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Course {

    @Getter
    @EqualsAndHashCode.Include
    private final Integer id;

    @Getter
    private final String title;

    private final AtomicInteger numberOfStudents;

    @Builder
    public Course(Integer id, String title, Integer numberOfStudents) {
        this.id = id;
        this.title = Optional.ofNullable(title)
                .filter(t -> !t.isBlank())
                .orElseThrow(() -> new InvalidDomainEntityError("Title is null or empty"));
        this.numberOfStudents = Optional.ofNullable(numberOfStudents)
                .map(AtomicInteger::new).orElse(new AtomicInteger(0));
    }

    public Integer getNumberOfStudents() {
        return numberOfStudents.intValue();
    }

    public Course enrollStudent() {
        // notice that we are not mutating the instance counter here
        return newCourse().numberOfStudents(numberOfStudents.get() + 1).build();
    }

    private CourseBuilder newCourse() {
        return Course.builder()
                .id(id)
                .title(title)
                .numberOfStudents(numberOfStudents.get());
    }

}
