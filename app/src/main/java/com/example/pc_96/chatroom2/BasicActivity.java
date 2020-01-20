package com.example.pc_96.chatroom2;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by pc-96 on 2018/8/10.
 */

public class BasicActivity extends AppCompatActivity {
    public Context mContext;
    public static Property property;    //property用于缓存连接信息,例如Socket和ObjectInputStream和ObjectOutputStream
    public static Profile profile;  //用于记录登陆者信息
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        ActivityList.addActivity(this);
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        ActivityList.removeActivity(this);
    }
}

class ActivityList {
    static ArrayList<Activity> activities = new ArrayList<>();

    static public void addActivity (Activity activity) {
        activities.add(activity);
    }

    static public void removeActivity (Activity activity) {
        activities.remove(activity);
    }

    static public void removeAll () {
        for (Activity activity : activities) {
            if (!activity.isFinishing()) {
                activity.finish();
            }
        }
    }
}

