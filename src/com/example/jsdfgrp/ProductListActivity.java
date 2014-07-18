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
import java.net.URLEncoder;
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
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
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
import com.jsdf.utils.Utils;
import com.jsdf.view.EmailDialog;
import com.jsdf.view.LoadView;
import com.jsdf.view.ToastView;

public class ProductListActivity extends Activity implements OnItemSelectedListener, OnGestureListener{
	//���ɶ�̬���飬��������  
    ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>(); 
    //������������Item�Ͷ�̬�����Ӧ��Ԫ��  
    SimpleAdapter listItemAdapter =null;
    ListView list=null;
    private Handler handler;
    private Context ctx;
    private static List<OrderList> orderList;
    private PopupWindow mpop = null;
    private Button synBtn=null; //ͬ����ť
    private Button filterBtn = null;
    private Dialog filterDialog = null;
    private Spinner spinnerIsGet;  
    private Spinner spinnerArea;
    private TextView productListTitle;
    private int selectIndex = 0;
    private Map<String,String> conditionCurrent = null;  //��¼��ǰ����������
    private GestureDetector gestureScanner; 
    private LoadView loadView = null;
    private String  selectAreaCode = "";
    private String  selectIsGetCode = "";
    private HashMap<String, Object> map ;
    private static final String CACHE_FILE = "cache.dat"; //�����ļ�
    private static String onlineModle = "1";  //1-onlineModle,2-offlineModel
    private boolean longClickFlag = false; //�����͵���Ŀ���
    private EmailDialog emailDialog = null;
    private EditText emailContent;
    private OrderList dbClickSelectOrder;
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_productlist);
		onlineModle = (String) this.getIntent().getExtras().get(MainActivity.MODLE_NAME);
		ctx = this;
		synBtn = (Button) findViewById(R.id.product_sycid);  
		filterBtn = (Button) findViewById(R.id.product_fitlerid);  
	     //��Layout�����ListView  
		list= (ListView) findViewById(R.id.ListView01);  
		productListTitle= (TextView)findViewById(R.id.productListTitleId);  
		emailDialog = new EmailDialog(this,R.style.EmailDialog);
		//����ѡ������ArrayAdapter��������   
		String[] colors={"����","δ��"}; 
		loadView = new LoadView(this);
		
		
		
	    handler=new Handler(){
	    	public void handleMessage(Message msg){
	    		MessageHandleBean handleBean = (MessageHandleBean)msg.obj;
	    		if(MessageHandleBean.PRODUCT_LIST_GETALL_CODE.equals(handleBean.getMsgType())){
		    		ProductObject productObject=(ProductObject)handleBean.getData();//obj��һ����String�࣬�����Ǳ���࣬���û������Ӧ��
		    		ProductDataUtil.setProductObject(productObject);
		    		drawListView(ProductDataUtil.getProductObject(),listItem,ctx,list);
	    		}else if(MessageHandleBean.PRODUCT_LIST_ONE_CODE.equals(handleBean.getMsgType())){
	    			String str = (String)handleBean.getData();
	    			String[] reutunArray = str.split("\\|");
	    			if(reutunArray[0].equals("0")){
	    				ProductDataUtil.updateIsGetStatus(reutunArray[2], reutunArray[3]);
	    				if(conditionCurrent==null){ //ֻ��ʾδ�û�����
	    					conditionCurrent = new HashMap<String,String>();
//	    					conditionCurrent.put(ProductDataUtil.ISGET_NAME, "0"); 
	    				}else{
//	    					conditionCurrent.put(ProductDataUtil.ISGET_NAME, "0"); 
	    				}
	    				reDrawListView(ctx,conditionCurrent);
	    			}else{
	    				ToastView toast = new ToastView(ctx,reutunArray[1]);
	    			    toast.setGravity(Gravity.CENTER, 0, 0);
	    			    toast.show();
	    			}
	    		}else if(MessageHandleBean.PRODUCT_SENDEMAIL_CODE.equals(handleBean.getMsgType())){
	    			String str = (String)handleBean.getData();
	    			String[] reutunArray = str.split("\\|");
	    			if(reutunArray[0].equals("0")){
//	    				ProductDataUtil.updateIsGetStatus(reutunArray[2], "1");
//	    				if(conditionCurrent==null){ //ֻ��ʾδ�û�����
//	    					conditionCurrent = new HashMap<String,String>();
//	    					conditionCurrent.put(ProductDataUtil.ISGET_NAME, "0"); 
//	    				}else{
//	    					conditionCurrent.put(ProductDataUtil.ISGET_NAME, "0"); 
//	    				}
	    				reDrawListView(ctx,conditionCurrent);
	    				ToastView toast = new ToastView(ctx,"�����ʼ��ɹ�");
	    			    toast.setGravity(Gravity.CENTER, 0, 0);
	    			    toast.show();
	    			    emailDialog.hide();
	    			}else{
	    				ToastView toast = new ToastView(ctx,"�����ʼ�ʧ�ܣ�"+reutunArray[1]);
	    			    toast.setGravity(Gravity.CENTER, 0, 0);
	    			    toast.show();
	    			}
	    		}else if(MessageHandleBean.PRODUCT_SYCNALL_CODE.equals(handleBean.getMsgType())){
	    			String str = (String)handleBean.getData();
	    			String[] reutunArray = str.split("\\|");
	    			if(reutunArray[0].equals("000")){
	    				//reDrawListView(ctx,conditionCurrent);
	    				ToastView toast = new ToastView(ctx,"ͬ���ɹ�");
	    			    toast.setGravity(Gravity.CENTER, 0, 0);
	    			    toast.show();
	    			    try {
							setCache(new ProductObject());
							ProductDataUtil.setProductObject(new ProductObject());
							reDrawListView(ctx,conditionCurrent);
						} catch (AppException e) {
							toast = new ToastView(ctx,"[��ջ������] " + e.getMessage());
		    			    toast.setGravity(Gravity.CENTER, 0, 0);
		    			    toast.show();
						}
	    			}else{
	    				ToastView toast = new ToastView(ctx,"ͬ��ʧ��"+reutunArray[1]);
	    			    toast.setGravity(Gravity.CENTER, 0, 0);
	    			    toast.show();
	    			}
	    		}else if(MessageHandleBean.EXCEPTION_CODE.equals(handleBean.getMsgType())){
	    			ToastView toast = new ToastView(ctx,(String)handleBean.getData());
    			    toast.setGravity(Gravity.CENTER, 0, 0);
    			    toast.show();
	    		}
//	    		orderList = productObject.getOrder_list();
//            	for(int i = 0; orderList!=null&&(i<orderList.size()) ; i++){
//            		OrderList tmpOrderList = orderList.get(i);
//            		HashMap<String, Object> map = new HashMap<String, Object>();  
//            		if(tmpOrderList.getIs_get().equals("0")){
//            			map.put("ItemImage", R.drawable.checkbox_unchecked);//ͼ����Դ��ID  
//            		}else{
//            			map.put("ItemImage", R.drawable.checkbox_checked);//ͼ����Դ��ID  
//            		}
//	                map.put("ItemTitle", tmpOrderList.getMarket());  
//	                map.put("ItemText", tmpOrderList.getFloor()+"F(" +tmpOrderList.getPurchase_code()+ ") "+tmpOrderList.getGoods_attr() +" -"+tmpOrderList.getGoods_number() +"" +
//	                		"��*P"+tmpOrderList.getGoods_price() + " [" +tmpOrderList.getShort_order_time()+"]");  
//	                listItem.add(map);  
//            	}
//            	
//            	listItemAdapter= new SimpleAdapter(ctx,listItem,R.layout.list_items,new String[] {"ItemImage","ItemTitle", "ItemText"},new int[] {R.id.ItemImage,R.id.ItemTitle,R.id.ItemText});  
//         	         
//         	    //��Ӳ�����ʾ  
//         	    list.setAdapter(listItemAdapter);
//	    		new UpdateListView(ctx,list,productObject,listItem).start(); //���߳�
	    	}
	    };
        
	    if("1".equals(onlineModle)){//Online Modle
	    	new GetProductThread(this).start(); //ͨ�����̻߳�ȡ�������ݣ��������߳�
	    	productListTitle.setText(R.string.onlineModle);
	    	synBtn.setText(R.string.product_cache);
	    }else if("2".equals(onlineModle)){ //Offline Modle
	    	try {
	    		ProductObject offObjct = getCache();
				drawListView(offObjct,listItem,ctx,list);
				ProductDataUtil.setProductObject(offObjct);
				productListTitle.setText(R.string.offlineModle);
				synBtn.setText(R.string.product_syn);
				filterBtn.setText(R.string.login_login);
			} catch (AppException e) {
				ToastView toast = new ToastView(ctx,e.getMessage());
			    toast.setGravity(Gravity.CENTER, 0, 0);
			    toast.show();
			}
	    }
	    
        //��ӵ��  
        list.setOnItemClickListener(new OnItemClickListener() {  
  
            @Override  
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,  
                    long arg3) {  
            	if(!longClickFlag){ //����û�д���
	            	OrderList selectOrder = ProductDataUtil.getCacheFromListByIndex(arg2);
	            	String getFlag = selectOrder.getIs_get();
					if("1".equals(onlineModle)){
						if("0".equals(getFlag)){//�û�
							ProductDataUtil.updateIsGetStatus(selectOrder.getOrder_id(), "1");
						    new UpdateOneProductStatus((ProductListActivity)ctx,selectOrder.getRec_id(),selectOrder.getOrder_id(),"1").start();
						}else{//ȡ���û�
							ProductDataUtil.updateIsGetStatus(selectOrder.getOrder_id(), "0");
						    new UpdateOneProductStatus((ProductListActivity)ctx,selectOrder.getRec_id(),selectOrder.getOrder_id(),"0").start();
						}
					}else if ("2".equals(onlineModle)){
						try{
						    ProductDataUtil.updateIsGetStatus(selectOrder.getOrder_id(), "1");
						    setCache(ProductDataUtil.getProductObject());
						    reDrawListView(ctx,conditionCurrent);
						} catch (AppException e) {
							ToastView toast = new ToastView(ctx,e.getMessage());
						    toast.setGravity(Gravity.CENTER, 0, 0);
						    toast.show();
						}
					}
					selectIndex = arg2;
					Log.v("shortClick", "shortClick");
            	}
            	longClickFlag=false;
            }  
        });  
       
       /**
        * @see<p>����</p>
        */
       list.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
            	dbClickSelectOrder= ProductDataUtil.getCacheFromListByIndex(arg2);
            	longClickFlag=true;
            	selectIndex = arg2;
            	emailDialog.show();
            	Button emailOk = (Button) emailDialog.findViewById(R.id.dialog_button_ok);//ȷ��
            	Button emailConcel = (Button) emailDialog.findViewById(R.id.dialog_button_cancel);//ȡ��
            	Button emailTommow = (Button) emailDialog.findViewById(R.id.dialog_button_tommow);  //������
            	Button emailDown = (Button) emailDialog.findViewById(R.id.dialog_button_down); //�¼�
            	Button emailProError = (Button) emailDialog.findViewById(R.id.dialog_button_prod_err); //��Ʒ��Ϣ����
            	emailContent = (EditText)emailDialog.findViewById(R.id.emailContentId);
            	
            	emailOk.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						String emailMsg = emailContent.getText().toString();
						String emailContext = dbClickSelectOrder.getFloor()+"F(" +dbClickSelectOrder.getPurchase_code()+ ") "+dbClickSelectOrder.getGoods_attr() +" -"+dbClickSelectOrder.getGoods_number() +"" +
			            		"��*P"+dbClickSelectOrder.getGoods_price() + " [" +dbClickSelectOrder.getShort_order_time()+"]";
		            	ProductDataUtil.updateOnEmailContent(dbClickSelectOrder.getOrder_id(), emailMsg);
		            	new SendEmail((ProductListActivity)ctx,dbClickSelectOrder.getEmail(),URLEncoder.encode(emailMsg),URLEncoder.encode(emailContext),dbClickSelectOrder.getOrder_id()).start();
		            	emailContent.clearFocus();
		    			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		    			imm.hideSoftInputFromWindow(emailContent.getWindowToken(), 0);
					}
				});
            	
            	emailConcel.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						emailDialog.hide();
					}
				});
            	
            	emailTommow.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						String emailMsg = ((Resources) getBaseContext().getResources()).getString(R.string.dialog_email_tommow);
						String emailContext = dbClickSelectOrder.getFloor()+"F(" +dbClickSelectOrder.getPurchase_code()+ ") "+dbClickSelectOrder.getGoods_attr() +" -"+dbClickSelectOrder.getGoods_number() +"" +
			            		"��*P"+dbClickSelectOrder.getGoods_price() + " [" +dbClickSelectOrder.getShort_order_time()+"]";
		            	ProductDataUtil.updateOnEmailContent(dbClickSelectOrder.getOrder_id(), emailMsg);
		            	new SendEmail((ProductListActivity)ctx,dbClickSelectOrder.getEmail(),URLEncoder.encode(emailMsg),URLEncoder.encode(emailContext),dbClickSelectOrder.getOrder_id()).start();
		            	emailContent.clearFocus();
		    			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		    			imm.hideSoftInputFromWindow(emailContent.getWindowToken(), 0);
					}
				});
            	
            	emailDown.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						String emailMsg = ((Resources) getBaseContext().getResources()).getString(R.string.dialog_email_down);
						String emailContext = dbClickSelectOrder.getFloor()+"F(" +dbClickSelectOrder.getPurchase_code()+ ") "+dbClickSelectOrder.getGoods_attr() +" -"+dbClickSelectOrder.getGoods_number() +"" +
			            		"��*P"+dbClickSelectOrder.getGoods_price() + " [" +dbClickSelectOrder.getShort_order_time()+"]";
		            	ProductDataUtil.updateOnEmailContent(dbClickSelectOrder.getOrder_id(), emailMsg);
		            	new SendEmail((ProductListActivity)ctx,dbClickSelectOrder.getEmail(),URLEncoder.encode(emailMsg),URLEncoder.encode(emailContext),dbClickSelectOrder.getOrder_id()).start();
		            	emailContent.clearFocus();
		    			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		    			imm.hideSoftInputFromWindow(emailContent.getWindowToken(), 0);
					}
				});
            	
            	emailProError.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						String emailMsg = ((Resources) getBaseContext().getResources()).getString(R.string.dialog_email_product_err);
						String emailContext = dbClickSelectOrder.getFloor()+"F(" +dbClickSelectOrder.getPurchase_code()+ ") "+dbClickSelectOrder.getGoods_attr() +" -"+dbClickSelectOrder.getGoods_number() +"" +
			            		"��*P"+dbClickSelectOrder.getGoods_price() + " [" +dbClickSelectOrder.getShort_order_time()+"]";
		            	ProductDataUtil.updateOnEmailContent(dbClickSelectOrder.getOrder_id(), emailMsg);
		            	new SendEmail((ProductListActivity)ctx,dbClickSelectOrder.getEmail(),URLEncoder.encode(emailMsg),URLEncoder.encode(emailContext),dbClickSelectOrder.getOrder_id()).start();
		            	emailContent.clearFocus();
		    			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		    			imm.hideSoftInputFromWindow(emailContent.getWindowToken(), 0);
					}
				});
				return false;
			}
       });
