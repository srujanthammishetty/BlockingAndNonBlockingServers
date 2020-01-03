package com.test.io.handler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

public class WriteHandler implements Handler<SelectionKey> {

    Map<SocketChannel, Queue<ByteBuffer>> pendingData;


    public WriteHandler(Map<SocketChannel, Queue<ByteBuffer>> pendingData) {
        this.pendingData = pendingData;
    }

    @Override
    public void handle(SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        Queue<ByteBuffer> queue = pendingData.getOrDefault(socketChannel, new ArrayDeque<>());
        while (!queue.isEmpty()) {
            ByteBuffer byteBuffer = queue.peek();
            int written = socketChannel.write(byteBuffer);
            if (written == -1) {
                socketChannel.close();
                System.out.println("Disconnected from Socket " + socketChannel + " (in write()) ");
                pendingData.remove(socketChannel);
                return;
            }
            if (byteBuffer.hasRemaining()) return;
            queue.remove();
        }
        selectionKey.interestOps(SelectionKey.OP_READ);
    }
}
