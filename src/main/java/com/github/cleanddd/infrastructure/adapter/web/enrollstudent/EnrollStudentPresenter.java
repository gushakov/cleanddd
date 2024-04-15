package com.github.cleanddd.infrastructure.adapter.web.enrollstudent;

import com.github.cleanddd.core.model.enrollment.EnrollResult;
import com.github.cleanddd.core.model.enrollment.Enrollment;
import com.github.cleanddd.core.usecase.enrollstudent.EnrollStudentPresenterOutputPort;
import com.github.cleanddd.infrastructure.adapter.web.AbstractRestPresenter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import javax.servlet.http.HttpServletResponse;
import java.util.Set;

public class EnrollStudentPresenter extends AbstractRestPresenter implements EnrollStudentPresenterOutputPort {

    public EnrollStudentPresenter(HttpServletResponse httpServletResponse, MappingJackson2HttpMessageConverter jacksonConverter) {
        super(httpServletResponse, jacksonConverter);
    }

    @Override
    public void presentMessageWhenCreatingNewCourseIfItExistsAlready() {
        try {
            presentOk(CreateCourseResponse
                    .builder()
                    .existsAlready(true)
                    .build());
        } catch (Exception e) {
            presentError(e);
        }
    }

    @Override
    public void presentResultOfSuccessfulCreationOfNewCourse(Integer courseId) {
        try {
            presentOk(CreateCourseResponse.builder()
                    .existsAlready(false)
                    .courseId(courseId)
                    .build());
        } catch (Exception e) {
            presentError(e);
        }
    }

    @Override
    public void presentMessageWhenCreatingNewStudentIfSheExistsAlready() {
        try {
            presentOk(CreateStudentResponse.builder()
                    .existsAlready(true)
                    .build());
        } catch (Exception e) {
            presentError(e);
        }
    }

    @Override
    public void presentResultOfSuccessfulCreationOfNewStudent(Integer studentId) {
        try {
            presentOk(CreateStudentResponse.builder().existsAlready(false)
                    .studentId(studentId)
                    .build());
        } catch (Exception e) {
            presentError(e);
        }
    }

    @Override
    public void presentResultOfSuccessfulEnrollment(EnrollResult enrollResult) {
        try {
            presentOk(enrollResult);
        } catch (Exception e) {
            presentError(e);
        }
    }

    @Override
    public void presentResultOfQueryForStudentEnrollments(Set<Enrollment> enrollments) {
        try {
            presentOk(enrollments);
        } catch (Exception e) {
            presentError(e);
        }
    }
}
