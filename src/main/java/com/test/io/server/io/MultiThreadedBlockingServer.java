package com.test.io.server.io;


import com.test.io.handler.Handler;
import com.test.io.handler.PrintingHandler;
import com.test.io.handler.ThreadedHandler;
import com.test.io.handler.TransmorgifyHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Blocking Server Implementation which spawns new thread for each connection.
 * <p>
 * But Spawning new thread for each connection isn't an effective solution for implementing a server
 * which needs to handle 10k concurrent requests.
 * <p>
 * This implementation may eventually crash down with {@link java.lang.OutOfMemoryError}
 */

public class MultiThreadedBlockingServer {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8090);

        Handler<Socket> handler = new ThreadedHandler<>(
                new PrintingHandler<>(
                        new TransmorgifyHandler()
                )
        );

        while (true) {
            Socket socket = serverSocket.accept();
            handler.handle(socket);
        }
    }

}
