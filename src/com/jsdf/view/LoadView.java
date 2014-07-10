package com.jsdf.view;

import android.app.Activity;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.LinearLayout;

import com.example.jsdfgrp.R;

public class LoadView {
	private Dialog dialog;
	
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
	     
    public void hide() { 
        dialog.dismiss(); 
    } 
	
}
