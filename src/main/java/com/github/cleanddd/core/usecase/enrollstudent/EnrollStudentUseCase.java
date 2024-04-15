package com.github.cleanddd.core.usecase.enrollstudent;

import com.github.cleanddd.core.model.course.Course;
import com.github.cleanddd.core.model.enrollment.EnrollResult;
import com.github.cleanddd.core.model.enrollment.Enrollment;
import com.github.cleanddd.core.model.student.Student;
import com.github.cleanddd.core.port.db.PersistenceOperationsOutputPort;

import javax.transaction.Transactional;
import java.util.Set;

public class EnrollStudentUseCase implements EnrollStudentInputPort {

    private final EnrollStudentPresenterOutputPort presenter;
    private final PersistenceOperationsOutputPort persistenceOps;

    public EnrollStudentUseCase(EnrollStudentPresenterOutputPort presenter, PersistenceOperationsOutputPort persistenceOps) {
        this.presenter = presenter;
        this.persistenceOps = persistenceOps;
    }

    @Override
    @Transactional
    public void createCourse(String title) {

        if (persistenceOps.courseExistsWithTitle(title)) {
            presenter.presentMessageWhenCreatingNewCourseIfItExistsAlready();
        } else {
            final Integer courseId = persistenceOps.persist(Course.builder()
                    .title(title)
                    .build());

            presenter.presentResultOfSuccessfulCreationOfNewCourse(courseId);
        }
    }

    @Override
    @Transactional
    public void createStudent(String fullName) {

        if (persistenceOps.studentExistsWithFullName(fullName)) {
            presenter.presentMessageWhenCreatingNewStudentIfSheExistsAlready();
        } else {
            final Integer studentId = persistenceOps.persist(Student.builder()
                    .fullName(fullName)
                    .build());

            presenter.presentResultOfSuccessfulCreationOfNewStudent(studentId);
        }
    }

    @Transactional
    @Override
    public void enroll(Integer courseId, Integer studentId) {
        try {

            // try to enroll the student in the course
            final Student student = persistenceOps.obtainStudentById(studentId);
            final EnrollResult enrollResult = student.enrollInCourse(courseId);

            // proceed only if enrollment has actually resulted in a new
            // course added to the set of student's courses
            if (enrollResult.isCourseAdded()) {

                persistenceOps.persist(enrollResult.getStudent());

                final Course course = persistenceOps.obtainCourseById(courseId);
                final Course updatedCourse = course.enrollStudent();
                persistenceOps.persist(updatedCourse);
            }

            // present the result of enrollment
            presenter.presentResultOfSuccessfulEnrollment(enrollResult);
        } catch (Exception e) {
            presenter.presentError(e);
        }
    }

    @Override
    public void findEnrollmentsForStudent(Integer studentId) {
        Set<Enrollment> enrollments = null;
        try {
            enrollments = persistenceOps.findEnrollments(studentId);
        } catch (Exception e) {
            presenter.presentError(e);
        }
        presenter.presentResultOfQueryForAllEnrollments(enrollments);
    }
}
