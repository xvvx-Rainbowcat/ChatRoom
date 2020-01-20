package com.example.pc_96.chatroom2;

import java.io.Serializable;

/**
 * Created by pc-96 on 2018/8/10.
 * 向服务器发送或接收的信息
 */
public class Msg implements Serializable {
    private static final long serialVersionUID = -9175365283524037366L;
    private String from;
    private String content;
    private byte[] image;
    private int status;

    public Msg(String from, String content, byte[] image, int status) {
        this.from = from;
        this.content = content;
        this.image = image;
        this.status = status;
    }

    public Msg(String from, byte[] image, int status) {
        this.from = from;
        this.image = image;
        this.status = status;
    }

    public Msg(String from, String content, int status) {
        this.from = from;
        this.content = content;
        this.status = status;
    }

    public Msg(String from, String content) {
        this.from = from;
        this.content = content;
    }

    public Msg(int status) {
        this.status = status;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
