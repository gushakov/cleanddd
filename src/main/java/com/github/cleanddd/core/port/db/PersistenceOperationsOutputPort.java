package com.github.cleanddd.core.port.db;

import com.github.cleanddd.core.model.course.Course;
import com.github.cleanddd.core.model.enrollment.Enrollment;
import com.github.cleanddd.core.model.student.Student;

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
