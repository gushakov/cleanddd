package com.github.cleanddd.core.usecase.enrollstudent;

import com.github.cleanddd.core.model.course.Course;
import com.github.cleanddd.core.model.enrollment.EnrollResult;
import com.github.cleanddd.core.model.enrollment.Enrollment;
import com.github.cleanddd.core.model.student.Student;
import com.github.cleanddd.core.port.db.PersistenceOperationsOutputPort;
import com.github.cleanddd.core.port.transaction.TransactionOperationsOutputPort;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class EnrollStudentUseCase implements EnrollStudentInputPort {

    EnrollStudentPresenterOutputPort presenter;
    TransactionOperationsOutputPort txOps;
    private final PersistenceOperationsOutputPort persistenceOps;

    @Override
    public void createCourse(String title) {

        try {
            txOps.doInTransaction(() -> {

                Integer courseId;
                if (persistenceOps.courseExistsWithTitle(title)) {
                    txOps.doAfterCommit(presenter::presentMessageWhenCreatingNewCourseIfItExistsAlready);
                    return;
                } else {
                    courseId = persistenceOps.persist(Course.builder()
                            .title(title)
                            .build());
                }
                txOps.doAfterCommit(() -> presenter.presentResultOfSuccessfulCreationOfNewCourse(courseId));

            });
        } catch (Exception e) {
            presenter.presentError(e);
        }

    }

    @Override
    public void createStudent(String fullName) {

        try {
            txOps.doInTransaction(() -> {

                Integer studentId;
                if (persistenceOps.studentExistsWithFullName(fullName)) {
                    txOps.doAfterCommit(presenter::presentMessageWhenCreatingNewStudentIfSheExistsAlready);
                    return;
                } else {
                    studentId = persistenceOps.persist(Student.builder()
                            .fullName(fullName)
                            .build());
                }
                txOps.doAfterCommit(() -> presenter.presentResultOfSuccessfulCreationOfNewStudent(studentId));

            });

        } catch (Exception e) {
            presenter.presentError(e);
        }

    }

    /*
        Point of interest
        -----------------
        1.  We are not using "org.springframework.transaction.annotation.Transactional"
            since it would introduce a (source-code) dependency to the
            outer layer, which is not allowed by the dependencies rule.
            The following point applies in this case, as well.
        2.  We are not using "javax.transaction.Transactional" annotation
            here. The transactional boundary is controlled entirely by
            the use case itself, because we consider demarcating logic
            as inherently belonging to the business scenario of the
            use case.
     */
    @Override
    public void enroll(Integer courseId, Integer studentId) {

        try {
            /*
                Point of interest
                -----------------
                We manually demarcate this use case with a transactional boundary,
                since, for the "enrollment" scenario, both aggregates may require
                updates to their state executed atomically.
             */
            txOps.doInTransaction(() -> {
                // start of transaction
                log.debug("[Transaction][Start] Start transaction for enrollment: student with ID {} to course with ID {}",
                        courseId, studentId);

                // try to enroll the student in the course
                final Student student = persistenceOps.obtainStudentById(studentId);
                EnrollResult enrollResult = student.enrollInCourse(courseId);

                // proceed only if enrollment has actually resulted in a new
                // course added to the set of student's courses
                if (enrollResult.isCourseAdded()) {
                    // save student aggregate root, will also save the "enrollment"
                    // entity (row in "enrollment" table)
                    persistenceOps.persist(enrollResult.getStudent());

                    final Course course = persistenceOps.obtainCourseById(courseId);
                    final Course updatedCourse = course.enrollStudent();

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
                }

                /*
                    Point of interest
                    -----------------
                    Successful outcome will be presented after the commit of
                    the current transaction. We do not want any errors in the
                    presentation resulting in a rollback of the transaction.
                 */

                // present the result of enrollment, after transaction commit
                txOps.doAfterCommit(() -> presenter.presentResultOfSuccessfulEnrollment(enrollResult));

                log.debug("[Transaction][End] End transaction for enrollment: student with ID {} to course with ID {}",
                        courseId, studentId);
                // end of transaction
            });

        } catch (Exception e) {

            /*
                Point of interest
                -----------------
                We are catching and presenting any errors which were not
                already presented inside the "doInTransaction()" method
                above. Thus, this error presentation will happen outside
                any active transaction and, as such, it will not influence
                the result of enrollment scenario (any database changes)
                even in case it fails and throws an exception.
             */

            presenter.presentError(e);
        }

    }

    @Override
    public void findEnrollmentsForStudent(Integer studentId) {
        try {
            txOps.doInTransaction(true, () -> {
                Set<Enrollment> enrollments = persistenceOps.findEnrollments(studentId);
                txOps.doAfterCommit(() -> presenter.presentResultOfQueryForStudentEnrollments(enrollments));
            });
        } catch (Exception e) {
            presenter.presentError(e);
        }
    }
}
