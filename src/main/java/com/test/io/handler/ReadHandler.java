package com.test.io.handler;

import com.test.io.util.Util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Queue;

public class ReadHandler implements Handler<SelectionKey> {

    Map<SocketChannel, Queue<ByteBuffer>> pendingData;

    public ReadHandler(Map<SocketChannel, Queue<ByteBuffer>> pendingData) {
        this.pendingData = pendingData;
    }

    @Override
    public void handle(SelectionKey selectionKey) throws IOException {

        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(800000);

        int read = socketChannel.read(byteBuffer);
        if (read == -1) {
            pendingData.remove(socketChannel);
            socketChannel.close();
            System.out.println("Disconnected from Socket " + socketChannel + " (in read()) ");
            return;
        }

        if (read > 0) {
            String inputData = new String(byteBuffer.array());
            System.out.println("Data from server: " + inputData);
            byteBuffer.clear();
            byteBuffer.put("ServerResponse".getBytes());
            Util.transmorgify(byteBuffer);
            System.out.println("Data from server: " + new String(byteBuffer.array()));
            pendingData.get(socketChannel).add(byteBuffer);
            selectionKey.interestOps(SelectionKey.OP_WRITE);
        }
    }
}
