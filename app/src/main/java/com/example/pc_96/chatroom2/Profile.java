package com.example.pc_96.chatroom2;

import java.io.Serializable;

public class Profile implements Serializable {
    private static final long serialVersionUID = -9175365283524037300L;
    private String id = null;
    private String password = null;
    private String name = null;
    private String brief = null;
    private byte[] image = null;
    private int status;

    public Profile(String id, String password, String name, String brief, byte[] image) {
        this.id = id;
        this.password = password;
        this.name = name;
        this.brief = brief;
        this.image = image;
        this.status = status;
    }

    public Profile (String id, String password, String name, byte[] image,int status) {
        this.id = id;
        this.password = password;
        this.name = name;
        this.image = image;
        this.status = status;
    }

    public Profile (String id, String password, int status) {
        this.id = id;
        this.password = password;
    }

    public Profile (int status) {
        this.status = status;
    }

    public int getStatus () {
        return status;
    }

    public void setStatus (int status) {
        this.status = status;
    }

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public String getBrief () {
        return brief;
    }

    public void setBrief (String brief) {
        this.brief = brief;
    }

    public String getId () {
        return id;
    }

    public void setId (String id) {
        this.id = id;
    }

    public String getPassword () {
        return password;
    }

    public void setPassword (String password) {
        this.password = password;
    }

    public byte[] getImage () {
        return image;
    }

    public void setImage (byte[] image) {
        this.image = image;
    }

}
