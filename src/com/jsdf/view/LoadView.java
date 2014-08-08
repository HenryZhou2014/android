package com.jsdf.view;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.jsdfgrp.R;

public class LoadView {
	private Dialog dialog;
	private  TextView textView;
	private Timer timer ;
	private int[] loadIds = {R.string.load_warit1,R.string.load_warit2,R.string.load_warit3,R.string.load_warit4};
	private int indexId = 0;
	public LoadView(Activity activity){
		dialog = new Dialog(activity,R.style.mask_dialog);
		LinearLayout popView = (LinearLayout) LayoutInflater.from(activity).inflate(R.layout.mask_wait, null);
		
		dialog.setContentView(popView,  
                new LinearLayout.LayoutParams( 
                        LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)); 
        dialog.setFeatureDrawableAlpha(Window.FEATURE_OPTIONS_PANEL, 0); 
	}
	
	 public void show() { 
        dialog.show(); 
	 } 
	 
	 public void show(int id) { 
		 textView = (TextView)dialog.findViewById(R.id.waitId);
		 textView.setText(id);
	     dialog.show(); 
	 } 
	 
	 public void showDyn() { 
		 timer = new Timer();
		 
		 textView = (TextView)dialog.findViewById(R.id.waitId);
		 timer.schedule(new TimerTask(){
			@Override
			public void run() {
				textView.setText(loadIds[indexId]);
				if(indexId<loadIds.length ){
					indexId++;
				}else{
					indexId=0;
				}
			}
			 
		 }, 0,200);
	     dialog.show(); 
	 }  
	 
	 public void hideDyn() { 
		 	timer.cancel();
		 	timer=null;
	        dialog.dismiss(); 
	} 
	 
    public void hide() { 
        dialog.dismiss(); 
    } 
	
}
