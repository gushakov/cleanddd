package com.github.cleanddd.infrastructure.adapter.db.map;

import com.github.cleanddd.core.model.course.Course;
import com.github.cleanddd.core.model.enrollment.Enrollment;
import com.github.cleanddd.core.model.student.Student;
import com.github.cleanddd.infrastructure.adapter.db.course.CourseEntity;
import com.github.cleanddd.infrastructure.adapter.db.enrollment.EnrollmentRow;
import com.github.cleanddd.infrastructure.adapter.db.student.StudentEntity;

public interface DbMapper {
    Course map(CourseEntity entity);

    CourseEntity map(Course model);

    Student map(StudentEntity entity);

    StudentEntity map(Student model);

    Enrollment map(EnrollmentRow row);
}
