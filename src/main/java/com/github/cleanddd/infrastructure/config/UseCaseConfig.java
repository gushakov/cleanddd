package com.github.cleanddd.infrastructure.config;

import com.github.cleanddd.core.port.db.PersistenceOperationsOutputPort;
import com.github.cleanddd.core.usecase.enrollstudent.EnrollStudentInputPort;
import com.github.cleanddd.core.usecase.enrollstudent.EnrollStudentUseCase;
import com.github.cleanddd.infrastructure.adapter.web.enrollstudent.EnrollStudentPresenter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpServletResponse;

@Configuration
public class UseCaseConfig {

    @Bean
    @Scope(WebApplicationContext.SCOPE_REQUEST)
    public EnrollStudentInputPort enrollStudentUseCase(PersistenceOperationsOutputPort persistenceOps,
                                                       HttpServletResponse httpServletResponse,
                                                       MappingJackson2HttpMessageConverter jackson2HttpMessageConverter) {
        return new EnrollStudentUseCase(new EnrollStudentPresenter(httpServletResponse, jackson2HttpMessageConverter),
                persistenceOps);
    }

}
