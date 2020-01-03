package com.test.io.server.nio.selector;


import com.test.io.handler.AcceptHandler;
import com.test.io.handler.Handler;
import com.test.io.handler.PoolReadHandler;
import com.test.io.handler.WriteHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SelectorWithWorkPoolNonBlockingServer {

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind((new InetSocketAddress(8090)));
        serverSocketChannel.configureBlocking(false);
        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        ExecutorService pool = Executors.newFixedThreadPool(10);

        Map<SocketChannel, Queue<ByteBuffer>> pendingData = new ConcurrentHashMap<>();
        Queue<Runnable> selectActions = new ConcurrentLinkedQueue<>();

        Handler<SelectionKey> acceptHandler = new AcceptHandler(pendingData);
        Handler<SelectionKey> readHandler = new PoolReadHandler(pool,selectActions,pendingData);
        Handler<SelectionKey> writeHandler = new WriteHandler(pendingData);
        while (true) {
            selector.select(); // blocks untill some event happens ( read/connect/write);
            processSelectorActions(selectActions);

            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            for (Iterator<SelectionKey> iterator = selectionKeys.iterator(); iterator.hasNext(); ) {
                SelectionKey selectionKey = iterator.next();
                iterator.remove();
                if (selectionKey.isValid()) {
                    if (selectionKey.isAcceptable()) {
                        acceptHandler.handle(selectionKey);
                    } else if (selectionKey.isReadable()) {
                        readHandler.handle(selectionKey);
                    } else if (selectionKey.isWritable()) {
                        writeHandler.handle(selectionKey);
                    }
                }
            }
        }
    }

    private static void processSelectorActions(Queue<Runnable> selectorActions) {
        Runnable action;
        while ((action = selectorActions.poll()) != null){
            action.run();
        }
    }
}
