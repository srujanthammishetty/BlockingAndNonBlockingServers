package com.test.io.server.nio;


import com.test.io.handler.Handler;
import com.test.io.handler.TransmorgifyChannelHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;

public class SingleThreadedPollingNonBlockingServer {

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind((new InetSocketAddress(8090)));

        //by default .accept() is a blocking method.
        serverSocketChannel.configureBlocking(false);
        Collection<SocketChannel> sockets = new ArrayList<>();
        Handler<SocketChannel> handler = new TransmorgifyChannelHandler();

        while (true) {
            // almost always null and 2non blocking, if serverSocketChannel.configureBlocking = false
            SocketChannel socketChannel = serverSocketChannel.accept();
            if (socketChannel != null) {
                sockets.add(socketChannel);
                System.out.println("Connected to" + socketChannel);
                socketChannel.configureBlocking(false);
            }

            for (SocketChannel socket : sockets) {
                if (socket.isConnected()) {
                    handler.handle(socket);
                }
            }
            sockets.removeIf(socketChannel1 -> !socketChannel.isConnected());
        }
    }
}
