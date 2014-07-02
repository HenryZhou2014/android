package com.jsdf.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

import android.app.Activity;

import com.jsdf.bean.ProductObject;
import com.jsdf.exception.AppException;

public class CacheUtils extends Activity{
	private static final String CACHE_FILE = "cache.dat";
	private static String fileUrl = "";
	private static CacheUtils instance = new CacheUtils();
	
	public synchronized static CacheUtils getInstance(){
		if(instance==null){
			instance = new CacheUtils();
		}
		return instance;
	}
	
	public  void 	 setCache(ProductObject productObject) throws AppException{
		ObjectOutputStream out =null;
		try {
			FileOutputStream openFileOutput = openFileOutput(CACHE_FILE,MODE_PRIVATE);
			out= new ObjectOutputStream(new BufferedOutputStream(openFileOutput));
			out.writeObject(productObject);	
		} catch (StreamCorruptedException e) {
			throw new AppException("缓存资源异常", e);
		} catch (FileNotFoundException e) {
			throw new AppException("缓存资源异常，缓存文件不存在", e);
		} catch (IOException e) {
			throw new AppException("缓存资源异常，缓存读写异常", e);
		}
		finally{
			try {
				if(out!=null)out.close();
			} catch (IOException e) {
				throw new AppException("缓存资源异常，关闭写入流异常", e);
			}
		}
	}
	
	public  ProductObject getCache() throws AppException{
		ObjectInputStream in=null;
		ProductObject productObject=null;
//		CacheUtils.class.getClassLoader().getResourceAsStream("config.properties");
		try {
			FileInputStream fileInputStream = openFileInput(CACHE_FILE);
			in = new ObjectInputStream(new BufferedInputStream(fileInputStream));
			productObject = (ProductObject)in.readObject();

		} catch (StreamCorruptedException e) {
			throw new AppException("缓存资源异常", e);
		} catch (FileNotFoundException e) {
			throw new AppException("缓存资源异常，缓存文件不存在", e);
		} catch (IOException e) {
			throw new AppException("缓存资源异常，缓存读写异常", e);
		} catch (ClassNotFoundException e) {
			throw new AppException("缓存资源异常，转换异常", e);
		}
		finally{
			try {
				if(in!=null)in.close();
			} catch (IOException e) {
				throw new AppException("缓存资源异常，关闭读取刘异常", e);
			}
		}
		return productObject;
	}

}
