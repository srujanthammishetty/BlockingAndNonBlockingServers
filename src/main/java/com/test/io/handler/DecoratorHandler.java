package com.test.io.handler;

import java.io.IOException;

public abstract class DecoratorHandler<T> implements Handler<T> {

    private final Handler<T> other;

    public DecoratorHandler(Handler<T> other) {
        this.other = other;
    }

    @Override
    public void handle(T t) throws IOException {
        other.handle(t);
    }
}
