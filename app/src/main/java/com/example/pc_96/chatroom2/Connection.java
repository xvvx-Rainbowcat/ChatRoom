package com.example.pc_96.chatroom2;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.LogRecord;

/**
 * Created by pc-96 on 2018/8/10.
 * Connection类主要用于管理用户对话时的各项网络操作,如接收信息与发送信息
 */

public class Connection implements Runnable {

    public static final int PRIVATE_FOUND = 8;
    public static final int PRIVATE_MISS = 9;
    public static final int PRIVATE_FORMATE = 10;
    public static final int TYPE_RECEIVED = 11;
    public static final int TYPE_SEND = 12;
    public static final int PHOTO_UPDATE = 18;
    public static final int IMAGE_SEND = 21;
    public static final int IMAGE_RECEIVE = 22;

    public Handler sendHandler;

    public Handler receiveHandler;

    private ObjectOutputStream oo;

    private ObjectInputStream oi;

    private boolean isRunning = true;

    private File filePath;

    private Context mContext;

    private Socket client;

    public Connection (Handler receiveHandler, Context mContext, Property property, File filePath) {
        this.filePath = filePath;
        this.receiveHandler = receiveHandler;   //在此获取ChatActivity中的Handler对象以传输数据
        this.mContext = mContext;
        this.client = property.getSocket(); //在开始聊天之前登陆必定完成,所以Socket和OO,Oi流必定已经获取完成
        this.oo = property.getOo();
        this.oi = property.getOi();
    }

    @Override
    public void run () {

        new Thread(new Runnable() {
            @Override
            public void run () {        //开启一个新线程用于获取信息
                while (isRunning) {
                    Msg msg = receive();
                    if (msg != null) {
                        switch (msg.getStatus()) {
                            case PRIVATE_FORMATE:
                            case PRIVATE_MISS:
                                sendMessage(msg.getStatus(), null);    //用户名不存在
                                break;
                            case PRIVATE_FOUND:
                            case TYPE_RECEIVED:
                            case IMAGE_RECEIVE:
                                sendMessage(msg.getStatus(), msg);          //接收到信息
                                break;
                            case PHOTO_UPDATE:
                                photoUpdate(msg);       //接收到用户头像
                                break;
                        }
                    } else {
                        isRunning = false;
                        close(oi, oo);
                        sendMessage(ChatActivity.ERR_RECEIVE, null);
                    }
                }
            }
        }).start();

        Looper.prepare();   //子线程的Looper必须手动开启,而主线程中的Looper会自动开启
        sendHandler = new Handler(Looper.myLooper()) {  //创建handler对象来与ChatActivity交互
            @Override
            public void handleMessage (Message message) {
                switch (message.what) {
                    case IMAGE_SEND:
                    case TYPE_SEND:
                        Msg msg = (Msg) message.obj;
                        send(msg);
                        break;
                }
            }
        };
        Looper.loop();  //Looper开始工作
    }

    private void photoUpdate (Msg msg) {    //接收到用户头像
        if (msg.getImage() != null) {
            File file = new File(filePath, msg.getFrom() + ".png");     //创建一个临时文件用于保存头像
            try {
                if (file.exists()) {
                    file.delete();
                    file.createNewFile();
                } else {
                    file.createNewFile();
                }
                FileOutputStream fo = new FileOutputStream(file);
                fo.write(msg.getImage());
                fo.flush();
                fo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Msg receive () {        //接收到信息
        Object temp;
        Msg msg = null;
        try {
            temp = oi.readObject();
            if (temp != null && temp instanceof Msg) {
                msg = (Msg) temp;
            }
        } catch (Exception e) {
            e.printStackTrace();
            isRunning = false;
            close(oi);
            sendMessage(ChatActivity.ERR_RECEIVE, null);
        }
        return msg;
    }

    private void send (Object object) {     //发送信息
        try {
            if (object != null) {
                oo.reset();
                oo.writeObject(object);
                oo.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            isRunning = false;
            close(oo);
            sendMessage(ChatActivity.ERR_RECEIVE, null);
        }
    }

    private void sendMessage (int what, Object obj) {       //向ChatActivity交互
        Message message = new Message();
        message.what = what;
        message.obj = obj;
        receiveHandler.sendMessage(message);
    }

    private static void close (Closeable... io) {       //出现错误关闭所有IO流
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