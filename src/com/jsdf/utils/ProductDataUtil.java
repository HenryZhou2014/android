package com.jsdf.utils;

import java.util.ArrayList;
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
	public static ProductObject getProductObject() {
		return productObject;
	}

	public static void setProductObject(ProductObject productObject) {
		ProductDataUtil.productObject = productObject;
	}
	
	public static ProductObject searchProduct(Map<String,String> condition){
		ProductObject returnObj = new ProductObject();
		ProductObject catchProduct = getProductObject();
		returnObj.setContent(catchProduct.getContent());
		returnObj.setError(catchProduct.getError());
		returnObj.setTime(catchProduct.getTime());
		List<OrderList> returnList = new ArrayList<OrderList>();
		List<OrderList> catchList = catchProduct.getOrder_list();
		String isGetCon = condition.get(ISGET_NAME);
		String areaCon = condition.get(AREACODE_NAME);
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
	
	
}
