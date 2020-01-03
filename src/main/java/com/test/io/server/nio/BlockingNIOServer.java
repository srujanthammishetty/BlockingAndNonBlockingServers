package com.test.io.server.nio;


import com.test.io.handler.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;

public class BlockingNIOServer {

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind((new InetSocketAddress(8090)));
        Handler<SocketChannel> handler = new ExecutorServiceHandler(
                new PrintingHandler<>(
                        new BlockingChannelHandler(
                                new TransmorgifyChannelHandler()
                        )
                ), Executors.newFixedThreadPool(10)
        );
        while (true) {
            SocketChannel socketChannel = serverSocketChannel.accept();
            handler.handle(socketChannel);
        }
    }

}
