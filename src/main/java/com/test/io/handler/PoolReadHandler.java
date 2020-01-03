package com.test.io.handler;

import com.test.io.util.Util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;

public class PoolReadHandler implements Handler<SelectionKey> {


    private ExecutorService pool;
    private Queue<Runnable> selectorActions;
    private Map<SocketChannel, Queue<ByteBuffer>> pendingData;


    public PoolReadHandler(ExecutorService pool, Queue<Runnable> selectorActions, Map<SocketChannel, Queue<ByteBuffer>> pendingData) {
        this.pool = pool;
        this.selectorActions = selectorActions;
        this.pendingData = pendingData;
    }

    @Override
    public void handle(SelectionKey selectionKey) throws IOException {

        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(80);
        int read = socketChannel.read(byteBuffer);
        if (read == -1) {
            pendingData.remove(socketChannel);
            socketChannel.close();
            System.out.println("Disconnected from Socket " + socketChannel + " (in poolRead()) ");
            return;
        }

        if (read > 0) {
            pool.submit(() -> {
                Util.transmorgify(byteBuffer);
                pendingData.get(socketChannel).add(byteBuffer);
                selectorActions.add(() -> {
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                });
                selectionKey.selector().wakeup();
            });
        }

    }
}
