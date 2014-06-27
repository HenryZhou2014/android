package com.example.jsdfgrp;

import com.jsdf.utils.ProductDataUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

public class FilterActivity extends Activity {
	
	private Button confimBtn = null;
	private Button concelBtn = null;
    private Spinner spinnerIsGet;  
    private Spinner spinnerArea;
	private String selectIsGetCode;
	private String  selectAreaCode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filter_panel);
		
		confimBtn = (Button) findViewById(R.id.filterConfirm);  
		concelBtn = (Button) findViewById(R.id.filterCancel);  
		
		confimBtn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				
				if("已拿".equals(selectIsGetCode)){
					intent.putExtra(ProductDataUtil.ISGET_NAME, "1");
				}else if("未拿".equals(selectIsGetCode)){
					intent.putExtra(ProductDataUtil.ISGET_NAME, "0");
				}else{
					intent.putExtra(ProductDataUtil.ISGET_NAME, "");
				}
				
				if("无".equals(selectAreaCode)){
					intent.putExtra(ProductDataUtil.AREACODE_NAME, "");
				}else{
					intent.putExtra(ProductDataUtil.AREACODE_NAME, selectAreaCode);
				}
				setResult(Activity.RESULT_OK, intent);
				finish();//结束之后会将结果传回From

			}
		});
		
		
        spinnerIsGet=(Spinner)findViewById(R.id.spinnerIsGet);  
		spinnerArea=(Spinner)findViewById(R.id.spinnerAreaTitle); 
		
        //添加Spinner事件监听器  
      spinnerIsGet.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				Log.v("setOnItemSelectedListener:","spinnerIsGet");
				selectIsGetCode = arg0.getItemAtPosition(arg2).toString();
				Log.v("selectIsGetCode",selectIsGetCode);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
      	
      });
      spinnerArea.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				Log.v("setOnItemSelectedListener:","spinnerArea");
				selectAreaCode = arg0.getItemAtPosition(arg2).toString();
				Log.v("selectAreaCode",selectAreaCode);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
      	
      }); 
	}

}
