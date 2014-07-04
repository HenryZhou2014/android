package com.example.jsdfgrp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.jsdf.bean.MessageHandleBean;
import com.jsdf.bean.OrderList;
import com.jsdf.bean.ProductObject;
import com.jsdf.exception.AppException;
import com.jsdf.http.Httpservice;
import com.jsdf.json.util.JsonUtils;
import com.jsdf.utils.ProductDataUtil;
import com.jsdf.view.ToastView;

public class ProductListActivity extends Activity implements OnItemSelectedListener, OnGestureListener{
	//生成动态数组，加入数据  
    ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>(); 
    //生成适配器的Item和动态数组对应的元素  
    SimpleAdapter listItemAdapter =null;
    ListView list=null;
    private Handler handler;
    private Context ctx;
    private static List<OrderList> orderList;
    private PopupWindow mpop = null;
    private Button synBtn=null; //同步按钮
    private Button filterBtn = null;
    private Dialog filterDialog = null;
    private Spinner spinnerIsGet;  
    private Spinner spinnerArea;
    private TextView productListTitle;
    
    private Map<String,String> conditionCurrent = null;  //记录当前的搜索条件
    
    
    private String  selectAreaCode = "";
    private String  selectIsGetCode = "";
    private HashMap<String, Object> map ;
    private static final String CACHE_FILE = "cache.dat"; //缓存文件
    private static String onlineModle = "1";  //1-onlineModle,2-offlineModel
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_productlist);
		onlineModle = (String) this.getIntent().getExtras().get(MainActivity.MODLE_NAME);
		ctx = this;
		synBtn = (Button) findViewById(R.id.product_sycid);  
		filterBtn = (Button) findViewById(R.id.product_fitlerid);  
	     //绑定Layout里面的ListView  
		list= (ListView) findViewById(R.id.ListView01);  
		productListTitle= (TextView)findViewById(R.id.productListTitleId);  
		
		//将可选内容与ArrayAdapter连接起来   
		String[] colors={"已拿","未拿"}; 
       
		
	    handler=new Handler(){
	    	public void handleMessage(Message msg){
	    		MessageHandleBean handleBean = (MessageHandleBean)msg.obj;
	    		if(MessageHandleBean.PRODUCT_LIST_GETALL_CODE.equals(handleBean.getMsgType())){
		    		ProductObject productObject=(ProductObject)handleBean.getData();//obj不一定是String类，可以是别的类，看用户具体的应用
		    		ProductDataUtil.setProductObject(productObject);
		    		drawListView(ProductDataUtil.getProductObject(),listItem,ctx,list);
	    		}else if(MessageHandleBean.PRODUCT_LIST_ONE_CODE.equals(handleBean.getMsgType())){
	    			String str = (String)handleBean.getData();
	    			String[] reutunArray = str.split("\\|");
	    			if(reutunArray[0].equals("0")){
	    				ProductDataUtil.updateIsGetStatus(reutunArray[2], "1");
	    				if(conditionCurrent==null){ //只显示未拿货数据
	    					conditionCurrent = new HashMap<String,String>();
	    					conditionCurrent.put(ProductDataUtil.ISGET_NAME, "0"); 
	    				}else{
	    					conditionCurrent.put(ProductDataUtil.ISGET_NAME, "0"); 
	    				}
	    				reDrawListView(ctx,conditionCurrent);
	    			}
	    		}
//	    		orderList = productObject.getOrder_list();
//            	for(int i = 0; orderList!=null&&(i<orderList.size()) ; i++){
//            		OrderList tmpOrderList = orderList.get(i);
//            		HashMap<String, Object> map = new HashMap<String, Object>();  
//            		if(tmpOrderList.getIs_get().equals("0")){
//            			map.put("ItemImage", R.drawable.checkbox_unchecked);//图像资源的ID  
//            		}else{
//            			map.put("ItemImage", R.drawable.checkbox_checked);//图像资源的ID  
//            		}
//	                map.put("ItemTitle", tmpOrderList.getMarket());  
//	                map.put("ItemText", tmpOrderList.getFloor()+"F(" +tmpOrderList.getPurchase_code()+ ") "+tmpOrderList.getGoods_attr() +" -"+tmpOrderList.getGoods_number() +"" +
//	                		"件*P"+tmpOrderList.getGoods_price() + " [" +tmpOrderList.getShort_order_time()+"]");  
//	                listItem.add(map);  
//            	}
//            	
//            	listItemAdapter= new SimpleAdapter(ctx,listItem,R.layout.list_items,new String[] {"ItemImage","ItemTitle", "ItemText"},new int[] {R.id.ItemImage,R.id.ItemTitle,R.id.ItemText});  
//         	         
//         	    //添加并且显示  
//         	    list.setAdapter(listItemAdapter);
//	    		new UpdateListView(ctx,list,productObject,listItem).start(); //子线程
	    	}
	    };
        
	    if("1".equals(onlineModle)){//Online Modle
	    	new GetProductThread(this).start(); //通过子线程获取网络数据，更新子线程
	    	productListTitle.setText(R.string.onlineModle);
	    }else if("2".equals(onlineModle)){ //Offline Modle
	    	try {
	    		ProductObject offObjct = getCache();
				drawListView(offObjct,listItem,ctx,list);
				ProductDataUtil.setProductObject(offObjct);
				productListTitle.setText(R.string.offlineModle);
			} catch (AppException e) {
				ToastView toast = new ToastView(ctx,e.getMessage());
			    toast.setGravity(Gravity.CENTER, 0, 0);
			    toast.show();
			}
	    }
	    
        //添加点击  
        list.setOnItemClickListener(new OnItemClickListener() {  
  
            @Override  
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,  
                    long arg3) {  
//                setTitle("点击第"+arg2+"个项目");  
            }  
        });  
       
       /**
        * @see<p>长按</p>
        */
       list.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
