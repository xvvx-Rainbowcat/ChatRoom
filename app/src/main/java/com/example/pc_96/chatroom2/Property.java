package com.example.pc_96.chatroom2;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by pc-96 on 2018/8/10.
 * 储存网络资源
 */

public class Property {
    private Socket socket;
    private ObjectInputStream oi;
    private ObjectOutputStream oo;

    public Property (Socket socket, ObjectInputStream oi, ObjectOutputStream oo) {
        this.socket = socket;
        this.oo = oo;
        this.oi = oi;
    }

    public Socket getSocket () {
        return socket;
    }

    public void setSocket (Socket socket) {
        this.socket = socket;
    }

    public ObjectInputStream getOi () {
        return oi;
    }

    public void setOi (ObjectInputStream oi) {
        this.oi = oi;
    }

    public ObjectOutputStream getOo () {
        return oo;
    }

    public void setOo (ObjectOutputStream oo) {
        this.oo = oo;
    }
}
