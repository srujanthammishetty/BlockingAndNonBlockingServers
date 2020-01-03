package com.test.io;

import java.io.IOException;
import java.net.Socket;

public class NastyChump {
    public static void main(String[] args) throws InterruptedException{
        Socket[] sockets = new Socket[10000];

        for (int i=0;i<sockets.length;i++){
            try {
                sockets[i] = new Socket("localhost",8090);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Thread.sleep(100000);
    }
}
