package com.test.io.handler;

import java.io.IOException;
import java.io.UncheckedIOException;

public class UnCheckedIOExceptionConverterHandler<T> extends DecoratorHandler<T> {

    public UnCheckedIOExceptionConverterHandler(Handler<T> handler) {
        super(handler);
    }

    @Override
    public void handle(T t) {
        try {
            super.handle(t);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
