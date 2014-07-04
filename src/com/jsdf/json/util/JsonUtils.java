package com.jsdf.json.util;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.jsdf.bean.OrderList;
import com.jsdf.bean.ProductObject;
import com.jsdf.exception.AppException;

public class JsonUtils {
	
	/**
	 * @author Henry 
	 * @see <p>拿货明细字符串转化为对象</p>
	 * @param proList
	 * @return
	 * @throws AppException 
	 */
	public static ProductObject  convertProductFromJsonStr(String proList) throws AppException {
		JSONObject json=null;
		JSONArray jsonArray = null;
		ProductObject  productObj=null;
		try{
			json = JSONObject.fromString(proList);
		}catch(Exception e){
			throw new AppException("JSON 字符串转换OBJ异常",e);
		}
		try{
			jsonArray = JSONArray.fromObject(json.get("order_list"));
			Object[] orderList = jsonArray.toArray();
			productObj = new ProductObject();
			productObj.setError((Integer)json.get("error"));
			productObj.setContent((String)json.get("content"));
			productObj.setTime((Integer)json.get("time"));
			List<OrderList> productList = new ArrayList<OrderList>();
			for(Object tmpObj:orderList){
				JSONObject jsonObject =  ((JSONObject)tmpObj);
				OrderList tmpOrder = new OrderList();
				tmpOrder.setPay_time(jsonObject.getString("pay_time"));
				tmpOrder.setOrder_id(jsonObject.getString("order_id"));
				tmpOrder.setPurchase_code(jsonObject.getString("purchase_code"));
				tmpOrder.setGoods_number(jsonObject.getString("goods_number"));
				tmpOrder.setGoods_price(jsonObject.getString("goods_price"));
				tmpOrder.setContent(jsonObject.getString("content"));
				tmpOrder.setGoods_attr(jsonObject.getString("goods_attr"));
				tmpOrder.setHow_oos(jsonObject.getString("how_oos"));
				tmpOrder.setIs_get(jsonObject.getString("is_get"));
				tmpOrder.setRec_id(jsonObject.getString("rec_id"));
				tmpOrder.setUser_id(jsonObject.getString("user_id"));
				tmpOrder.setEmail(jsonObject.getString("email"));
				tmpOrder.setQq(jsonObject.getString("qq"));
				tmpOrder.setMobile_phone(jsonObject.getString("mobile_phone"));
				tmpOrder.setG_goods_type(jsonObject.getString("goods_type"));
				tmpOrder.setShort_order_time(jsonObject.getString("short_order_time"));
				tmpOrder.setMarket(jsonObject.getString("market"));
				tmpOrder.setFloor(jsonObject.getString("floor"));
				productList.add(tmpOrder);
			}
			productObj.setOrder_list(productList);
		}catch(Exception e){
			throw new AppException("JSON对象字符串转换productObj异常",e);
		}
		return productObj;
	}
	
	public static String[]  getLoginCodeByJsonStr(String json) throws AppException {
		String[] code=new String[2];
		try{
			code[0] = JSONObject.fromString(json).getString("error");
			code[1] = JSONObject.fromString(json).getString("content");
		}catch(Exception e){
			throw new AppException("登录   JSON 字符串转换OBJ异常",e);
		}
		return code;
	}
	
	
	public static JSONObject strConvert2Json(String str) throws AppException{
		JSONObject  jsonObj =null;
		try{
			jsonObj= JSONObject.fromString(str);
		}catch(Exception e){
			throw new AppException("拿单请求返回数据json转换异常",e);
		}
		return jsonObj;
	}
}
