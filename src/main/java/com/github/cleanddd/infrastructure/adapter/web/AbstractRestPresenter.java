package com.github.cleanddd.infrastructure.adapter.web;

import com.github.cleanddd.core.port.ErrorHandlingPresenterOutputPort;
import com.github.cleanddd.core.port.db.EntityDoesNotExistError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.DelegatingServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

// copied from https://github.com/gushakov/clean-rest

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractRestPresenter implements ErrorHandlingPresenterOutputPort {

    private final HttpServletResponse httpServletResponse;
    private final MappingJackson2HttpMessageConverter jacksonConverter;

    /**
     * Uses {@linkplain MappingJackson2HttpMessageConverter} of this presenter to
     * serialize {@code content} and write it to current HTTP response. Subclasses
     * (concrete presenters) must catch all exceptions thrown from this method.
     *
     * @param content any object which can be serialized to JSON
     * @param <T>     any type
     * @throws RuntimeException if {@code content} cannot be serialized
     */
    protected <T> void presentOk(T content) {

        final DelegatingServerHttpResponse httpOutputMessage =
                new DelegatingServerHttpResponse(new ServletServerHttpResponse(httpServletResponse));

        httpOutputMessage.setStatusCode(HttpStatus.OK);

        try {
            jacksonConverter.write(content, MediaType.APPLICATION_JSON, httpOutputMessage);
        } catch (IOException e) {
            // not propagating a technical error back to use case
            // to avoid an error handling loop
            log.error(e.getMessage(), e);
        }

    }

    @Override
    public void presentError(Exception e) {

        /*
            Point of interest
            -----------------
            We can simulate an error in presentation to see how transactional
            demarcation works.
            For example, using "int t = 1/0;".
         */

        try {
            final DelegatingServerHttpResponse httpOutputMessage =
                    new DelegatingServerHttpResponse(new ServletServerHttpResponse(httpServletResponse));

            if (e instanceof EntityDoesNotExistError) {
                httpOutputMessage.setStatusCode(HttpStatus.BAD_REQUEST);
            } else {
                httpOutputMessage.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            jacksonConverter.write(Map.of("error", String.valueOf(e.getMessage())),
                    MediaType.APPLICATION_JSON, httpOutputMessage);
        } catch (IOException ex) {
            // not propagating a technical error back to use case
            // to avoid an error handling loop
            log.error(ex.getMessage(), ex);
        }

    }

}
