package com.github.cleanddd.infrastructure.adapter.web.enrollstudent;

import com.github.cleanddd.core.usecase.enrollstudent.EnrollStudentInputPort;
import com.github.cleanddd.infrastructure.adapter.db.enrollment.EnrollmentsQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

@RequiredArgsConstructor
@RestController
@Slf4j
public class EnrollStudentController {

    final WebApplicationContext appContext;

    @PostMapping("/create-course")
    public void createCourse(@RequestBody CreateCourseRequest createCourseRequest) {
        final EnrollStudentInputPort enrollStudentUseCase = getUseCase();
        enrollStudentUseCase.createCourse(createCourseRequest.getTitle());
    }

    @PostMapping("/create-student")
    public void createStudent(@RequestBody CreateStudentRequest createStudentRequest) {
        final EnrollStudentInputPort enrollStudentUseCase = getUseCase();
        enrollStudentUseCase.createStudent(createStudentRequest.getFullName());
    }

    @PostMapping("/enroll")
    public void enroll(@RequestBody EnrollRequest enrollRequest) {
        final EnrollStudentInputPort enrollStudentUseCase = getUseCase();
        enrollStudentUseCase.enroll(enrollRequest.getCourseId(),
                enrollRequest.getStudentId());
    }

    @PostMapping("/enrollments")
    public void enrollments(@RequestBody EnrollmentsQuery enrollmentsQuery) {
        final EnrollStudentInputPort enrollStudentUseCase = getUseCase();
        enrollStudentUseCase.findEnrollmentsForStudent(enrollmentsQuery.getStudentId());
    }

    /*
        Get the use case prototype bean from the web application context.
     */
    private EnrollStudentInputPort getUseCase() {
        return appContext.getBean(EnrollStudentInputPort.class);
    }

}
