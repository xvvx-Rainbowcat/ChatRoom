package com.example.pc_96.chatroom2;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by pc-96 on 2018/8/10.
 * 注册模块,获取注册信息并且交由ProfileCheck来向服务器发送注册请求
 */
public class RegisterActivity extends BasicActivity {
    public static final int ACT_REGISTER = 1;
    public static final int PROPERTY_GET = 2;
    public static final int RES_PASS = 3;
    public static final int RES_DENY = 4;
    public static final int ERR_RECEIVE = 5;
    public static final int CONNECT_READY = 6;
    public static final int FROM_ALBUM = 13;
    public static final int FROM_CAMERA = 14;
    public static final int PERMITION_CAMERA = 15;
    public static final int CROP_FINISH = 17;
    public static final int SAME_ID = 19;
    public static final int SAME_NAME = 20;


    private Button btn_register;
    private EditText edit_username;
    private EditText edit_password;
    private EditText edit_name;
    private CircleImageView imgv_circle;
    private ImageButton img_btn_photo;
    private ImageButton img_btn_camera;
    private ArrayList<ImageButton> buttonList;
    private ProfileCheck profileCheck;
    private Handler receiveHandler;
    private Profile temp;
    private Uri imageUri;
    private int flag = 1;
    private Boolean has_photo = false;
    private File cropFile;
    private File tempPhoto;
    private String id;
    private String password;
    private String name;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        btn_register = findViewById(R.id.register_btn_register);
        img_btn_camera = findViewById(R.id.img_btn_camera);
        img_btn_photo = findViewById(R.id.img_btn_photo);
        edit_username = findViewById(R.id.register_edit_username);
        edit_password = findViewById(R.id.register_edit_password);
        edit_name = findViewById(R.id.register_edit_name);
        imgv_circle = findViewById(R.id.register_imgv_circle);

        buttonList = new ArrayList<>();
        buttonList.add(img_btn_camera);
        buttonList.add(img_btn_photo);