//				ImageView itemImage = (ImageView)arg1.findViewById(R.id.ItemImage);
//				itemImage.setImageResource(R.drawable.checkbox_checked);
				OrderList selectOrder = ProductDataUtil.getCacheFromListByIndex(arg2);
				if("1".equals(onlineModle)){
				    ProductDataUtil.updateIsGetStatus(selectOrder.getOrder_id(), "1");
				    new UpdateOneProductStatus((ProductListActivity)ctx,selectOrder.getRec_id(),selectOrder.getOrder_id()).start();
				}else if ("2".equals(onlineModle)){
				    ProductDataUtil.updateIsGetStatus(selectOrder.getOrder_id(), "1");
				}
//				ToastView toast = new ToastView(ctx,selectOrder.getEmail()+" : " + selectOrder.getMarket());
//			    toast.setGravity(Gravity.CENTER, 0, 0);
//			    toast.show();
				return false;
			}
       });
//       list.setOnClickListener(l)   
      //添加长按点击  
//        list.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {  
//            @Override  
//            public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
//                menu.setHeaderTitle("操作");     
//                menu.add(0, 0, 0, "标记为已拿货");  
//                menu.add(0, 1, 0, "标记为未拿货");
//                menu.add(0, 2, 0, "发送邮件信息");    
//            }  
//        }); 
        
        synBtn.setOnClickListener(new Button.OnClickListener() {//同步按钮
			@Override
			public void onClick(View arg0) {
				try {
					setCache(ProductDataUtil.getProductObject());
					ProductObject cacheObj = getCache();
//					System.out.println(cacheObj);
//					System.out.println(cacheObj.getOrder_list());
//					if(cacheObj.getOrder_list()!=null)
//						System.out.println(cacheObj.getOrder_list());
					ToastView toast = new ToastView(ctx,"同步成功");
				    toast.setGravity(Gravity.CENTER, 0, 0);
				    toast.show();
				} catch (AppException e) {
					ToastView toast = new ToastView(ctx,e.getMessage());
				    toast.setGravity(Gravity.CENTER, 0, 0);
				    toast.show();
				}
			}
		});
        
        filterBtn.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent filterIntent = new Intent(ctx,FilterActivity.class);
				int requestCode =0;
				Bundle  Bundle =null;
				startActivityForResult(filterIntent, requestCode, Bundle);

			}
		});
        
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View filterView = layoutInflater.inflate(R.layout.filter_panel, null);
        filterDialog = new AlertDialog.Builder(this)
        	.setTitle("条件筛选")
        	.setIcon(R.drawable.jsdf_logo)
        	.setView(filterView)
        	.setPositiveButton("确定", new DialogInterface.OnClickListener() {
        	     @Override
        	     public void onClick(DialogInterface dialog, int which) {
        	    	 selectIsGetCode = spinnerIsGet.getItemAtPosition(spinnerIsGet.getSelectedItemPosition()).toString();
        	    	 selectAreaCode =  spinnerArea.getSelectedItem().toString();
        	    		Log.v("testValue :",selectIsGetCode + " : " + selectAreaCode);
        	    	 ToastView toast = new ToastView(ctx,selectAreaCode+":"+selectIsGetCode);
        			    toast.setGravity(Gravity.CENTER, 0, 0);
        			    toast.show();
        	     }
        	})
        	 .setNegativeButton("取消", new DialogInterface.OnClickListener() {
		     @Override
		     public void onClick(DialogInterface dialog, int which) {
		      // TODO Auto-generated method stub
		     }
		    }).create();   
    }  
      
    //长按菜单响应函数  
    @Override  
    public boolean onContextItemSelected(MenuItem item) {  
//        setTitle("点击了长按菜单里面的第"+item.getItemId()+"个项目"); 
    	switch (item.getItemId()) {
		case 0: //标记为已拿货
//			ImageView itemImage = (ImageView)findViewById(R.id.ItemImage);
//			itemImage.setImageResource(R.drawable.checkbox_checked);
			break;
		case 1:
			break;
		case 2:
			break;
		default:
			break;
		}
        return super.onContextItemSelected(item);  
    } 
    
    public Handler getHandler(){
    	return this.handler;
    }

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
//		Log.v("arg0.getId():",arg0.getId()+"");
//		if(arg0.getId() == R.id.spinnerIsGet){
//			selectIsGetCode = arg0.getItemAtPosition(arg2).toString();
////			ToastView toast = new ToastView(ctx,selectIsGetCode);
////		    toast.setGravity(Gravity.CENTER, 0, 0);
////		    toast.show();
//			Log.v("selectIsGetCode",selectIsGetCode);
//		}
//		if(arg0.getId() == R.id.spinnerAreaTitle){
//			selectAreaCode = arg0.getItemAtPosition(arg2).toString();
//			Log.v("selectAreaCode",selectAreaCode);
//		}
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	switch (resultCode) { //resultCode为回传的标记，我在B中回传的是RESULT_OK
    	   case RESULT_OK:
    	    String isgetFlag = data.getStringExtra(ProductDataUtil.ISGET_NAME);
    	    String areaFlag = data.getStringExtra(ProductDataUtil.AREACODE_NAME);
			ToastView toast = new ToastView(this,isgetFlag+" - "+areaFlag);
		    toast.setGravity(Gravity.CENTER, 0, 0);
		    toast.show();
		    Map<String,String> condition = new HashMap<String,String>();
		    condition.put(ProductDataUtil.ISGET_NAME, isgetFlag);
		    condition.put(ProductDataUtil.AREACODE_NAME, areaFlag);
		    conditionCurrent= condition;
		    reDrawListView(this,condition);
    	    break;
    	default:
    	    break;
    	}
    }
    
    /**
     * @author Henry
     * @<p>显示ViewList</p>
     * @param productObject
     * @param listItem
     * @param context
     * @param list
     */
	public void drawListView(ProductObject productObject,ArrayList<HashMap<String, Object>> listItem,Context context,ListView list){
		List<OrderList> orderList  = productObject.getOrder_list();
		//清空
		listItem.clear();
    	SimpleAdapter listItemAdapter= new SimpleAdapter(context,listItem,R.layout.list_items,new String[] {"ItemImage","ItemTitle", "ItemText"},new int[] {R.id.ItemImage,R.id.ItemTitle,R.id.ItemText});  
//    	listItemAdapter.notifyDataSetChanged();
//    	listItemAdapter.notifyDataSetInvalidated();
    	list.setAdapter(listItemAdapter);
    	ProductDataUtil.clearCacheListViewData();
    	for(int i = 0; orderList!=null&&(i<orderList.size()) ; i++){
    		OrderList tmpOrderList = orderList.get(i);
    		ProductDataUtil.cacheListViewData(i, tmpOrderList);
    		map = new HashMap<String, Object>();  
    		if(tmpOrderList.getIs_get().equals("0")){
    			map.put("ItemImage", R.drawable.checkbox_unchecked);//图像资源的ID  
    		}else{
    			map.put("ItemImage", R.drawable.checkbox_checked);//图像资源的ID  
    		}
            map.put("ItemTitle", tmpOrderList.getMarket());  
            map.put("ItemText", tmpOrderList.getFloor()+"F(" +tmpOrderList.getPurchase_code()+ ") "+tmpOrderList.getGoods_attr() +" -"+tmpOrderList.getGoods_number() +"" +
            		"件*P"+tmpOrderList.getGoods_price() + " [" +tmpOrderList.getShort_order_time()+"]");  
            listItem.add(map);  
    	}
    	
    	listItemAdapter= new SimpleAdapter(context,listItem,R.layout.list_items,new String[] {"ItemImage","ItemTitle", "ItemText"},new int[] {R.id.ItemImage,R.id.ItemTitle,R.id.ItemText});  
 	         
 	    //添加并且显示  
 	    list.setAdapter(listItemAdapter);
	}
	
	public void reDrawListView(Context context,Map<String,String> condition){
		ProductObject  product = ProductDataUtil.searchProduct(condition);
		drawListView(product,listItem,context,list);
	}
    
	/**
	 * @author Henry
	 * @see <p>缓存</p>
	 * @param productObject
	 * @throws AppException
	 */
	public  void setCache(ProductObject productObject) throws AppException{
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
//			e.printStackTrace();
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
	
	/**
	 * @author Henry
	 * @see <p>读取缓存</p>
	 * @return
	 * @throws AppException
	 */
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

	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}
    
}
class UpdateListView extends Thread{ //子线程更新界面
	ListView list;
//	SimpleAdapter listItemAdapter;
	ProductObject productObject;
	ArrayList<HashMap<String, Object>> listItem;
	Context context;
	public UpdateListView(){
		
	}
	
