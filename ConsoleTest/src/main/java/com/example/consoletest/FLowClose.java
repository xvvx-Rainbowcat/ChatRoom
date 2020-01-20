package com.example.consoletest;

import java.io.Closeable;
import java.io.IOException;

public class FLowClose {
	public static void close(Closeable ... io) {
		for(Closeable temp:io) {
			try {
				temp.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