//       list.setOnClickListener(l)   
      //��ӳ������  
//        list.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {  
//            @Override  
//            public void onCreateContextMenu(ContextMenu menu, View v,ContextMenuInfo menuInfo) {  
//                menu.setHeaderTitle("����");     
//                menu.add(0, 0, 0, "���Ϊ���û�");  
//                menu.add(0, 1, 0, "���Ϊδ�û�");
//                menu.add(0, 2, 0, "�����ʼ���Ϣ");    
//            }  
//        }); 
        
        synBtn.setOnClickListener(new Button.OnClickListener() {//ͬ����ť
			@Override
			public void onClick(View arg0) {
				try {
//					ProductObject cacheObj = getCache();
//					System.out.println(cacheObj);
//					System.out.println(cacheObj.getOrder_list());
//					if(cacheObj.getOrder_list()!=null)
//						System.out.println(cacheObj.getOrder_list());
//					loadView.show();

					 if("1".equals(onlineModle)){//Online Modle
							setCache(ProductDataUtil.getProductObject());
							ToastView toast = new ToastView(ctx,"���߳ɹ�");
						    toast.setGravity(Gravity.CENTER, 0, 0);
						    toast.show();
				    }else if("2".equals(onlineModle)){ //Offline Modle
				    	ProductObject cacheObj = getCache();
				    	List<OrderList> listM=cacheObj.getOrder_list();
				    	if(listM!=null && listM.size()>0){
					    	String jsonStrAll = JsonUtils.getSyncAllJsonStr(cacheObj);
					    	new SyscAllProduct((ProductListActivity)ctx,jsonStrAll).start();
				    	}else{
				    		ToastView toast = new ToastView(ctx,"��ͬ����¼��");
						    toast.setGravity(Gravity.CENTER, 0, 0);
						    toast.show();
				    	}
				    }
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
				 if("1".equals(onlineModle)){//Online Modle
					 	Intent filterIntent = new Intent(ctx,FilterActivity.class);
						int requestCode =0;
						Bundle  Bundle =null;
						startActivityForResult(filterIntent, requestCode, Bundle);
				 }else if("2".equals(onlineModle)){//offline 
					 	Intent loginIntent = new Intent(ctx,MainActivity.class);
					 	startActivity(loginIntent);
					 	((ProductListActivity)ctx).finish();
				 }
			}
		});
        
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View filterView = layoutInflater.inflate(R.layout.filter_panel, null);
        filterDialog = new AlertDialog.Builder(this)
        	.setTitle("����ɸѡ")
        	.setIcon(R.drawable.jsdf_logo)
        	.setView(filterView)
        	.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
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
        	 .setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
		     @Override
		     public void onClick(DialogInterface dialog, int which) {
		      // TODO Auto-generated method stub
		     }
		    }).create();   
    }  
      
    //�����˵���Ӧ����  
    @Override  
    public boolean onContextItemSelected(MenuItem item) {  
//        setTitle("����˳����˵�����ĵ�"+item.getItemId()+"����Ŀ"); 
    	switch (item.getItemId()) {
		case 0: //���Ϊ���û�
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
    	switch (resultCode) { //resultCodeΪ�ش��ı�ǣ�����B�лش�����RESULT_OK
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
     * @<p>��ʾViewList</p>
     * @param productObject
     * @param listItem
     * @param context
     * @param list
     */
	public void drawListView(ProductObject productObject,ArrayList<HashMap<String, Object>> listItem,Context context,ListView list){
		List<OrderList> orderList  = productObject.getOrder_list();
		//���
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
    		if((ProductDataUtil.isNotNull(tmpOrderList.getContent())&&tmpOrderList.getIs_get().equals("1")) ||tmpOrderList.getIs_get().equals("1")){
    			map.put("ItemImage", R.drawable.checkbox_selected);//ͼ����Դ��ID  
    			
    		}else if(ProductDataUtil.isNotNull(tmpOrderList.getContent())){
    			map.put("ItemImage", R.drawable.email_32);//ͼ����Դ��ID 
    		}
    		else if(tmpOrderList.getIs_get().equals("0")){
    			map.put("ItemImage", R.drawable.checkbox_unselect);//ͼ����Դ��ID  
    		}
    		
            map.put("ItemTitle", tmpOrderList.getMarket());  
            map.put("ItemText", tmpOrderList.getFloor()+"F(" +tmpOrderList.getPurchase_code()+ ") "+tmpOrderList.getGoods_attr() +" -"+tmpOrderList.getGoods_number() +"" +
            		"��*P"+tmpOrderList.getGoods_price() + " [" +tmpOrderList.getShort_order_time()+"]");  
            listItem.add(map);  
    	}
    	if(orderList!=null&&(orderList.size()==0)){
    		ToastView toast = new ToastView(ctx,"[�����¼Ϊ��!] ");
		    toast.setGravity(Gravity.CENTER, 0, 0);
		    toast.show();
    	}
    	
    	listItemAdapter= new SimpleAdapter(context,listItem,R.layout.list_items,new String[] {"ItemImage","ItemTitle", "ItemText"},new int[] {R.id.ItemImage,R.id.ItemTitle,R.id.ItemText});  
 	         
 	    //��Ӳ�����ʾ  
 	    list.setAdapter(listItemAdapter);
 	    list.setSelection(selectIndex);
	}
	
	public void reDrawListView(Context context,Map<String,String> condition){
		ProductObject  product = ProductDataUtil.searchProduct(condition);
		drawListView(product,listItem,context,list);
	}
    
	/**
	 * @author Henry
	 * @see <p>����</p>
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
			throw new AppException("������Դ�쳣", e);
		} catch (FileNotFoundException e) {
			throw new AppException("������Դ�쳣�������ļ�������", e);
		} catch (IOException e) {
//			e.printStackTrace();
			throw new AppException("������Դ�쳣�������д�쳣", e);
		}
		finally{
			try {
				if(out!=null)out.close();
			} catch (IOException e) {
				throw new AppException("������Դ�쳣���ر�д�����쳣", e);
			}
		}
	}
	
	/**
	 * @author Henry
	 * @see <p>��ȡ����</p>
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
			throw new AppException("������Դ�쳣", e);
		} catch (FileNotFoundException e) {
			throw new AppException("������Դ�쳣�������ļ�������", e);
		} catch (IOException e) {
			throw new AppException("������Դ�쳣�������д�쳣", e);
		} catch (ClassNotFoundException e) {
			throw new AppException("������Դ�쳣��ת���쳣", e);
		}
		finally{
			try {
				if(in!=null)in.close();
			} catch (IOException e) {
				throw new AppException("������Դ�쳣���رն�ȡ���쳣", e);
			}
		}
		return productObject;
	}

	@Override
	public boolean onDown(MotionEvent arg0) {
		Log.v("onDown", "onDown");
		return false;
	}

	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		Log.v("onFling", "onFling");
		return false;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		Log.v("onLongPress", "onLongPress");
	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		Log.v("onScroll", "onScroll");
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		Log.v("onShowPress", "onShowPress");
	}

	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		Log.v("onSingleTapUp", "onSingleTapUp");
		return false;
	}
    
}
class UpdateListView extends Thread{ //���̸߳��½���
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
            map.put("ItemImage", R.drawable.checkbox_unchecked);//ͼ����Դ��ID  
            map.put("ItemTitle", tmpOrderList.getMarket());  
            map.put("ItemText", tmpOrderList.getFloor()+"F(" +tmpOrderList.getPurchase_code()+ ") "+tmpOrderList.getGoods_attr() +" -"+tmpOrderList.getGoods_number() +"" +
            		"��*P"+tmpOrderList.getGoods_price() + " [" +tmpOrderList.getShort_order_time()+"]");  
            listItem.add(map);  
    	}
    	
    	SimpleAdapter listItemAdapter= new SimpleAdapter(context,listItem,R.layout.list_items,new String[] {"ItemImage","ItemTitle", "ItemText"},new int[] {R.id.ItemImage,R.id.ItemTitle,R.id.ItemText});  
 	         
 	    //��Ӳ�����ʾ  
 	    list.setAdapter(listItemAdapter); 
	}
}
class SendEmail extends Thread{ //SENDEMAIL
	private ProductListActivity context;
	private String email;
	private String msg;
	private String content;
	private String orderId;
	public SendEmail(){
		
	}
	
	public SendEmail(ProductListActivity context,String email,String msg,String content,String orderId){
		this.context = context;
		this.email = email;
		this.msg = msg;
		this.content = content;
		this.orderId = orderId;
	}
	
	@Override
	public void run() {
		String returnStr;
		Message message = Message.obtain();
		try {
			returnStr = Httpservice.sendEmail(email, msg, content, orderId);
			JSONObject  jsonObject = JsonUtils.strConvert2Json(returnStr);
			String returnCode =jsonObject.getString("error");
			String msg = jsonObject.getString("msg");
	    	message.obj = new MessageHandleBean(MessageHandleBean.PRODUCT_SENDEMAIL_CODE,returnCode+"|"+msg);
	    	context.getHandler().sendMessage(message) ;    
		} catch (AppException e) {
			message.obj = new MessageHandleBean(MessageHandleBean.PRODUCT_SENDEMAIL_CODE,"1|"+msg);
	    	context.getHandler().sendMessage(message) ;    
		}
	 	
	}
	
}



class UpdateOneProductStatus extends Thread{ //UPDATE ONE PRODUCT STATUS
	private ProductListActivity context;
	private String recid;
	private String orderId;
	private String getFlag;
	public UpdateOneProductStatus(){
		
	}
	
	public UpdateOneProductStatus(ProductListActivity context,String recid,String orderId,String getFlag){
		this.context = context;
		this.recid = recid;
		this.orderId = orderId;
		this.getFlag = getFlag;
	}
	
	@Override
	public void run() {
		String returnStr;
		Message message = Message.obtain();
		try {
			returnStr = Httpservice.productOneSync(recid,getFlag);
			JSONObject  jsonObject = JsonUtils.strConvert2Json(returnStr);
			String returnCode =jsonObject.getString("error");
	    	message.obj = new MessageHandleBean(MessageHandleBean.PRODUCT_LIST_ONE_CODE,returnCode+"|"+recid+"|"+orderId+"|"+getFlag);
	    	context.getHandler().sendMessage(message) ;    
		} catch (AppException e) {
			message.obj = new MessageHandleBean(MessageHandleBean.PRODUCT_LIST_ONE_CODE,"1|"+e.getMessage()+"|"+orderId+"|"+getFlag);
	    	context.getHandler().sendMessage(message) ;    
		}
	 	
	}
	
}

class GetProductThread extends Thread { //���߳�ȥ��Χ������Դ 
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
		Message message = Message.obtain();
		try {
			productStr= Httpservice.getProductList(Httpservice.TEAM_GET_PRODUCT);
	    	Log.v("SEESIONID", Httpservice.clientSessionId);
			productObject = JsonUtils.convertProductFromJsonStr(productStr);
		} catch (AppException e) {
//			ToastView toast = new ToastView(context, e.getMessage());
//	        toast.setGravity(Gravity.CENTER, 0, 0);
//	        toast.show();
			message.obj = new MessageHandleBean(MessageHandleBean.EXCEPTION_CODE,e.getMessage());
	    	context.getHandler().sendMessage(message) ;
	    	return;
		}
    	
    	message.obj = new MessageHandleBean(MessageHandleBean.PRODUCT_LIST_GETALL_CODE,productObject);
    	context.getHandler().sendMessage(message) ;     	
	}
	
}


class SyscAllProduct extends Thread{ //SYSC ALL PRODUCT STATUS
	private ProductListActivity context;
	private String orderId;
	public SyscAllProduct(){
		
	}
	
	public SyscAllProduct(ProductListActivity context,String orderId){
		this.context = context;
		this.orderId = orderId;
	}
	
	@Override
	public void run() {
		String returnStr;
		Message message = Message.obtain();
		try {
//			Httpservice.LoginGRP("admin", "admin123");
			Httpservice.relogin();
			returnStr = Httpservice.productSync(orderId);
			JSONObject  jsonObject = JsonUtils.strConvert2Json(returnStr);
			String returnCode =jsonObject.getString("error");
			String content = jsonObject.getString("content");
	    	message.obj = new MessageHandleBean(MessageHandleBean.PRODUCT_SYCNALL_CODE,returnCode+"|"+content);
	    	context.getHandler().sendMessage(message) ;    
		} catch (AppException e) {
			message.obj = new MessageHandleBean(MessageHandleBean.PRODUCT_SYCNALL_CODE,"001"+"|"+e.getMessage());
	    	context.getHandler().sendMessage(message) ;    
		}
	 	
	}
	
}
