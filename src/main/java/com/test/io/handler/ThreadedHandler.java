package com.test.io.handler;

public class ThreadedHandler<T> extends UnCheckedIOExceptionConverterHandler<T> {

    public ThreadedHandler(Handler<T> handler) {
        super(handler);
    }

    @Override
    public void handle(T t) {
        new Thread(() -> super.handle(t));
    }
}