	public UpdateListView(Context context,ListView list,ProductObject productObject,ArrayList<HashMap<String, Object>> listItem){
		this.list = list;
		this.productObject = productObject;
		this.listItem = listItem;
		this.context = context;
	}
	
	@Override
	public void run() {
		List<OrderList> orderList = productObject.getOrder_list();
    	for(int i = 0; orderList!=null&&(i<orderList.size()) ; i++){
    		OrderList tmpOrderList = orderList.get(i);
    		HashMap<String, Object> map = new HashMap<String, Object>();  
            map.put("ItemImage", R.drawable.checkbox_unchecked);//图像资源的ID  
            map.put("ItemTitle", tmpOrderList.getMarket());  
            map.put("ItemText", tmpOrderList.getFloor()+"F(" +tmpOrderList.getPurchase_code()+ ") "+tmpOrderList.getGoods_attr() +" -"+tmpOrderList.getGoods_number() +"" +
            		"件*P"+tmpOrderList.getGoods_price() + " [" +tmpOrderList.getShort_order_time()+"]");  
            listItem.add(map);  
    	}
    	
    	SimpleAdapter listItemAdapter= new SimpleAdapter(context,listItem,R.layout.list_items,new String[] {"ItemImage","ItemTitle", "ItemText"},new int[] {R.id.ItemImage,R.id.ItemTitle,R.id.ItemText});  
 	         
 	    //添加并且显示  
 	    list.setAdapter(listItemAdapter); 
	}
}

