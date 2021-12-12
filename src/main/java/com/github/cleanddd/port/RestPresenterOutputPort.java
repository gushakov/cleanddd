package com.github.cleanddd.port;

public interface RestPresenterOutputPort {
    // copied from https://github.com/gushakov/clean-rest
    <T> void presentOk(T content);

    void presentError(Throwable t);
}
