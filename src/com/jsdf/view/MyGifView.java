package com.jsdf.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.util.AttributeSet;
import android.view.View;

import com.example.jsdfgrp.R;

public class MyGifView extends View {
	private long movieStart;  
    private Movie movie;  
  
  
    public MyGifView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
        movie=Movie.decodeStream(getResources().openRawResource(R.drawable.loading21));
    }  
      
  
    /* (non-Javadoc) 
     * @see android.view.View#onDraw(android.graphics.Canvas) 
     */  
    @Override  
    protected void onDraw(Canvas canvas) {  
        // TODO Auto-generated method stub  
    	canvas.drawColor(Color.TRANSPARENT);
    	super.onDraw(canvas); 
        long now=android.os.SystemClock.uptimeMillis();  
        //第一次播放  
        if (movieStart == 0) {  
        	movieStart = now;  
        }  
        if (movie != null) {  
        	 int relTime = (int) ((now - movieStart) % movie.duration());
             movie.setTime(relTime);
             movie.draw(canvas, getWidth() - movie.width(), getHeight() - movie.height());
             this.invalidate(); 
        }  
         
    }  
}
