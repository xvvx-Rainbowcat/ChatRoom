package com.example.consoletest;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class Receiver implements Runnable {
    private DataInputStream dis;
    private boolean isRunning = true;

    public Receiver(Socket slient){
        try {
            dis = new DataInputStream(slient.getInputStream());
        } catch (IOException e) {
            isRunning = false;
            FLowClose.close(dis);
        }
    }

    public String receive() {
        String msg = "";
        try {
            msg = dis.readUTF();
        } catch (IOException e) {
            isRunning = false;
            FLowClose.close(dis);
        }
        return msg;
    }

    @Override
    public void run() {
        while (isRunning) {
            System.out.println(receive());
        }
    }
}