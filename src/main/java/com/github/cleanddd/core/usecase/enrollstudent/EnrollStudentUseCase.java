package com.github.cleanddd.core.usecase.enrollstudent;

import com.github.cleanddd.core.model.course.Course;
import com.github.cleanddd.core.model.enrollment.EnrollResult;
import com.github.cleanddd.core.model.enrollment.Enrollment;
import com.github.cleanddd.core.model.student.Student;
import com.github.cleanddd.core.port.db.PersistenceOperationsOutputPort;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Slf4j
public class EnrollStudentUseCase implements EnrollStudentInputPort {

    private final EnrollStudentPresenterOutputPort presenter;
    private final PersistenceOperationsOutputPort persistenceOps;

    public EnrollStudentUseCase(EnrollStudentPresenterOutputPort presenter, PersistenceOperationsOutputPort persistenceOps) {
        this.presenter = presenter;
        this.persistenceOps = persistenceOps;
    }

    @Override
    public void createCourse(String title) {

        try {
            Integer courseId;
            if (persistenceOps.courseExistsWithTitle(title)) {
                presenter.presentMessageWhenCreatingNewCourseIfItExistsAlready();
                return;
            } else {
                courseId = persistenceOps.persist(Course.builder()
                        .title(title)
                        .build());
            }
            presenter.presentResultOfSuccessfulCreationOfNewCourse(courseId);
        } catch (Exception e) {
            presenter.presentError(e);
        }

    }

    @Override
    public void createStudent(String fullName) {

        try {
            Integer studentId;
            if (persistenceOps.studentExistsWithFullName(fullName)) {
                presenter.presentMessageWhenCreatingNewStudentIfSheExistsAlready();
                return;
            } else {
                studentId = persistenceOps.persist(Student.builder()
                        .fullName(fullName)
                        .build());
            }
            presenter.presentResultOfSuccessfulCreationOfNewStudent(studentId);
        } catch (Exception e) {
            presenter.presentError(e);
        }

    }

    /*
        Point of interest
        -----------------
        We are not using "javax.transaction.Transaction" annotation
        here. The transactional boundary is drawn tighter, just around
        the DB updates themselves. This allows to call the presenter
        outside the transactional boundary.
     */
    @Override
    public void enroll(Integer courseId, Integer studentId) {
        try {
            EnrollResult enrollResult;
            // try to enroll the student in the course
            final Student student = persistenceOps.obtainStudentById(studentId);
            enrollResult = student.enrollInCourse(courseId);

            // proceed only if enrollment has actually resulted in a new
            // course added to the set of student's courses
            if (enrollResult.isCourseAdded()) {

                final Course course = persistenceOps.obtainCourseById(courseId);
                final Course updatedCourse = course.enrollStudent();

                /*
                    Point of interest
                    -----------------
                    We are saving both aggregate roots in a single transaction.
                 */
                persistenceOps.doInTransaction(() -> {
                    log.debug("[Transaction][Start] Start transaction for enrollment: student with ID {} to course with ID {}",
                            courseId, studentId);

                    // save student aggregate root, will also save the "enrollment"
                    // entity (row in "enrollment" table)
                    persistenceOps.persist(enrollResult.getStudent());

                    /*
                        Point of interest
                        -----------------
                        We can try to provoke an error here to see how transactional boundary
                        is enforced. We can try running something like: "int t = 1/0;", for
                        example. Then we can see in the console how the transaction is
                        rolled back without either a new enrollment or a students counter
                        in the course being updated.
                     */

                    // save course aggregate root
                    persistenceOps.persist(updatedCourse);
                    log.debug("[Transaction][End] End transaction for enrollment: student with ID {} to course with ID {}",
                            courseId, studentId);
                });
            }

            /*
                Point of interest
                -----------------
                Presentation logic will be executed outside the transaction.
                We do not want any errors in presentation resulting in
                a rollback of the transaction.
             */

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
            return;
        }
        presenter.presentResultOfQueryForStudentEnrollments(enrollments);
    }
}
