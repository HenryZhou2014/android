package com.jsdf.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.example.jsdfgrp.R;

public class ReloginDialog  extends Dialog {
	Context context;
	
    public ReloginDialog(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        this.context = context;
    }
    public ReloginDialog(Context context, int theme){
        super(context, theme);
        this.context = context;
       
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.relogin_panel);
    }
}
