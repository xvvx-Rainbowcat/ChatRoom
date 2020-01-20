package com.example.consoletest;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private String name;

    public static void main(String[] args) throws IOException {
        Socket client = new Socket("192.168.1.23", 7777);
        System.out.println("请输入用户名:");
        String name = new Scanner(System.in).nextLine();
        if (name.equals("")) {
            return;
        }
        new Thread(new Sender(client,name)).start();
        new Thread(new Receiver(client)).start();
    }
}