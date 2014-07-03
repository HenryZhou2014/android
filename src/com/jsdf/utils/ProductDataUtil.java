package com.jsdf.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jsdf.bean.OrderList;
import com.jsdf.bean.ProductObject;

public class ProductDataUtil {
	private static ProductObject productObject;
	public final static String ISGET_NAME="ISGET_NAME";
	public final static String AREACODE_NAME="AREACODE";
	public final static String ISGET_NAME_VALUE_UNGET="0";
	public final static String ISGET_NAME_VALUE_GET="1";
	
	/**
	 * 用于记录当前listview 显示的数据和下标
	 */
	private static Map<Integer,OrderList> listViewCacheMap = new HashMap<Integer,OrderList>(); 
	
	public static ProductObject getProductObject() {
		return productObject;
	}

	public static void setProductObject(ProductObject productObject) {
		ProductDataUtil.productObject = productObject;
	}
	
	/**
	 * 
	 * @param orderId
	 * @param status 0-未拿 1-已拿
	 * @return
	 */
	public static boolean updateIsGetStatus(String orderId,String status){
		List<OrderList> swichList = new ArrayList<OrderList>();
		List<OrderList> tmpList =  productObject.getOrder_list();
		try{
			for(int i = 0 ; tmpList!=null&& i< tmpList.size();i++ ){
				if(tmpList.get(i).getOrder_id().equals(orderId))
				{
					OrderList  switchOrder = tmpList.get(i);
					switchOrder.setIs_get(status);
					swichList.add(switchOrder);
				}else{
					swichList.add(tmpList.get(i));
				}
			}
			productObject.setOrder_list(swichList);
		}catch(Exception e){
			return false;
		}
		return true;
	}
	
	public static ProductObject searchProduct(Map<String,String> condition){
		ProductObject returnObj = new ProductObject();
		ProductObject catchProduct = getProductObject();
		returnObj.setContent(catchProduct.getContent());
		returnObj.setError(catchProduct.getError());
		returnObj.setTime(catchProduct.getTime());
		List<OrderList> returnList = new ArrayList<OrderList>();
		List<OrderList> catchList = catchProduct.getOrder_list();
		String isGetCon = condition!=null?condition.get(ISGET_NAME):"";
		String areaCon =  condition!=null?condition.get(AREACODE_NAME):"";
		if( isNotNull(isGetCon) && isNotNull(areaCon)){
			for(int i =0 ; catchList!=null&&i<catchList.size();i++){
				OrderList catchOrder = catchList.get(i);
				if(isGetCon.equals(catchOrder.getIs_get()) && areaCon.equals(catchOrder.getMarket())){
					returnList.add(catchOrder);
				}
			}
		}else if( isNotNull(isGetCon) && !isNotNull(areaCon)){
			for(int i =0 ; catchList!=null&&i<catchList.size();i++){
				OrderList catchOrder = catchList.get(i);
				if(isGetCon.equals(catchOrder.getIs_get())){
					returnList.add(catchOrder);
				}
			}
		}
		else if( !isNotNull(isGetCon) && isNotNull(areaCon)){
			for(int i =0 ; catchList!=null&&i<catchList.size();i++){
				OrderList catchOrder = catchList.get(i);
				if(areaCon.equals(catchOrder.getMarket())){
					returnList.add(catchOrder);
				}
			}
		}else{
			returnList = catchProduct.getOrder_list();
		}
		returnObj.setOrder_list(returnList);
		return returnObj;
	}
	
	public static boolean isNotNull(String str){
		if(str !=null && str.trim().length()>0){
			return true;
		}
		return false;
	}
	
	/**
	 * @see <p>记录当前listview中的所有记录下标对应的数据</p>
	 * @param index
	 * @param orderList
	 */
	public static void cacheListViewData(int index,OrderList orderList){
		listViewCacheMap.put(index, orderList);
	}
	
	/**
	 * @see <p>清空</p>
	 */
	public static void clearCacheListViewData(){
		listViewCacheMap.clear();
	}
	
	/**
	 * @see <p>更加list选择下标找到数据</p>
	 * @author Henry
	 * @param index
	 * @return
	 */
	public static OrderList getCacheFromListByIndex(int index){
		return listViewCacheMap.get(index);
	}
}

