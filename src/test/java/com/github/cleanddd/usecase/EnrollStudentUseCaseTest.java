package com.github.cleanddd.usecase;

import com.github.cleanddd.core.model.course.Course;
import com.github.cleanddd.core.model.enrollment.EnrollResult;
import com.github.cleanddd.core.model.student.Student;
import com.github.cleanddd.core.port.db.PersistenceOperationsOutputPort;
import com.github.cleanddd.core.port.transaction.TransactionOperationsOutputPort;
import com.github.cleanddd.core.port.transaction.TransactionRunnableWithoutResult;
import com.github.cleanddd.core.usecase.enrollstudent.EnrollStudentPresenterOutputPort;
import com.github.cleanddd.core.usecase.enrollstudent.EnrollStudentUseCase;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EnrollStudentUseCaseTest {

    @Mock
    private EnrollStudentPresenterOutputPort presenter;

    @Mock
    private TransactionOperationsOutputPort txOps;

    @Mock
    private PersistenceOperationsOutputPort persistenceOps;


    @BeforeEach
    void setUp() {

        // return a mock course when asked to obtain one
        lenient().when(persistenceOps.obtainCourseById(anyInt()))
                .thenAnswer(invocation -> Course.builder()
                        .id(invocation.getArgument(0))
                        .title("Software architecture 101")
                        .build());

        // return a mock student enrolled in some courses when asked to obtain one
        lenient().when(persistenceOps.obtainStudentById(anyInt()))
                .thenAnswer(invocation -> Student.builder()
                        .id(invocation.getArgument(0))
                        .fullName("Brad Pitt")
                        .coursesIds(Set.of(1, 2, 3))
                        .build());

        // mock transaction operations
        lenient().doAnswer(invocation -> {
            ((TransactionRunnableWithoutResult) invocation.getArgument(0)).run();
            return null;
        }).when(txOps).doInTransaction(any(TransactionRunnableWithoutResult.class));

        lenient().doAnswer(invocation -> {
            ((TransactionRunnableWithoutResult) invocation.getArgument(0)).run();
            return null;
        }).when(txOps).doAfterCommit(any(TransactionRunnableWithoutResult.class));

        lenient().doAnswer(invocation -> {
            ((TransactionRunnableWithoutResult) invocation.getArgument(0)).run();
            return null;
        }).when(txOps).doAfterRollback(any(TransactionRunnableWithoutResult.class));

        // just return the ID a course or a student when asked to
        // persist it
        lenient().when(persistenceOps.persist(any(Course.class)))
                .thenAnswer(invocation -> ((Course) invocation.getArgument(0)).getId());

        lenient().when(persistenceOps.persist(any(Student.class)))
                .thenAnswer(invocation -> ((Student) invocation.getArgument(0)).getId());
    }

    @Test
    void testEnroll_StudentCanEnrollInCourseSheIsNotYetEnrolledIn() {

        final EnrollStudentUseCase useCase = new EnrollStudentUseCase(presenter, txOps, persistenceOps);

        // enroll student in a new course
        useCase.enroll(4, 1);
        assertNoError();

        // verify the result of enrollment
        final ArgumentCaptor<EnrollResult> resultArg = ArgumentCaptor.forClass(EnrollResult.class);
        verify(presenter, times(1))
                .presentResultOfSuccessfulEnrollment(resultArg.capture());

        final EnrollResult enrollResult = resultArg.getValue();
        Assertions.assertThat(enrollResult.isCourseAdded()).isTrue();
        Assertions.assertThat(enrollResult.getStudent().getCoursesIds())
                .containsOnly(1, 2, 3, 4);
    }

    private void assertNoError() {
        verify(presenter, times(0)).presentError(any(Exception.class));
    }
}
