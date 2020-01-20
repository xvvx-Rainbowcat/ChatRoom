package com.example.pc_96.chatroom2;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by pc-96 on 2018/8/10.
 * 用于与服务器交互登陆信息
 * 客户端发送用户名和信息,服务器将在登陆成功后向客户端返还用户信息(昵称,头像)
 */

public class ProfileCheck implements Runnable {


    public static final int ACT_LOGIN = 0;
    public static final int ACT_REGISTER = 1;
    public static final int PROPERTY_GET = 2;
    public static final int RES_PASS = 3;
    public static final int RES_DENY = 4;
    public static final int ERR_RECEIVE = 5;
    public static final int CONNECT_READY = 6;
    public static final int SAME_ID = 19;
    public static final int SAME_NAME = 20;

    public Handler sendHandler;
    private Handler receiveHandler;
    private ObjectOutputStream oo;
    private ObjectInputStream oi;
    private boolean isRunning = true;
    private Socket client;
    private Context mContext;


    public ProfileCheck (Handler receiveHandler, Context mContext, Property property) {
        this.receiveHandler = receiveHandler;
        this.mContext = mContext;
        if (property != null) {     //如果之前有完成过注册,那网络资源便已获取过
            client = property.getSocket();
            oo = property.getOo();
            oi = property.getOi();
        }
    }

    @Override
    public void run () {
        try {
            if (client == null) {   //如果之前没有与服务器连接过
                Socket client = new Socket();
                InetSocketAddress address = new InetSocketAddress("192.168.1.100", 7777);
                client.connect(address, 3000);  //设定连接3s超时
                oo = new ObjectOutputStream(client.getOutputStream());
                oi = new ObjectInputStream(client.getInputStream());
                Property property = new Property(client, oi, oo);
                sendMessage(PROPERTY_GET, property);
            }
            isRunning = true;
            Looper.prepare();   //为子线程准备Looper
            sendHandler = new Handler(Looper.myLooper()) {
                @Override
                public void handleMessage (Message message) {
                    if (message.what == ACT_LOGIN) {  //登陆发送
                        send(message.obj);
                    } else if (message.what == ACT_REGISTER) {    //注册发送
                        send(message.obj);
                    }
                }
            };
            new Thread(new Runnable() {
                @Override
                public void run () {    //开启新线程并且用于接收账户信息
                    while (isRunning) {
                        Profile profile = receive();
                        if (profile != null) {
                            switch (profile.getStatus()) {
                                case RES_PASS:
                                    sendMessage(RES_PASS, profile);
                                    isRunning = false;
                                    break;
                                case RES_DENY:
                                    sendMessage(RES_DENY, null);
                                    break;
                                case SAME_ID:
                                    sendMessage(SAME_ID, null);
                                    break;
                                case SAME_NAME:
                                    sendMessage(SAME_NAME, null);
                                    break;
                            }
                        }
                    }
                }
            }).start();
            sendMessage(CONNECT_READY, null);
            Looper.loop();
        } catch (Exception e) {
            e.printStackTrace();
            client = null;
            isRunning = false;
            close(oi, oo);
            sendMessage(ERR_RECEIVE, null);
        }
    }

    private void sendMessage (int what, Object obj) {
        Message message = new Message();
        message.what = what;
        message.obj = obj;
        receiveHandler.sendMessage(message);
    }

    private Profile receive () {
        Object temp = null;
        Profile profile = null;
        try {
            profile = null;
            temp = oi.readObject();
            if (temp instanceof Profile) {
                profile = (Profile) temp;
            }
        } catch (EOFException e) {
            e.printStackTrace();
            profile = (Profile) temp;
            return profile;
        } catch (Exception e) {
            e.printStackTrace();
            client = null;
            isRunning = false;
            close(oi);
            sendMessage(ERR_RECEIVE, null);
        }
        return profile;
    }

    private void send (Object object) {
        if (object != null) {
            try {
                oo.reset();
                oo.writeObject(object);
                oo.flush();
            } catch (IOException e) {
                e.printStackTrace();
                client = null;
                isRunning = false;
                close(oo);
                sendMessage(ERR_RECEIVE, null);
            }
        }
    }

    private static void close (Closeable... io) {
        for (Closeable temp : io) {
            try {
                if (temp != null) {
                    temp.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}