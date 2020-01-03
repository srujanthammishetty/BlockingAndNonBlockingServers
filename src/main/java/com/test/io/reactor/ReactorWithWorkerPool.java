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
import java.util.concurrent.ExecutorService;

public class ReactorWithWorkerPool implements Runnable {

    Selector selector;
    ExecutorService pool;

    ReactorWithWorkerPool(String host, int port, ExecutorService pool) {
        try {
            this.pool = pool;
            selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(host, port));
            serverSocketChannel.configureBlocking(false);
            SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            selectionKey.attach(new AcceptHandler(selectionKey));
        } catch (IOException e) {
            throw new RuntimeException(" Failed to initiate server", e);
        }
    }

    @Override
    public void run() {

        System.out.println("ReactorThread: " + Thread.currentThread().getName());
        while (!Thread.interrupted()) {
            try {
                int eventsSize = selector.select();
                if (eventsSize > 0) {
                    Set<SelectionKey> selectionKeySet = selector.selectedKeys();
                    Iterator<SelectionKey> selectionKeyIterator = selectionKeySet.iterator();
                    while (selectionKeyIterator.hasNext()) {
                        SelectionKey selectionKey = selectionKeyIterator.next();
                        selectionKeyIterator.remove();
                        Runnable runnableHandler = (Runnable) selectionKey.attachment();
                        if (runnableHandler != null)
                            runnableHandler.run();
 /*                           if (runnableHandler instanceof AcceptHandler) {
                                runnableHandler.run();
                            } else {
                                pool.execute(runnableHandler);
                            }*/
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    class AcceptHandler implements Runnable {
        SelectionKey selectionKey;

        public AcceptHandler(SelectionKey selectionKey) {
            this.selectionKey = selectionKey;
        }

        @Override
        public void run() {
            System.out.println("AcceptorHandler Running In Thread: " + Thread.currentThread().getName());
            try {
                SocketChannel socketChannel = ((ServerSocketChannel) selectionKey.channel()).accept();
                System.out.println("Connected to Socket :" + socketChannel);
                if (socketChannel != null) {
                    socketChannel.configureBlocking(false);
                    new RequestHandler(socketChannel, selectionKey);

                    /*SelectionKey selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
                    RequestContext requestContext = new RequestContext(ByteBuffer.allocate(1024), selectionKey);
                    selectionKey.attach(requestContext);
                    new ReaderHandler(requestContext);
                    new WriterHandler(requestContext);
                    */
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    class RequestContext {
        private ByteBuffer byteBuffer;
        private SelectionKey selectionKey;

        public RequestContext(ByteBuffer byteBuffer, SelectionKey selectionKey) {
            this.byteBuffer = byteBuffer;
            this.selectionKey = selectionKey;
        }

        public ByteBuffer getByteBuffer() {
            return byteBuffer;
        }

        public SelectionKey getSelectionKey() {
            return selectionKey;
        }
    }

    class ReaderHandler implements Runnable {

        RequestContext requestContext;

        public ReaderHandler(RequestContext requestContext) {
            this.requestContext = requestContext;
        }

        @Override
        public void run() {
            try {
                SelectionKey selectionKey = requestContext.getSelectionKey();
                SocketChannel socketChannel = (SocketChannel) requestContext.getSelectionKey().channel();
                System.out.println("RequestHandler Running In Thread: " + Thread.currentThread().getName());
                System.out.println("Reading data from socketChannel : ( " + Thread.currentThread().getName() + " )");
                socketChannel.read(requestContext.getByteBuffer());
                selectionKey.interestOps(SelectionKey.OP_WRITE);
                selectionKey.selector().wakeup();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class WriterHandler implements Runnable {
        RequestContext requestContext;

        public WriterHandler(RequestContext requestContext) {
            this.requestContext = requestContext;
        }

        @Override
        public void run() {
            try {
                SelectionKey selectionKey = requestContext.getSelectionKey();
                ByteBuffer byteBuffer = requestContext.getByteBuffer();
                SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                Util.transmorgify(byteBuffer);
                socketChannel.write(byteBuffer);
                byteBuffer.compact();
                selectionKey.interestOps(SelectionKey.OP_READ);
                selectionKey.selector().wakeup();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class RequestHandler implements Runnable {

        SocketChannel socketChannel;
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        State state;
        SelectionKey requestHandlerSelectionKey;

        public RequestHandler(SocketChannel socketChannel, SelectionKey selectionKey) {
            this.socketChannel = socketChannel;
            Selector selector = selectionKey.selector();
            try {
                socketChannel.configureBlocking(false);
                requestHandlerSelectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
                state = State.READING;
                requestHandlerSelectionKey.attach(this);
                //            selector.wakeup();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            socketChannel = (SocketChannel) requestHandlerSelectionKey.channel();
            try {

                if (state.equals(State.PROCESSING)) {
                    System.out.println("Inside Processing State");
                } else if (state.equals(State.READING)) {
                    System.out.println("RequestHandler Running In Thread: " + Thread.currentThread().getName());
                   // state = State.PROCESSING;
                    System.out.println("Reading data from socketChannel : ( " + Thread.currentThread().getName() + " )");
             //       socketChannel.read(byteBuffer);
           //         requestHandlerSelectionKey.interestOps(SelectionKey.OP_READ);
                    System.out.println("Setting state to Writing : ( " + Thread.currentThread().getName() + " ) ");
              //      state = State.WRITING;
                    requestHandlerSelectionKey.selector().wakeup();
                } else if (state.equals(State.WRITING)) {
                    System.out.println("RequestHandler Running In Thread: " + Thread.currentThread().getName());
                    state = State.PROCESSING;
                    Util.transmorgify(byteBuffer);
                    socketChannel.write(byteBuffer);
                    byteBuffer.compact();
                    state = State.READING;
                    requestHandlerSelectionKey.interestOps(SelectionKey.OP_READ);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    enum State {
        READING,
        WRITING,
        PROCESSING;
    }
}
