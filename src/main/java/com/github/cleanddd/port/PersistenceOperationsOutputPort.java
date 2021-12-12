package com.github.cleanddd.port;

import com.github.cleanddd.model.Course;
import com.github.cleanddd.model.Enrollment;
import com.github.cleanddd.model.Student;

import java.util.Set;

public interface PersistenceOperationsOutputPort {
    Integer persist(Course course);

    Course obtainCourseById(Integer courseId);

    boolean courseExistsWithTitle(String title);

    Integer persist(Student student);

    Student obtainStudentById(Integer studentId);

    boolean studentExistsWithFullName(String fullName);

    Set<Enrollment> findEnrollments(Integer studentId);
}
