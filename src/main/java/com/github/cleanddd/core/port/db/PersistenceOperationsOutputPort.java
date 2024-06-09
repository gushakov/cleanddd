package com.github.cleanddd.core.port.db;

import com.github.cleanddd.core.model.course.Course;
import com.github.cleanddd.core.model.enrollment.Enrollment;
import com.github.cleanddd.core.model.student.Student;

import java.util.Set;

public interface PersistenceOperationsOutputPort {

    /**
     * Executes provided {@linkplain Runnable} in a transaction configured
     * with default propagation strategy and isolation level.
     *
     * @param runnable runnable to execute
     * @throws PersistenceError if there was a problem setting up or executing
     *                          a transaction, all other {@linkplain RuntimeException}s
     *                          which may be thrown by the {@code runnable} itself are
     *                          propagated to the caller
     */
    void doInTransaction(Runnable runnable);

    void doAfterCommit(Runnable runnable);

    void doAfterRollback(Runnable runnable);

    Integer persist(Course course);

    Course obtainCourseById(Integer courseId);

    boolean courseExistsWithTitle(String title);

    Integer persist(Student student);

    Student obtainStudentById(Integer studentId);

    boolean studentExistsWithFullName(String fullName);

    Set<Enrollment> findEnrollments(Integer studentId);
}
