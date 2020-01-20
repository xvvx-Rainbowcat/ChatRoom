package com.example.pc_96.chatroom2;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc-96 on 2018/8/13.
 * 聊天室模块,将Connection获取的信息在此展示,并且在此获取信息交由Connection发送
 */
public class ChatActivity extends BasicActivity {
    public static final int ERR_RECEIVE = 5;
    public static final int PRIVATE_FOUND = 8;
    public static final int PRIVATE_MISS = 9;
    public static final int PRIVATE_FORMATE = 10;
    public static final int TYPE_RECEIVED = 11;     //接收到信息
    public static final int TYPE_SEND = 12;         //发送信息
    public static final int FROM_ALBUM = 13;
    public static final int IMAGE_SEND = 21;
    public static final int IMAGE_RECEIVE = 22;

    private RecyclerView chat_recycler_msg;
    private Button chat_btn_msg_send;
    private ImageButton chat_btn_plus;
    private ImageButton chat_btn_photo;
    private EditText chat_edit_msg;
    private ArrayList<Msg> msgArrayList;
    private MsgAdapter msgAdapter;
    private Connection connection;
    private Handler receiveHandler;
    private ArrayList<ImageButton> buttonList;
    private int flag = 1;

    @Override
    protected void onCreate (final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        msgArrayList = new ArrayList<>();
        chat_recycler_msg = findViewById(R.id.chat_recycler_msg);
        chat_btn_msg_send = findViewById(R.id.chat_btn_msg_send);
        chat_edit_msg = findViewById(R.id.chat_edit_msg);
        chat_btn_plus = findViewById(R.id.chat_btn_plus);
        chat_btn_photo = findViewById(R.id.chat_btn_photo);

        receiveHandler = new Handler(Looper.myLooper()) {   //创建Handler对象来在不同的线程之间通信
            @Override
            public void handleMessage (Message message) {
                Toast toast = Toast.makeText(mContext, null, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 50);
                switch (message.what) {
                    case PRIVATE_FOUND:     //接收到私信
                    case TYPE_RECEIVED:     //接收到广播信息
                    case IMAGE_RECEIVE:     //接收到图片
                        Msg msg = (Msg) message.obj;
                        if (msg.getFrom().equalsIgnoreCase("system")) { //如果是系统发来的信息就弹一个Toast
                            toast.setText(msg.getContent());
                            toast.show();
                        } else {    //否则将信息加入ArrayList,并且交由MsgAdapter去读取显示
                            msgArrayList.add(msg);
                            msgAdapter.notifyDataSetChanged();
                            chat_recycler_msg.scrollToPosition(msgArrayList.size() - 1);
                        }
                        break;
                    case ERR_RECEIVE:
                        toast.setText("连接出现错误,请检查网络或联系管理员");
                        toast.show();
                        break;
                    case PRIVATE_MISS:
                        toast.setText("用户不存在");
                        toast.show();
                        break;
                    case PRIVATE_FORMATE:
                        toast.setText("格式错误--@用户名:私聊内容");
                        toast.show();
                        break;
                }
            }
        };

        chat_btn_msg_send.setOnClickListener(new View.OnClickListener() {   //发送按钮
            @Override
            public void onClick (View v) {
                String temp = chat_edit_msg.getText().toString();   //从EidtText获取字符串
                if (temp != null && !temp.equals("")) {
                    Msg msg = new Msg(profile.getName(), chat_edit_msg.getText().toString(),TYPE_SEND);     //创建需要发送的信息

                    chat_edit_msg.setText("");
                    msgArrayList.add(msg);
                    msgAdapter.notifyDataSetChanged();
                    chat_recycler_msg.scrollToPosition(msgArrayList.size() - 1);

                    Message message = new Message();
                    message.obj = msg;
                    message.what = ChatActivity.TYPE_SEND;
                    connection.sendHandler.sendMessage(message);    //向连接模块发送信息,通知其接收一条需要发送的信息
                }
            }
        });

        chat_btn_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                buttonAnimation(buttonList, 1);     //单击加号弹出按钮
                if (flag == 1) {
                    flag = -1;
                } else {
                    flag = 1;
                }
            }
        });

        chat_btn_photo.setOnClickListener(new View.OnClickListener() {      //从图库选择一张图片发送
            @Override
            public void onClick (View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, FROM_ALBUM);
                buttonAnimation(buttonList, 1);
                if (flag == 1) {
                    flag = -1;
                } else {
                    flag = 1;
                }
            }
        });

        msgAdapter = new MsgAdapter(msgArrayList, profile, getExternalCacheDir(), this);    //创建并且初始化RecyclerView
        LinearLayoutManager manager = new LinearLayoutManager(this);
        chat_recycler_msg.setAdapter(msgAdapter);
        chat_recycler_msg.setLayoutManager(manager);

        buttonList = new ArrayList<>();
        buttonList.add(chat_btn_photo);

        connection = new Connection(receiveHandler, mContext, property, getExternalCacheDir());     //创建并且开启连接模块
        new Thread(connection).start();

        Toast.makeText(ChatActivity.this, "欢迎来到聊天室", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed () {  //判断是否要退出聊天界面
        AlertDialog.Builder quitdialog = new AlertDialog.Builder(ChatActivity.this);
        quitdialog.setTitle("确定要退出吗?");
        quitdialog.setPositiveButton("退出", new DialogInterface.OnClickListener() {
            @Override
            public void onClick (DialogInterface dialog, int which) {
                property = null;    //退出时清空所有的连接信息来为下一次登陆做准备
                profile = null;
                finish();
            }
        });
        quitdialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick (DialogInterface dialog, int which) {
                return;
            }
        });
        quitdialog.show();
    }

    private void buttonAnimation (final List<ImageButton> buttonList, int radius) {

        for (int i = 0; i < buttonList.size(); i++) {

            ObjectAnimator objAnimatorY;
            ObjectAnimator objAnimatorRotate;

            // 将按钮设为可见
            buttonList.get(i).setVisibility(View.VISIBLE);

            // 按钮在X、Y方向的移动距离
            float distanceY = -(float) (flag * radius * 150);

            // Y方向移动
            objAnimatorY = ObjectAnimator.ofFloat(buttonList.get(i), "y", buttonList.get(i).getY(), buttonList.get(i).getY() + distanceY);
            objAnimatorY.setDuration(150);
            objAnimatorY.setStartDelay(100);
            objAnimatorY.start();

            // 按钮旋转
            objAnimatorRotate = ObjectAnimator.ofFloat(buttonList.get(i), "rotation", 0, 360);
            objAnimatorRotate.setDuration(300);
            objAnimatorY.setStartDelay(100);
            objAnimatorRotate.start();

            if (flag == -1) {
                objAnimatorY.addListener(new Animator.AnimatorListener() {

                    @Override
                    public void onAnimationStart (Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat (Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd (Animator animation) {
                        // 将按钮设为不可见
                        for (int i = 0; i < buttonList.size(); i++) {
                            buttonList.get(i).setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onAnimationCancel (Animator animation) {
                    }
                });
            }

        }
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_OK){
            switch (requestCode) {
                case FROM_ALBUM:        //当用户选择了一张想要发送的照片时
                    try {
                        Uri uri = data.getData();   //获取被封装过的Uri
                        getBitmap(uri);     //生成Bitmap并且发送
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }
    }

    private void getBitmap (Uri uri) throws IOException {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};   //DATA在公用数据库中存放的是真实地址
        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
        String path = null;
        if (cursor != null) {   //查询公用数据库并且获取图片真实路径
            if (cursor.moveToNext()) {
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                path = cursor.getString(columnIndex);
            }
            cursor.close();
        }
        if (path != null) {     //如果正确地获取了图片地址
            Bitmap bitmap = BitmapFactory.decodeFile(path);     //将图片转为Bitmap
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // 把压缩后的数据存放到baos中
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);      //将图片以20%的清晰度压缩进字节流
            byte[] image = baos.toByteArray();      //以字节提取压缩过的图片
            Bitmap compressedBitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
            Msg msg = new Msg(profile.getName(), image, IMAGE_SEND);    //创建将要发送的信息

            msgArrayList.add(msg);
            msgAdapter.notifyDataSetChanged();
            chat_recycler_msg.scrollToPosition(msgArrayList.size() - 1);    //放入RecyclerView

            Message message = new Message();
            message.what = IMAGE_SEND;
            message.obj = msg;
            connection.sendHandler.sendMessage(message);    //发送信息
        }
    }
}
