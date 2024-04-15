package com.github.cleanddd.core.usecase.enrollstudent;

import com.github.cleanddd.core.model.enrollment.EnrollResult;
import com.github.cleanddd.core.model.enrollment.Enrollment;
import com.github.cleanddd.core.port.ErrorHandlingPresenterOutputPort;

import java.util.Set;

public interface EnrollStudentPresenterOutputPort extends ErrorHandlingPresenterOutputPort {

    void presentMessageWhenCreatingNewCourseIfItExistsAlready();

    void presentResultOfSuccessfulCreationOfNewCourse(Integer courseId);

    void presentMessageWhenCreatingNewStudentIfSheExistsAlready();

    void presentResultOfSuccessfulCreationOfNewStudent(Integer studentId);

    void presentResultOfSuccessfulEnrollment(EnrollResult enrollResult);

    void presentResultOfQueryForAllEnrollments(Set<Enrollment> enrollments);
}
