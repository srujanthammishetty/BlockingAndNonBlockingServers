package com.test.io.reactor;

import com.test.io.util.Util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;


/*
*       http://jeewanthad.blogspot.com/2013/02/reactor-pattern-explained-part-1.html
*       http://gee.cs.oswego.edu/dl/cpjslides/nio.pdf
*
*
* 1) selector.selectedKeys returns same read event if data is not read from channel.
* 2) .wakeUp -> awakes any thread waiting on selector.select()
* 3) .interestOps() -> SelectionKey.OP_WRITE doesn't require any external event from client.
*       On setting it to OP_WRITE, it registers new event to registor inside selector.
*
*       OP_READ opt gets triggered only if channel receives any data from user.
*
* 4) reading of data should always be done inside the reactor thread. And then processing of data
*     can be handled to worker pool.
*
* 5) Handling socketTimeOut and ConnectionTimeout in Selector.
* 6)
* */

public class Reactor implements Runnable {
    Selector selector;
    Reactor(String host, int port) {
        try {
            selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(host, port));
            serverSocketChannel.configureBlocking(false);
            SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            selectionKey.attach(new AcceptHandler());
        } catch (IOException e) {
            throw new RuntimeException(" Failed to initiate server", e);
        }
    }

    @Override
    public void run() {

        while (!Thread.interrupted()) {
            try {
                int eventsSize = selector.select();
                if (eventsSize > 0) {
                    Set<SelectionKey> selectionKeySet = selector.selectedKeys();
                    Iterator<SelectionKey> selectionKeyIterator = selectionKeySet.iterator();
                    while (selectionKeyIterator.hasNext()) {
                        SelectionKey selectionKey = selectionKeyIterator.next();
                        Handler handler = (Handler) selectionKey.attachment();
                        if (handler != null)
                            handler.handle(selectionKey);
                    }
                    selectionKeySet.clear();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    class AcceptHandler implements Handler<SelectionKey> {

        @Override
        public void handle(SelectionKey selectionKey) {

            try {
                SocketChannel socketChannel = ((ServerSocketChannel) selectionKey.channel()).accept();
                System.out.println("Connected to Socket :" + socketChannel);
                new RequestHandler(socketChannel, selectionKey);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    class RequestHandler implements Handler<SelectionKey> {
        SocketChannel socketChannel;
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        State state;

        public RequestHandler(SocketChannel socketChannel, SelectionKey selectionKey) {
            this.socketChannel = socketChannel;
            Selector selector = selectionKey.selector();
            try {
                socketChannel.configureBlocking(false);
                SelectionKey requestHandlerSelectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
                state = State.READING;
                requestHandlerSelectionKey.attach(this);
                selector.wakeup();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void handle(SelectionKey selectionKey) throws IOException {
            socketChannel = (SocketChannel) selectionKey.channel();

            if (state.equals(State.READING)) {
                socketChannel.read(byteBuffer);
                state = State.WRITING;
                selectionKey.interestOps(SelectionKey.OP_WRITE);
//                selectionKey.selector().wakeup();
            } else {
                Util.transmorgify(byteBuffer);
                socketChannel.write(byteBuffer);
                byteBuffer.compact();
                state = State.READING;
                selectionKey.interestOps(SelectionKey.OP_READ);
            }
        }
    }

    enum State {
        READING,
        WRITING;
    }

}
