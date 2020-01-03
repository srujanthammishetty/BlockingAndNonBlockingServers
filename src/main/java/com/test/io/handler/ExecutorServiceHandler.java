package com.test.io.handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

public class ExecutorServiceHandler<T> extends DecoratorHandler<T> {

    private ExecutorService pool;
    private Thread.UncaughtExceptionHandler exceptionHandler;

    public ExecutorServiceHandler(Handler<T> handler, ExecutorService pool, Thread.UncaughtExceptionHandler exceptionHandler) {
        super(handler);
        this.pool = pool;
        this.exceptionHandler = exceptionHandler;
    }


    public ExecutorServiceHandler(Handler<T> handler, ExecutorService pool) {
        this(handler,pool,(t, e) -> { System.out.println("UnCaughtException in Thread: " + t +" ,Exception "+e);});
    }

    @Override
    public void handle(T t) {
  /*      pool.submit(() -> {
                    super.handle(t);
                    return null;
                }
        );*/

    pool.submit(new FutureTask(()->{
        super.handle(t);
        return null; }){
        @Override
        protected void setException(Throwable t) {
            exceptionHandler.uncaughtException(Thread.currentThread(),t);
        }
    }) ;

    }
}
