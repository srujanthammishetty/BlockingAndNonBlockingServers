package com.test.io.server.io;


import com.test.io.handler.ExecutorServiceHandler;
import com.test.io.handler.Handler;
import com.test.io.handler.PrintingHandler;
import com.test.io.handler.TransmorgifyHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

/**
 * Blocking Server which accepts multiple connections and uses worker pool threads to serve each request.
 * <p>
 * Instead of spawning new thread for each connection as implemented in {@link MultiThreadedBlockingServer}, this class
 * effectively creates a worker pool of threads and reuses the same threads for serving each request.
 * <p>
 * This prevents from system getting crash down with {@link java.lang.OutOfMemoryError}
 */

public class ExecutorServiceBlockingServer {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8090);

        Handler<Socket> handler = new ExecutorServiceHandler<>(
                new PrintingHandler<>(
                        new TransmorgifyHandler()
                ), Executors.newFixedThreadPool(10)
        );

        while (true) {
            Socket socket = serverSocket.accept();
            handler.handle(socket);
        }
    }

}
