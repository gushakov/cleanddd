package com.github.cleanddd.usecase;

import com.github.cleanddd.dto.CreateCourseResponse;
import com.github.cleanddd.dto.CreateStudentResponse;
import com.github.cleanddd.model.Course;
import com.github.cleanddd.model.EnrollResult;
import com.github.cleanddd.model.Student;
import com.github.cleanddd.port.EnrollStudentInputPort;
import com.github.cleanddd.port.PersistenceOperationsOutputPort;
import com.github.cleanddd.port.RestPresenterOutputPort;

import javax.transaction.Transactional;

public class EnrollStudentUseCase implements EnrollStudentInputPort {

    private final RestPresenterOutputPort restPresenter;
    private final PersistenceOperationsOutputPort persistenceOps;

    public EnrollStudentUseCase(RestPresenterOutputPort restPresenter, PersistenceOperationsOutputPort persistenceOps) {
        this.restPresenter = restPresenter;
        this.persistenceOps = persistenceOps;
    }

    @Override
    @Transactional
    public void createCourse(String title) {

        if (persistenceOps.courseExistsWithTitle(title)) {
            restPresenter.presentOk(CreateCourseResponse.builder().existsAlready(true));
        } else {
            final Integer courseId = persistenceOps.persist(Course.builder()
                    .title(title)
                    .build());

            restPresenter.presentOk(CreateCourseResponse.builder()
                    .existsAlready(false)
                    .courseId(courseId)
                    .build());
        }
    }

    @Override
    @Transactional
    public void createStudent(String fullName) {

        if (persistenceOps.studentExistsWithFullName(fullName)) {
            restPresenter.presentOk(CreateStudentResponse.builder().existsAlready(true).build());
        } else {
            final Integer studentId = persistenceOps.persist(Student.builder()
                    .fullName(fullName)
                    .build());

            restPresenter.presentOk(CreateStudentResponse.builder().existsAlready(false)
                    .studentId(studentId)
                    .build());
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
            restPresenter.presentOk(enrollResult);
        } catch (Exception e) {
            restPresenter.presentError(e);
        }
    }

    @Override
    public void findEnrollmentsForStudent(Integer studentId) {
        restPresenter.presentOk(persistenceOps.findEnrollments(studentId));
    }
}
