package com.example.jsdfgrp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import com.jsdf.bean.OrderList;
import com.jsdf.bean.ProductObject;
import com.jsdf.exception.AppException;
import com.jsdf.http.Httpservice;
import com.jsdf.json.util.JsonUtils;
import com.jsdf.utils.ProductDataUtil;
import com.jsdf.view.ToastView;

public class ProductListActivity extends Activity implements OnItemSelectedListener {
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
    private String  selectAreaCode = "";
    private String  selectIsGetCode = "";
    private HashMap<String, Object> map ;
   
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_productlist);
		ctx = this;
		
		synBtn = (Button) findViewById(R.id.product_sycid);  
		filterBtn = (Button) findViewById(R.id.product_fitlerid);  
	     //绑定Layout里面的ListView  
		list= (ListView) findViewById(R.id.ListView01);  
		 
		//将可选内容与ArrayAdapter连接起来   
		String[] colors={"已拿","未拿"}; 
       
		
	    handler=new Handler(){
	    	public void handleMessage(Message msg){
	    		ProductObject productObject=(ProductObject)msg.obj;//obj不一定是String类，可以是别的类，看用户具体的应用
	    		ProductDataUtil.setProductObject(productObject);
	    		
	    		drawListView(ProductDataUtil.getProductObject(),listItem,ctx,list);
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
         
	    new GetProductThread(this).start(); //通过子线程获取网络数据，更新子线程
	    
        //添加点击  
        list.setOnItemClickListener(new OnItemClickListener() {  
  
            @Override  
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,  
                    long arg3) {  
//                setTitle("点击第"+arg2+"个项目");  
            }  
        });  
        
       list.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				ImageView itemImage = (ImageView)arg1.findViewById(R.id.ItemImage);
				itemImage.setImageResource(R.drawable.checkbox_checked);
				return false;
			}
       });
          
      //添加长按点击  
        list.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {  
            @Override  
            public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
                menu.setHeaderTitle("操作");     
                menu.add(0, 0, 0, "标记为已拿货");  
                menu.add(0, 1, 0, "标记为未拿货");
                menu.add(0, 2, 0, "发送邮件信息");    
            }  
        }); 
        
        synBtn.setOnClickListener(new Button.OnClickListener() {//同步按钮
			@Override
			public void onClick(View arg0) {
				
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
		Log.v("arg0.getId():",arg0.getId()+"");
		if(arg0.getId() == R.id.spinnerIsGet){
			selectIsGetCode = arg0.getItemAtPosition(arg2).toString();
//			ToastView toast = new ToastView(ctx,selectIsGetCode);
//		    toast.setGravity(Gravity.CENTER, 0, 0);
//		    toast.show();
			Log.v("selectIsGetCode",selectIsGetCode);
		}
		if(arg0.getId() == R.id.spinnerAreaTitle){
			selectAreaCode = arg0.getItemAtPosition(arg2).toString();
			Log.v("selectAreaCode",selectAreaCode);
		}
		
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
		HashMap<String, Object> emptymap = new HashMap<String, Object>(); 
		listItem.add(emptymap);
    	SimpleAdapter listItemAdapter= new SimpleAdapter(context,listItem,R.layout.list_items,new String[] {"ItemImage","ItemTitle", "ItemText"},new int[] {R.id.ItemImage,R.id.ItemTitle,R.id.ItemText});  
//    	listItemAdapter.notifyDataSetChanged();
//    	listItemAdapter.notifyDataSetInvalidated();
    	list.setAdapter(listItemAdapter);
    	
    	for(int i = 0; orderList!=null&&(i<orderList.size()) ; i++){
    		OrderList tmpOrderList = orderList.get(i);
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
    	message.obj = productObject;
    	context.getHandler().sendMessage(message) ;     	
	}
	
}
