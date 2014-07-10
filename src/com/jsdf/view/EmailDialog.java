package com.jsdf.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import com.example.jsdfgrp.R;

public class EmailDialog  extends Dialog {
	Context context;
    public EmailDialog(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        this.context = context;
    }
    public EmailDialog(Context context, int theme){
        super(context, theme);
        this.context = context;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.email_dialog);
    }
}
