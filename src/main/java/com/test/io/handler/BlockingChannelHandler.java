package com.test.io.handler;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class BlockingChannelHandler extends DecoratorHandler<SocketChannel> {

    public BlockingChannelHandler(Handler<SocketChannel> other) {
        super(other);
    }

    @Override
    public void handle(SocketChannel socketChannel) throws IOException {
        while (socketChannel.isConnected()){
                super.handle(socketChannel);
        }
    }
}
