package com.example.consoletest;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

class MyServer {
	private BufferedReader reader;
	private ServerSocket server;
	private Socket socket;
	private ArrayList<MyChannal> mychannals = new ArrayList<>();

	void getserver() {
		try {
			server = new ServerSocket(7777);
			System.out.println("服务器创建完成");
			for (;;) {
				System.out.println("现连接数量:" + mychannals.size());
				socket = server.accept();
				MyChannal myChannal = new MyChannal(socket);
				mychannals.add(myChannal);
				new Thread(myChannal).start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class MyChannal implements Runnable {

		private Socket client;
		private DataInputStream dis;
		private DataOutputStream dos;
		private String name;
		private boolean isRunning = false;

		public MyChannal(Socket client) {
			try {
				this.client = client;
				dis = new DataInputStream(client.getInputStream());
				dos = new DataOutputStream(client.getOutputStream());
				this.name = dis.readUTF();
				send("System", "欢迎来到聊天室");
				broadCast("System", this.name + "来到聊天室");
				isRunning = true;
			} catch (IOException e) {
				e.printStackTrace();
				isRunning = false;
			}
		}

		@Override
		public void run() {
			while (isRunning) {
				sendOthers(receive());
			}
		}

		public void send(String name, String msg) {
			try {
				if(!msg.trim().equals("")) {
					dos.writeUTF(name + "/" + msg);
					dos.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private String receive() {
			String msg = "";
			try {
				msg = dis.readUTF();
				System.out.println(this.name + ":" + msg);
			} catch (Exception e) {
				e.printStackTrace();
				isRunning = false;
				mychannals.remove(this);
			}
			return msg;
		}

		private void broadCast(String name, String msg) {
			for (MyChannal other : mychannals) {
				if (other!=null && other != this) {
						other.send(name, msg);
				}
			}
		}

		private void sendOthers(String msg) {
			if (msg.startsWith("@")) {
				if (msg.indexOf(":") > -1) {
					String targetName = msg.substring(1, msg.indexOf(':'));
					String targetMsg = msg.substring(msg.indexOf(':') + 1);
					boolean isSend = false;
					for (MyChannal target : mychannals) {
						if (target.name.equals(targetName)) {
							target.send(this.name, "[私聊]" + targetMsg);
							isSend = true;
						}
					}
					if (isSend == false) {
						send("System", "用户名不存在");
					}
				} else {
					send("System", "格式错误---@用户名:消息内容");
				}
			} else {
				broadCast(this.name, msg);
			}
		}

	}
}

public class ServerTest {
	public static void main(String[] args) {
		MyServer server = new MyServer();
		server.getserver();
	}
}
