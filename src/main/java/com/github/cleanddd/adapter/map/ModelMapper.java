package com.github.cleanddd.adapter.map;

import com.github.cleanddd.adapter.jpa.entity.CourseEntity;
import com.github.cleanddd.adapter.jpa.entity.StudentEntity;
import com.github.cleanddd.dto.EnrollmentRow;
import com.github.cleanddd.model.Course;
import com.github.cleanddd.model.Enrollment;
import com.github.cleanddd.model.Student;

public interface ModelMapper {
    Course map(CourseEntity entity);

    CourseEntity map(Course model);

    Student map(StudentEntity entity);

    StudentEntity map(Student model);

    Enrollment map(EnrollmentRow row);
}
