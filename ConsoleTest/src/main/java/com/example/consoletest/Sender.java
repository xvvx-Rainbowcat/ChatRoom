package com.example.consoletest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Sender implements Runnable{

    private BufferedReader bufferedReader;

    private DataOutputStream dos;

    private boolean isRunning = true;

    private String name;
    public Sender(Socket client,String name) {
        try {
        	bufferedReader = new BufferedReader(new InputStreamReader(System.in, "utf-8"));
            dos = new DataOutputStream(client.getOutputStream());
            this.name = name;
            send(this.name);
        } catch (IOException e) {
            e.printStackTrace();
            isRunning = false;
        }
    }

    private String getMsgFromConsole() {
        try {
            return bufferedReader.readLine();
        } catch (IOException e) {
            return "";
        }
    }

    private void send(String msg) {
        if (msg != null && !msg.equals("")) {
            try {
                dos.writeUTF(msg);
                dos.flush();
            } catch (IOException e) {
                isRunning = false;
                FLowClose.close(dos,bufferedReader);
            }
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            send(getMsgFromConsole());
        }
    }
}