class UpdateOneProductStatus extends Thread{ //UPDATE ONE PRODUCT STATUS
	private ProductListActivity context;
	private String recid;
	private String orderId;
	public UpdateOneProductStatus(){
		
	}
	
	public UpdateOneProductStatus(ProductListActivity context,String recid,String orderId){
		this.context = context;
		this.recid = recid;
		this.orderId = orderId;
	}
	
	@Override
	public void run() {
		String returnStr= Httpservice.productOneSync(recid,"1");
		JSONObject  jsonObject = JsonUtils.strConvert2Json(returnStr);
		String returnCode =jsonObject.getString("error");
    	Message message = Message.obtain();
    	message.obj = new MessageHandleBean(MessageHandleBean.PRODUCT_LIST_ONE_CODE,returnCode+"|"+recid+"|"+orderId);
    	context.getHandler().sendMessage(message) ;     	
	}
	
}

class GetProductThread extends Thread { //子线程去范围网络资源 
	private ProductListActivity context;
	public GetProductThread(){
		
	}
	
	public GetProductThread(ProductListActivity context){
		this.context = context;
	}
	
	@Override
	public void run() {
		ProductObject productObject=null;
		String productStr ="";
		try {
			productStr= Httpservice.getProductList(Httpservice.TEAM_GET_PRODUCT);
	    	Log.v("SEESIONID", Httpservice.clientSessionId);
			productObject = JsonUtils.convertProductFromJsonStr(productStr);
		} catch (AppException e) {
			ToastView toast = new ToastView(context, e.getMessage());
	        toast.setGravity(Gravity.CENTER, 0, 0);
	        toast.show();
		}
    	Message message = Message.obtain();
    	message.obj = new MessageHandleBean(MessageHandleBean.PRODUCT_LIST_GETALL_CODE,productObject);
    	context.getHandler().sendMessage(message) ;     	
	}
	
}
