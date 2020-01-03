package com.test.io.reactor;

import java.io.IOException;

public class TestReactor {
    public static void main(String[] args) throws IOException {
/*
        Reactor reactor = new Reactor("localhost",8090);
        reactor.run();
*/

        ReactorTest reactor = new ReactorTest(8090,true);
        reactor.run();

    }
}
