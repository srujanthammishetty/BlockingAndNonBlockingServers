package com.test.io.handler;

import java.io.IOException;

public class PrintingHandler<T> extends DecoratorHandler<T> {

    public PrintingHandler(Handler<T> other) {
        super(other);
    }

    @Override
    public void handle(T s) throws IOException {
        System.out.println("Connected to Socket : " + s);
        super.handle(s);
        System.out.println("DisConnected from Socket: " + s);
    }

}
