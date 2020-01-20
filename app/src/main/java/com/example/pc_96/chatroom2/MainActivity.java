package com.example.pc_96.chatroom2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by pc-96 on 2018/8/10.
 * 登陆模块,用于获取登陆信息,与ProfileCheck交互以向服务器比对登陆信息
 */
public class MainActivity extends BasicActivity {
    public static final int ACT_LOGIN = 0;
    public static final int PROPERTY_GET = 2;
    public static final int RES_PASS = 3;
    public static final int RES_DENY = 4;
    public static final int ERR_RECEIVE = 5;
    public static final int CONNECT_READY = 6;
    public static final int PERMITION_EXTERNAL = 16;

    private Button btn_login;
    private Button btn_register;
    private EditText edit_username;
    private EditText edit_password;
    Handler handler_receive;
    private ProfileCheck profileCheck;
    private ProgressBar progress_bar;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_login = findViewById(R.id.btn_login);
        btn_register = findViewById(R.id.btn_register);
        edit_username = findViewById(R.id.edit_username);
        edit_password = findViewById(R.id.edit_password);
        progress_bar = findViewById(R.id.progress_bar);

        handler_receive = new Handler(Looper.myLooper()) {  //与ProfileCheck交互
            @Override
            public void handleMessage (Message message) {
                switch (message.what) {
                    case PROPERTY_GET:  //与服务器完成连接,提取Socket与IO流
                        property = (Property) message.obj;
                        break;
                    case RES_PASS:  //登陆成功保存头像并且前往ChatActivity
                        profile = (Profile) message.obj;
                        if (profile.getImage() != null) {
                            File file = new File(getExternalCacheDir(), profile.getName() + ".png");
                            FileOutputStream fo;
                            try {
                                if (!file.exists()) {
                                    file.delete();
                                    file.createNewFile();
                                }
                                fo = new FileOutputStream(file);
                                fo.write(profile.getImage());
                                fo.flush();
                                fo.close();
                            } catch (Exception e) {
                                Toast toast_photo = Toast.makeText(MainActivity.this, "头像储存失败", Toast.LENGTH_LONG);
                                toast_photo.setGravity(Gravity.CENTER_VERTICAL | Gravity.TOP, 0, 50);
                                toast_photo.show();
                                e.printStackTrace();
                            }
                        }
                        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                        profileCheck.sendHandler.removeCallbacks(profileCheck);     //关闭ProfileCheck
                        startActivity(intent);
                        break;
                    case RES_DENY:  //登陆失败
                        Toast toast_deny = Toast.makeText(MainActivity.this, "账户名或密码错误", Toast.LENGTH_LONG);
                        toast_deny.setGravity(Gravity.CENTER_VERTICAL | Gravity.TOP, 0, 50);
                        toast_deny.show();
                        break;
                    case ERR_RECEIVE:
                        Toast toast_err = Toast.makeText(MainActivity.this, "连接出现错误,请检查网络或联系管理员", Toast.LENGTH_LONG);
                        toast_err.setGravity(Gravity.CENTER_VERTICAL | Gravity.TOP, 0, 50);
                        toast_err.show();
                        break;
                    case CONNECT_READY:     //ChatActivity发回用于确定连接完成
                        MainActivity.this.sendMessage(ProfileCheck.ACT_LOGIN, profile);
                        break;
                    default:
                        break;
                }
                progress_bar.setVisibility(View.GONE);  //尝试连接后撤回进度条并且使按钮可以点击
                btn_login.setClickable(true);
                btn_register.setClickable(true);
            }
        };

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                String username = edit_username.getText().toString();
                String password = edit_password.getText().toString();
                profile = new Profile(username, password, ACT_LOGIN);
                profileCheck = new ProfileCheck(handler_receive, MainActivity.this, property);
                new Thread(profileCheck).start();       //创建登陆线程

                progress_bar.setVisibility(View.VISIBLE);       //显示进度条并且使按钮不可点击
                btn_login.setClickable(false);
                btn_register.setClickable(false);
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);  //前往注册模块
                startActivity(intent);
            }
        });

        //应用开始前确认权限信息
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMITION_EXTERNAL);
        } else {
            btn_login.setEnabled(true);
            btn_register.setEnabled(true);
        }

    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMITION_EXTERNAL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(MainActivity.this, "请前往设置开启权限", Toast.LENGTH_LONG).show();
                    btn_register.setEnabled(false);
                    btn_login.setEnabled(false);
                }
        }
    }

    private void sendMessage (int what, Object obj) {   //向ProfileCheck发送信息
        Message message = new Message();
        message.what = what;
        message.obj = obj;
        profileCheck.sendHandler.sendMessage(message);
    }
}
