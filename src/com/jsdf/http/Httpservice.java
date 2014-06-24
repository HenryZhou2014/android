package com.jsdf.http;

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import android.util.Log;

import com.jsdf.exception.AppException;
import com.jsdf.utils.Utils;


public class Httpservice {
	
	public static String baseUrl = "http://www.jisudaifa.com/";
	public static String testBaseUrl = "http://df.jisudaifa.com/";
	public static final String USER_LOGIN= "user.php";
	public static final String USER_REGISTER="app_user.php";
	public static final String TEAM_GET_PRODUCT="wosdf/purchasing.php"; 
	public static final String USER_SIGN_GRP="wosdf/privilege.php";
	
	public static final String ACTION_GRP_LOGIN = "signin";
	public static String clientSessionId="";
	private static final String COOKIE_NAME="ECS_ID";
	private static Cookie[] cookie=null;
	private static HttpClient client = new HttpClient();
	private static int timeOut = 1000*500;
	static{
		client.getHttpConnectionManager().getParams().setConnectionTimeout(timeOut);
	}
	public static void sysnSessionId(){
		clientSessionId = Utils.getProperties(Utils.SESSIONID);
	}
	
	public  static String get(String subUrl,String parames){
		String getUrl = testBaseUrl+subUrl+"?"+parames;
		getUrl += "&"+COOKIE_NAME + "=" +clientSessionId;
		System.out.println("getUrl:"+getUrl);
		GetMethod get  = new GetMethod(getUrl);
//		get.setRequestHeader("Cookie","ECSCP_ID=f17bdf38d9a7f8115635fe983eff4a8a4f3056f5; ECS_ID=ab5f1f0b6018df662b5b119beadc1d081823a469;");
		String returnStr="";
		try {
			int status = client.executeMethod(get);
			if(status!= HttpStatus.SC_OK){
				System.err.println("Method failed: "+ get.getStatusLine());
			    return status+"_ERROR";
			}
			returnStr = get.getResponseBodyAsString();
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return returnStr;
	}
	
	public static String post(String subUrl,String parames){
		String postUrl = testBaseUrl+subUrl;
		System.out.println("postUrl:"+postUrl);
		PostMethod post  = new PostMethod(postUrl);
		post.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,new DefaultHttpMethodRetryHandler());  //使用系统提供的默认的恢复策略
		post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,"utf-8"); //编码
		
		post.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
		String returnStr="";
		String[] params = parames.split("&");
		for(String param : params){
			String[] tmpParam = param.split("=");
			post.addParameter(tmpParam[0], tmpParam[1]);
		}
		try {
			int status = client.executeMethod(post);
			if(status!= HttpStatus.SC_OK){
				System.err.println("Method failed: "+ post.getStatusLine());
			    return status+"_ERROR";
			}
			returnStr = post.getResponseBodyAsString();
			Header[] h = post.getResponseHeaders();
			for(Header hh:h){
				System.out.println(hh.getName()+":"+hh.getValue());
			}
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return returnStr;
	}
	
	public static String LoginGRP(String username,String passwrod) throws AppException{
		String postUrl = testBaseUrl+USER_SIGN_GRP;
		String parames = "username="+username+"&password="+passwrod+"&act="+ACTION_GRP_LOGIN+"&type=1&is_ajax=1";
		Log.v("loginUrl", postUrl);
		Log.v("parames", parames);
		PostMethod post  = new PostMethod(postUrl);
		post.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,new DefaultHttpMethodRetryHandler());  //使用系统提供的默认的恢复策略
		post.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,"utf-8"); //编码
		
		String returnStr="";
		String[] params = parames.split("&");
		for(String param : params){
			String[] tmpParam = param.split("=");
			post.addParameter(tmpParam[0], tmpParam[1]);
		}
		try {
			int status = client.executeMethod(post);
			
			Header[] h = post.getResponseHeaders();
			for(Header hh:h){
				System.out.println(hh.getName()+":"+hh.getValue());
				if(hh.getValue().contains(COOKIE_NAME)){
					clientSessionId = hh.getValue().substring(7, 47);
				}
			}
			
			
			if(status!= HttpStatus.SC_OK){
				System.err.println("Method failed: "+ post.getStatusLine());
			    return status+"_ERROR";
			}
			returnStr = post.getResponseBodyAsString();
			
			
		}catch(UnknownHostException e){
			Log.v("getProductList EXCPTION",e.getMessage());
			throw new AppException("域名无法正常解析，服务器异常",e);
		} catch (HttpException e) {
			Log.v("getProductList EXCPTION",e.getMessage());
			throw new AppException("请求服务器异常",e);
		} catch (IOException e) {
			Log.v("getProductList EXCPTION",e.getMessage());
			throw new AppException("请求服务器异常",e);
		}
		Log.v("LOGIN_RESULT", returnStr);
		return returnStr;
	}
	
	
	public  static String getProductList(String subUrl) throws AppException{
		Log.v("sysnSessionIdBefore", clientSessionId);
		sysnSessionId();
		Log.v("sysnSessionIdAfter", clientSessionId);
		System.out.println("getProductList sessionId:"+clientSessionId);
		String getUrl = testBaseUrl+subUrl+"?"+"act=mobilelist&type=1&is_ajax=1";
		getUrl += "&"+COOKIE_NAME + "=" +clientSessionId;
		System.out.println("getUrl:"+getUrl);
		GetMethod get  = new GetMethod(getUrl);
		String returnStr="";
		try {
			int status = client.executeMethod(get);
			if(status!= HttpStatus.SC_OK){
				System.err.println("Method failed: "+ get.getStatusLine());
			    return status+"_ERROR";
			}
			returnStr = get.getResponseBodyAsString();
		} 
		catch(UnknownHostException e){
			Log.v("getProductList EXCPTION",e.getMessage());
			throw new AppException("域名无法正常解析，服务器异常",e);
		}
		catch (HttpException e) {
			Log.v("getProductList EXCPTION",e.getMessage());
			throw new AppException("请求服务器异常",e);
		} catch (IOException e) {
			Log.v("getProductList EXCPTION",e.getMessage());
			throw new AppException("请求服务器异常",e);
		}
		return returnStr;
	}
	
	public static void main(String[] args){
//		System.out.println(LoginGRP("admin","admin123"));
//		System.out.println(getProductList(TEAM_GET_PRODUCT, "act=mobilelist"));
	}
	
}