        receiveHandler = new Handler(Looper.myLooper()) {   //与ProfileCheck线程进行信息交互
            @Override
            public void handleMessage (Message message) {
                super.handleMessage(message);
                Toast toast = Toast.makeText(RegisterActivity.this, null, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.TOP, 0, 50);
                switch (message.what) {
                    case CONNECT_READY:     //连接完成
                        if (has_photo) {
                            Bitmap bitmap = BitmapFactory.decodeFile(cropFile.getPath());   //获取裁剪过的图片
                            ByteArrayOutputStream bos = new ByteArrayOutputStream(bitmap.getByteCount());
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);   //将图片转为Byte数组
                            byte[] image = bos.toByteArray();
                            temp = new Profile(id, password, name, image, ACT_REGISTER);
                        } else {
                            temp = new Profile(id, password, name, null, ACT_REGISTER);
                        }
                        RegisterActivity.this.sendMessage(ProfileCheck.ACT_REGISTER, temp); //向ProfileCheck发送注册请求
                        break;
                    case PROPERTY_GET:  //获取网络资源
                        property = (Property) message.obj;
                        break;
                    case RES_PASS:
                        toast.setText("注册成功");
                        toast.show();
                        profile = temp; //注册成功后复制注册信息
                        finish();
                        break;
                    case RES_DENY:
                        toast.setText("注册失败,请联系管理员");
                        toast.show();
                        break;
                    case ERR_RECEIVE:
                        toast.setText("连接出现错误,请检查网络或联系管理员");
                        toast.show();
                        break;
                    case SAME_ID:
                        toast.setText("用户名重复,请重新选择用户名");
                        toast.show();
                        break;
                    case SAME_NAME:
                        toast.setText("昵称重复,请重新选择昵称");
                        toast.show();
                        break;
                    default:
                        break;
                }
            }
        };

        edit_name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction (TextView v, int actionId, KeyEvent event) {
                String temp = edit_name.getText().toString();
                if (temp.length() >= 30) {
                    Toast.makeText(RegisterActivity.this, "昵称不可多于8个字符", Toast.LENGTH_SHORT).show();
                } else if (temp.length() <= 0) {
                    Toast.makeText(RegisterActivity.this, "昵称不可为空", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        edit_username.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction (TextView v, int actionId, KeyEvent event) {
                String temp = edit_username.getText().toString();
                if (temp.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "用户名不可少于6个字符", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        edit_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction (TextView v, int actionId, KeyEvent event) {
                String temp = edit_password.getText().toString();
                if (temp.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "少于6个字符", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        imgv_circle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
//                buttonAnimation(buttonList, 250);
//                if (flag == 1) {
//                    flag = -1;
//                } else {
//                    flag = 1;
//                }
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, FROM_ALBUM);
            }
        });

        img_btn_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, FROM_ALBUM);
            }
        });

        img_btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {  //调用系统相机并且获取相片
                if (ActivityCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.CAMERA}, PERMITION_CAMERA);
                }

                tempPhoto = new File(getExternalCacheDir(), "tempPhoto.jpg");
                try {
                    if (tempPhoto.exists()) {
                        tempPhoto.delete();
                        tempPhoto.createNewFile();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                imageUri = FileProvider.getUriForFile(RegisterActivity.this,
                        "com.example.pc_96.chatroom2.fileprovider", tempPhoto);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, FROM_CAMERA);
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                //获取注册信息并且判断
                id = edit_username.getText().toString();
                password = edit_password.getText().toString();
                name = edit_name.getText().toString();
                if (id.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "用户名不可少于6个字符", Toast.LENGTH_LONG).show();
                } else if (password.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "密码不可少于6个字符", Toast.LENGTH_SHORT).show();
                } else if (name.length() <= 0) {
                    Toast.makeText(RegisterActivity.this, "昵称不可为空", Toast.LENGTH_SHORT).show();
                } else if (name.length() >= 30) {
                    Toast.makeText(RegisterActivity.this, "用户名不可多于30个字符", Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder registerdialog = new AlertDialog.Builder(RegisterActivity.this);
                    registerdialog.setTitle("确定要注册吗?");
                    registerdialog.setCancelable(false);
                    registerdialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick (DialogInterface dialog, int which) {
                            profileCheck = new ProfileCheck(receiveHandler, RegisterActivity.this, null);
                            new Thread(profileCheck).start();   //开启线程
                        }
                    });
                    registerdialog.setNegativeButton("否", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick (DialogInterface dialog, int which) {
                        }
                    });
                    registerdialog.show();
                }
                if (!has_photo) {
                    AlertDialog.Builder photodialog = new AlertDialog.Builder(RegisterActivity.this);
                    photodialog.setTitle("没有选择头像");
                    photodialog.setCancelable(false);
                    photodialog.setNegativeButton("使用默认头像", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick (DialogInterface dialog, int which) {
                        }
                    });
                    photodialog.setPositiveButton("选择一张图片", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick (DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(intent, FROM_ALBUM);
                        }
                    });
                    photodialog.show();
                }
            }
        });
        img_btn_camera.setEnabled(false);
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode) {
                case FROM_ALBUM:
                    imageUri = data.getData();  //获取选择的图片的Uri
                    doCrop();   //裁剪图片
                    break;
                case FROM_CAMERA:
                    Toast.makeText(RegisterActivity.this, "问题等待解决", Toast.LENGTH_SHORT).show();
//                doCrop();
//                Bitmap tt = BitmapFactory.decodeFile(tempPhoto.getPath());
//                imgv_circle.setImageBitmap(tt);
                    break;
                case CROP_FINISH:   //当裁剪完成后
                    Bitmap bitmap = BitmapFactory.decodeFile(cropFile.getPath());
                    imgv_circle.setImageBitmap(bitmap);
                    has_photo = true;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMITION_CAMERA:  //获取相机的权限
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent, FROM_ALBUM);
                } else {
                    Toast.makeText(RegisterActivity.this, "请前往设置开启权限", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void doCrop () {
        cropFile = new File(getExternalCacheDir(), "crop.jpg");     //裁剪图片
        try {
            if (cropFile.exists()) {
                cropFile.delete();
                cropFile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(imageUri, "image/*");
        intent.putExtra("crop", "true");    //确定裁剪
        intent.putExtra("outputX", 200);    //X方向像素
        intent.putExtra("outputY", 200);
        intent.putExtra("aspectX", 1);      //长宽比
        intent.putExtra("aspectY", 1);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cropFile));   //放置裁剪完成后图片放置的文件的Uri地址
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);//无返回值,因为图片裁剪后直接放置在cropFile这个文件中
        startActivityForResult(intent, CROP_FINISH);
    }

    private void buttonAnimation (final List<ImageButton> buttonList, int radius) {     //按钮动画

        for (int i = 0; i < buttonList.size(); i++) {

            ObjectAnimator objAnimatorX;
            ObjectAnimator objAnimatorY;
            ObjectAnimator objAnimatorRotate;

            // 将按钮设为可见
            buttonList.get(i).setVisibility(View.VISIBLE);

            // 按钮在X、Y方向的移动距离
            float distanceX = (float) (flag * radius * (Math.cos(getAngle(buttonList.size(), i))));
            float distanceY = -(float) (flag * radius * (Math.sin(getAngle(buttonList.size(), i))));

            // X方向移动
            objAnimatorX = ObjectAnimator.ofFloat(buttonList.get(i), "x", buttonList.get(i).getX(), buttonList.get(i).getX() + distanceX);
            objAnimatorX.setDuration(300);
            objAnimatorX.setStartDelay(100);
            objAnimatorX.start();

            // Y方向移动
            objAnimatorY = ObjectAnimator.ofFloat(buttonList.get(i), "y", buttonList.get(i).getY(), buttonList.get(i).getY() + distanceY);
            objAnimatorY.setDuration(300);
            objAnimatorY.setStartDelay(100);
            objAnimatorY.start();

            // 按钮旋转
            objAnimatorRotate = ObjectAnimator.ofFloat(buttonList.get(i), "rotation", 0, 360);
            objAnimatorRotate.setDuration(300);
            objAnimatorY.setStartDelay(100);
            objAnimatorRotate.start();

            if (flag == -1) {
                objAnimatorX.addListener(new Animator.AnimatorListener() {

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

    private static double getAngle (int total, int index) {
        return Math.toRadians(180 / (total - 1) * index);
    }

    private void sendMessage (int what, Object obj) {       //向ProfileCheck发送信息
        Message message = new Message();
        message.what = what;
        message.obj = obj;
        profileCheck.sendHandler.sendMessage(message);
    }

}
