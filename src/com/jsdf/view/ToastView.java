package com.jsdf.view;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jsdfgrp.R;

public class ToastView {
	
	public static Toast toast;
	private int time;
	private Timer timer;
	
	public ToastView(Context context, String text) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.toast_view, null);
		TextView t = (TextView) view.findViewById(R.id.toast_text);
		t.setText(text);
		if(toast != null) {
			toast.cancel();
		}
		toast = new Toast(context);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(view);
	}
	
	public ToastView(Context context, int text) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.toast_view, null);
		TextView t = (TextView) view.findViewById(R.id.toast_text);
		t.setText(text);
		if(toast != null) {
			toast.cancel();
		}
		toast = new Toast(context);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(view);
	}
	
	//设置toast显示位置
	public void setGravity(int gravity, int xOffset, int yOffset) {
		//toast.setGravity(Gravity.CENTER, 0, 0); //居中显示
		toast.setGravity(gravity, xOffset, yOffset);
	}
	
	//设置toast显示时间
	public void setDuration(int duration) {
		toast.setDuration(duration);
	}
	
	//设置toast显示时间(自定义时�?
	public void setLongTime(int duration) {
		//toast.setDuration(duration);
		time = duration;
		timer = new Timer();
        timer.schedule(new TimerTask(){
        	@Override
        	public void run() {
        		 
        		if(time-1000 >= 0) {
        			show();
        			time= time - 1000;
        		} else {
        			timer.cancel();
        		}
        	}
        }, 0, 1000);
	}
	
	public void show() {
		toast.show();
	}
	
	public static void cancel() {
		if(toast != null) {
			toast.cancel();
		}
	}

}
