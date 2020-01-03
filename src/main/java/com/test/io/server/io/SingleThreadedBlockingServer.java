package com.test.io.server.io;


import com.test.io.handler.Handler;
import com.test.io.handler.PrintingHandler;
import com.test.io.handler.TransmorgifyHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * SingleThread Blocking Server Implementation.
 * This implementation cannot serve more than one request at once.
 * Doesn't support concurrent request handling.
 */
public class SingleThreadedBlockingServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8090);
        Handler<Socket> handler = new PrintingHandler<>(new TransmorgifyHandler());
        while (true) {
            handler.handle(serverSocket.accept());
        }
    }

}
