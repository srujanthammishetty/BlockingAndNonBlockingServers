package com.test.io.server.nio.selector;


import com.test.io.handler.AcceptHandler;
import com.test.io.handler.Handler;
import com.test.io.handler.ReadHandler;
import com.test.io.handler.WriteHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class SingleThreadedSelectorNonBlockingServer {

    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind((new InetSocketAddress(8080)));
        serverSocketChannel.configureBlocking(false);
        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        Map<SocketChannel, Queue<ByteBuffer>> pendingData = new HashMap<>();
        Handler<SelectionKey> acceptHandler = new AcceptHandler(pendingData);
        Handler<SelectionKey> readHandler = new ReadHandler(pendingData);
        Handler<SelectionKey> writeHandler = new WriteHandler(pendingData);
        while (true) {
            selector.select(); // blocks untill some evetn happens ( read/connect/write);
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
}
