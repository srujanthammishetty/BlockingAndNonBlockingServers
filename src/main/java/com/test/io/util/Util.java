package com.test.io.util;

import java.nio.ByteBuffer;

public class Util {

    public static int transmorgify(int data) {
        return Character.isLetter(data) ? data ^ ' ' : data;
    }

    public static void transmorgify(ByteBuffer byteBuffer) {
        // pos , limit ,capacity

        System.out.println("Transmorgifaction done by "+ Thread.currentThread());
        //  limit = pos, pos=0, capacity
        byteBuffer.flip();
        for (int i = 0; i < byteBuffer.limit(); i++) {
            byteBuffer.put(i, (byte) transmorgify(byteBuffer.get(i)));
        }
    }
}
