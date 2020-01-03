package com.test.io.handler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AcceptHandler implements Handler<SelectionKey> {

    Map<SocketChannel, Queue<ByteBuffer>> pendingData;

    public AcceptHandler(Map<SocketChannel, Queue<ByteBuffer>> pendingData) {
        this.pendingData = pendingData;
    }

    @Override
    public void handle(SelectionKey selectionKey) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
        SocketChannel socketChannel = serverSocketChannel.accept();
        System.out.println("Connected to Socket: " + socketChannel);
        pendingData.put(socketChannel, new ConcurrentLinkedQueue<>());
        socketChannel.configureBlocking(false);
        socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ);
    }
}
