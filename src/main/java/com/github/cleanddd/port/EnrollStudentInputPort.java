package com.github.cleanddd.port;

public interface EnrollStudentInputPort {
    void createCourse(String title);

    void createStudent(String fullName);

    void enroll(Integer courseId, Integer studentId);

    void findEnrollmentsForStudent(Integer studentId);
}
