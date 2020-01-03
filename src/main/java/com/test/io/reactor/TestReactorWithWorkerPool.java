package com.test.io.reactor;

import java.util.concurrent.Executors;

public class TestReactorWithWorkerPool {
    public static void main(String[] args) {
        ReactorWithWorkerPool reactorWithWorkerPool = new ReactorWithWorkerPool("localhost", 8090, Executors.newFixedThreadPool(10));
        reactorWithWorkerPool.run();
        ;
    }
}
