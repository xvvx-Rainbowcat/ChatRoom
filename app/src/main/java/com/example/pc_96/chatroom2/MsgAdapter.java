package com.example.pc_96.chatroom2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by pc-96 on 2018/8/10.
 * 为ChatActivity中RecyclerView准备的Adapter
 */

public class MsgAdapter extends RecyclerView.Adapter<MsgAdapter.MsgViewHolder> {

    private ArrayList<Msg> list;
    private Profile profile;
    private File filepath;  //保存缓存目录地址
    private Context context;

    static class MsgViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout relative_msg_left;
        RelativeLayout relative_msg_right;
        CircleImageView circle_imgv_msg_left;
        CircleImageView circle_imgv_msg_right;
        LinearLayout linear_msg_left;
        LinearLayout linear_msg_right;
        TextView txv_msg_name_left;
        TextView txv_msg_name_right;
        TextView txv_msg_left;
        TextView txv_msg_right;
        ImageView msg_imgv_left;
        ImageView msg_imgv_right;

        MsgViewHolder (View view) {
            super(view);
            this.relative_msg_left = view.findViewById(R.id.relative_msg_left);
            this.relative_msg_right = view.findViewById(R.id.relative_msg_right);
            this.linear_msg_left = view.findViewById(R.id.linear_msg_left);
            this.linear_msg_right = view.findViewById(R.id.linear_msg_right);
            this.circle_imgv_msg_left = view.findViewById(R.id.circle_imgv_msg_left);
            this.circle_imgv_msg_right = view.findViewById(R.id.circle_imgv_msg_right);
            this.txv_msg_name_left = view.findViewById(R.id.txv_msg_name_left);
            this.txv_msg_name_right = view.findViewById(R.id.txv_msg_name_right);
            this.txv_msg_left = view.findViewById(R.id.txv_msg_left);
            this.txv_msg_right = view.findViewById(R.id.txv_msg_right);
            this.msg_imgv_left = view.findViewById(R.id.msg_imgv_left);
            this.msg_imgv_right = view.findViewById(R.id.msg_imgv_right);
        }
    }

    public MsgAdapter (ArrayList<Msg> list, Profile profile, File filePath, Context context) {
        this.list = list;
        this.profile = profile;
        this.filepath = filePath;
        this.context = context;
    }

    @Override
    public MsgViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_item, parent, false);
        return new MsgViewHolder(view);
    }

    @Override
    public void onBindViewHolder (MsgViewHolder holder, int position) {
        Msg msg = list.get(position);
        byte[] image;   //用于缓存图片
        File file = new File(filepath, msg.getFrom() + ".png");     //从缓冲目录获取头像
        switch (msg.getStatus()) {
            case ChatActivity.TYPE_RECEIVED:    //接收到文字信息
                holder.relative_msg_left.setVisibility(View.VISIBLE);
                holder.relative_msg_right.setVisibility(View.GONE);
                holder.msg_imgv_right.setVisibility(View.GONE);
                holder.msg_imgv_left.setVisibility(View.GONE);

                holder.txv_msg_left.setText(msg.getContent());  //从msg获取聊天框中显示的聊天信息
                holder.txv_msg_name_left.setText(msg.getFrom());    //从msg获取信息来源
                holder.linear_msg_left.setBackground(context.getDrawable(R.drawable.msg_left));
                if (file.exists()) {        //如果头像文件存在就显示头像,否则就显示默认头像
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                    holder.circle_imgv_msg_left.setImageBitmap(bitmap);
                } else {
                    holder.circle_imgv_msg_left.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_person));
                }
                break;
            case ChatActivity.TYPE_SEND:        //发送文字信息
                holder.relative_msg_left.setVisibility(View.GONE);
                holder.relative_msg_right.setVisibility(View.VISIBLE);
                holder.msg_imgv_right.setVisibility(View.GONE);
                holder.msg_imgv_left.setVisibility(View.GONE);

                holder.txv_msg_right.setText(msg.getContent());
                holder.txv_msg_name_right.setText(msg.getFrom());
                holder.linear_msg_right.setBackground(context.getDrawable(R.drawable.msg_right));
                if (profile.getImage() != null) {
                    holder.circle_imgv_msg_right.setImageBitmap(BitmapFactory.decodeByteArray(profile.getImage(), 0, profile.getImage().length));
                } else {
                    holder.circle_imgv_msg_right.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_person));
                }
                break;
            case ChatActivity.IMAGE_SEND:       //发送图片信息
                holder.relative_msg_left.setVisibility(View.GONE);
                holder.relative_msg_right.setVisibility(View.VISIBLE);
                holder.msg_imgv_right.setVisibility(View.VISIBLE);
                holder.msg_imgv_left.setVisibility(View.GONE);
                holder.txv_msg_right.setText("");
                holder.linear_msg_right.setBackground(context.getDrawable(R.drawable.invisible_color)); //接收或发送图片时隐藏聊天框

                image = msg.getImage(); //从信息获取图片
                if (image != null) {    //创建Bitmap并且显示
                    Bitmap bitmap = BitmapFactory.decodeByteArray(image,0,image.length);
                    holder.msg_imgv_right.setImageBitmap(bitmap);
                }
                holder.txv_msg_name_right.setText(msg.getFrom());
                if (profile.getImage() != null) {
                    holder.circle_imgv_msg_right.setImageBitmap(BitmapFactory.decodeByteArray(profile.getImage(), 0, profile.getImage().length));
                } else {
                    holder.circle_imgv_msg_right.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_person));
                }
                break;
            case ChatActivity.IMAGE_RECEIVE:        //接收图片信息
                holder.relative_msg_left.setVisibility(View.VISIBLE);
                holder.relative_msg_right.setVisibility(View.GONE);
                holder.msg_imgv_right.setVisibility(View.GONE);
                holder.msg_imgv_left.setVisibility(View.VISIBLE);

                holder.txv_msg_left.setText("");
                holder.linear_msg_left.setBackground(context.getDrawable(R.drawable.invisible_color));
                image = msg.getImage();
                if (image != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(image,0,image.length);
                    holder.msg_imgv_left.setImageBitmap(bitmap);
                }
                holder.txv_msg_name_left.setText(msg.getFrom());
                if (file.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                    holder.circle_imgv_msg_left.setImageBitmap(bitmap);
                } else {
                    holder.circle_imgv_msg_left.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_person));
                }
                break;
        }
    }

    @Override
    public int getItemCount () {
        return list.size();
    }
}
