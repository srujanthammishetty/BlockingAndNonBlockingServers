package com.test.io.handler;

import com.test.io.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TransmorgifyHandler implements Handler<Socket> {

    @Override
    public void handle(Socket socket) throws IOException {

        try (Socket temp = socket;
             InputStream inputStream = socket.getInputStream();
             OutputStream outputStream = socket.getOutputStream();
        ) {
            int data;
            while ((data = inputStream.read()) != -1) {
                outputStream.write(Util.transmorgify(data));
            }
        }

    }
}
