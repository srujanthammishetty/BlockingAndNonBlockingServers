package com.test.io.handler;

import com.test.io.util.Util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class TransmorgifyChannelHandler implements Handler<SocketChannel> {

    ByteBuffer byteBuffer;

    public TransmorgifyChannelHandler() {
        byteBuffer = ByteBuffer.allocate(80);
    }

    @Override
    public void handle(SocketChannel socketChannel) throws IOException {

        // flip the buffer before reading [ sets currentPosition to 0 and limit is set to position ]
        // compact the buffer after writing [ sets currentPosition to number of bytes present in buffer and limit to capacity]
        int read = socketChannel.read(byteBuffer);
        if (read == -1) {
            socketChannel.close();
            return;
        }
        if (read > 0) {
            Util.transmorgify(byteBuffer);
            while (byteBuffer.hasRemaining()) {
                socketChannel.write(byteBuffer);
            }
            byteBuffer.compact();
            //limit=capacity, pos= number of available bytes
        }
    }
}
