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

        Integer courseId;
        try {
            if (persistenceOps.courseExistsWithTitle(title)) {
                presenter.presentMessageWhenCreatingNewCourseIfItExistsAlready();
                return;
            } else {
                courseId = persistenceOps.persist(Course.builder()
                        .title(title)
                        .build());
            }
        } catch (Exception e) {
            presenter.presentError(e);
            persistenceOps.rollback();
            return;
        }

        presenter.presentResultOfSuccessfulCreationOfNewCourse(courseId);

    }

    @Override
    @Transactional
    public void createStudent(String fullName) {

        Integer studentId;
        try {
            if (persistenceOps.studentExistsWithFullName(fullName)) {
                presenter.presentMessageWhenCreatingNewStudentIfSheExistsAlready();
                return;
            } else {
                studentId = persistenceOps.persist(Student.builder()
                        .fullName(fullName)
                        .build());
            }
        } catch (Exception e) {
            presenter.presentError(e);
            persistenceOps.rollback();
            return;
        }

        presenter.presentResultOfSuccessfulCreationOfNewStudent(studentId);

    }

    @Transactional
    @Override
    public void enroll(Integer courseId, Integer studentId) {
        EnrollResult enrollResult;
        try {
            // try to enroll the student in the course
            final Student student = persistenceOps.obtainStudentById(studentId);
            enrollResult = student.enrollInCourse(courseId);

            // proceed only if enrollment has actually resulted in a new
            // course added to the set of student's courses
            if (enrollResult.isCourseAdded()) {

                persistenceOps.persist(enrollResult.getStudent());

                final Course course = persistenceOps.obtainCourseById(courseId);
                final Course updatedCourse = course.enrollStudent();
                persistenceOps.persist(updatedCourse);
            }

        } catch (Exception e) {
            persistenceOps.rollback();
            presenter.presentError(e);
            return;
        }

        // present the result of enrollment
        presenter.presentResultOfSuccessfulEnrollment(enrollResult);

    }

    @Override
    public void findEnrollmentsForStudent(Integer studentId) {
        Set<Enrollment> enrollments = null;
        try {
            enrollments = persistenceOps.findEnrollments(studentId);
        } catch (Exception e) {
            presenter.presentError(e);
            return;
        }
        presenter.presentResultOfQueryForStudentEnrollments(enrollments);
    }
}
