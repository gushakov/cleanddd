package com.github.cleanddd.usecase;

import com.github.cleanddd.port.EnrollStudentInputPort;
import com.github.cleanddd.port.PersistenceOperationsOutputPort;
import com.github.cleanddd.presenter.RestPresenter;
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
        return new EnrollStudentUseCase(new RestPresenter(httpServletResponse, jackson2HttpMessageConverter), persistenceOps);
    }

}
