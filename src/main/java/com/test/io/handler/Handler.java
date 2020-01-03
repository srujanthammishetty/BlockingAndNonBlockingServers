package com.test.io.handler;


import java.io.IOException;

public interface Handler<T> {
    void handle(T t) throws IOException;
}
