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
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jsdf.bean.EmailBean;
import com.jsdf.bean.MessageHandleBean;
import com.jsdf.bean.OrderList;
import com.jsdf.bean.ProductObject;
import com.jsdf.exception.AppException;
import com.jsdf.http.Httpservice;
import com.jsdf.json.util.JsonUtils;
import com.jsdf.json.util.LoginThread;
import com.jsdf.utils.CacheEmail;
import com.jsdf.utils.ProductDataUtil;
import com.jsdf.utils.Utils;
import com.jsdf.view.EmailDialog;
import com.jsdf.view.LoadView;
import com.jsdf.view.ReloginDialog;
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
    private ReloginDialog reloginWin = null;
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
    private int currentPosition;
    private int scrolledX;
    private int scrolledY;
    //list header
    private LinearLayout mHeaderLinearLayout = null;
    private TextView mHeaderTextView = null;
    private TextView mHeaderUpdateText = null;
    private ImageView mHeaderPullDownImageView = null;
    private ImageView mHeaderReleaseDownImageView = null;
    private ProgressBar mHeaderProgressBar = null;
    private int mHeaderHeight;
    private int mCurrentScrollState;
    private final static int NONE_PULL_REFRESH = 0;   //����״̬
    private final static int ENTER_PULL_REFRESH = 1;  //��������ˢ��״̬
    private final static int OVER_PULL_REFRESH = 2;   //��������ˢ��״̬
    private final static int EXIT_PULL_REFRESH = 3;     //���ֺ󷴵������״̬
    private int mPullRefreshState = 0;                         //��¼ˢ��״̬
    private float mDownY;
    private float mMoveY;
    private Resources resource;
    //relogin
    private Button loginconform;
    private Button loginconcel;
	private EditText userName;
	private EditText password;
	String name="";
	String psd="";
    //relogin
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); 
		resource = (Resources) getBaseContext().getResources();
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
		
		//relogin panel init begin
		reloginWin= new ReloginDialog(this,R.style.ReloginDialog);
		
		//relogin panel init end 
		
	    handler=new Handler(){
	    	public void handleMessage(Message msg){
	    		MessageHandleBean handleBean = (MessageHandleBean)msg.obj;
	    		if(MessageHandleBean.PRODUCT_LIST_GETALL_CODE.equals(handleBean.getMsgType())){
		    		ProductObject productObject=(ProductObject)handleBean.getData();//obj��һ����String�࣬�����Ǳ���࣬���û������Ӧ��
		    		
		    		if(1== productObject.getError()){
		    			ToastView toast = new ToastView(ctx,"��ȡ����ʧ��  �����µ�¼");
	    			    toast.setGravity(Gravity.CENTER, 0, 0);
	    			    toast.show();
		    		}else{
			    		ProductDataUtil.setProductObject(productObject);
			    		drawListView(ProductDataUtil.getProductObject(),listItem,ctx,list);
		    		}
//		    		loadView.hide();
	    		}else if(MessageHandleBean.PRODUCT_LIST_ONE_CODE.equals(handleBean.getMsgType())){
	    			String str = (String)handleBean.getData();
	    			String[] reutunArray = str.split("\\|");
//	    			loadView.hide();
	    			if(reutunArray[0].equals("0")){
	    				ProductDataUtil.updateIsGetStatus(reutunArray[2], reutunArray[3]);
	    				if(conditionCurrent==null){ //ֻ��ʾδ�û�����
	    					conditionCurrent = new HashMap<String,String>();
//	    					conditionCurrent.put(ProductDataUtil.ISGET_NAME, "0"); 
	    				}else{
//	    					conditionCurrent.put(ProductDataUtil.ISGET_NAME, "0"); 
	    				}
	    				reDrawListView(ctx,conditionCurrent);
	    			}
	    			else if(reutunArray[0].equals("1")){
	    				ToastView toast = new ToastView(ctx,"��ȡ����ʧ��  �����µ�¼");
	    			    toast.setGravity(Gravity.CENTER, 0, 0);
	    			    toast.show();
	    			}
	    			else{
	    				ToastView toast = new ToastView(ctx,reutunArray[1]);
	    			    toast.setGravity(Gravity.CENTER, 0, 0);
	    			    toast.show();
	    			}
	    		}else if(MessageHandleBean.PRODUCT_SENDEMAIL_CODE.equals(handleBean.getMsgType())){
	    			String str = (String)handleBean.getData();
	    			String[] reutunArray = str.split("\\|");
//	    			loadView.hide();
	    			emailDialog.hide();
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
	    			    
	    			}
	    			else if(reutunArray[0].equals("1")){
	    				ToastView toast = new ToastView(ctx,"�����ʼ�ʧ��  �����µ�¼");
	    			    toast.setGravity(Gravity.CENTER, 0, 0);
	    			    toast.show();
	    			}
	    			else{
	    				ToastView toast = new ToastView(ctx,"�����ʼ�ʧ�ܣ�"+reutunArray[1]);
	    			    toast.setGravity(Gravity.CENTER, 0, 0);
	    			    toast.show();
	    			}
	    		}else if(MessageHandleBean.PRODUCT_SYCNALL_CODE.equals(handleBean.getMsgType())){
	    			String str = (String)handleBean.getData();
	    			String[] reutunArray = str.split("\\|");
	    			loadView.hide();
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
	    			}else if(reutunArray[0].equals("1")){
	    				ToastView toast = new ToastView(ctx,"��ȡ����ʧ��  �����µ�¼");
	    			    toast.setGravity(Gravity.CENTER, 0, 0);
	    			    toast.show();
	    			}
	    			else{
	    				ToastView toast = new ToastView(ctx,"ͬ��ʧ��"+reutunArray[1]);
	    			    toast.setGravity(Gravity.CENTER, 0, 0);
	    			    toast.show();
	    			}
	    		}else if(MessageHandleBean.EXCEPTION_CODE.equals(handleBean.getMsgType())){
	    			ToastView toast = new ToastView(ctx,(String)handleBean.getData());
    			    toast.setGravity(Gravity.CENTER, 0, 0);
    			    toast.show();
	    		}
	    		else if(MessageHandleBean.RELOGIN_CODE.equals(handleBean.getMsgType())){
	    			String[] str = (String[])handleBean.getData();
	    			loadView.hide();
	    			if(str[0].equals("0")){
						try {
							Utils.setProperties(Utils.USERNAME, name);
							Utils.setProperties(Utils.PASSWORD, psd);
						} catch (AppException e) {
							ToastView toast = new ToastView(ctx,"д�û���Ϣ����"+e.getMessage());
		    			    toast.setGravity(Gravity.CENTER, 0, 0);
		    			    toast.show();
						}
	    				ToastView toast = new ToastView(ctx,"��½�ɹ�");
	    			    toast.setGravity(Gravity.CENTER, 0, 0);
	    			    toast.show();
	    			    reloginWin.dismiss();
	    			}else{
	    				ToastView toast = new ToastView(ctx,"��½ʧ��:"+str[1]);
	    			    toast.setGravity(Gravity.CENTER, 0, 0);
	    			    toast.show();
	    			}
	    		}
	    		loadView.hide();
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
	    	loadView.show();
	    	productListTitle.setText(R.string.onlineModle);
	    	synBtn.setText(R.string.product_cache);
	    }else if("2".equals(onlineModle)){ //Offline Modle
	    	try {
	    		ProductObject offObjct = getCache();
				drawListView(offObjct,listItem,ctx,list);
				ProductDataUtil.setProductObject(offObjct);
				productListTitle.setText(R.string.offlineModle);
				synBtn.setText(R.string.upload_btn);
				filterBtn.setText(R.string.download_btn);
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
	            	if(selectOrder!=null){
		            	String getFlag = selectOrder.getIs_get();
						if("1".equals(onlineModle)){
							if("0".equals(getFlag)){//�û�
								 loadView.show();
								ProductDataUtil.updateIsGetStatus(selectOrder.getRec_id(), "1");
							    new UpdateOneProductStatus((ProductListActivity)ctx,selectOrder.getRec_id(),selectOrder.getOrder_id(),"1").start();
							   
							}else{//ȡ���û�
								loadView.show();
								ProductDataUtil.updateIsGetStatus(selectOrder.getRec_id(), "0");
							    new UpdateOneProductStatus((ProductListActivity)ctx,selectOrder.getRec_id(),selectOrder.getOrder_id(),"0").start();
							}
						}else if ("2".equals(onlineModle)){
							try{
								if("0".equals(getFlag)){//�û�
								    ProductDataUtil.updateIsGetStatus(selectOrder.getRec_id(), "1");
								    
								}else{
									ProductDataUtil.updateIsGetStatus(selectOrder.getRec_id(), "0");
								}
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
            	}
            	longClickFlag=false;
            }  
        });  
       
//       list.setOnTouchListener(new OnTouchListener(){
//
//		@Override
//		public boolean onTouch(View arg0, MotionEvent ev) {
//			 switch (ev.getAction()) {
//		        case MotionEvent.ACTION_DOWN:
//		            //���°���λ��
//		            //�ı�
//		            mDownY = ev.getY();
//		            break;
//		        case MotionEvent.ACTION_MOVE:
//		            //�ƶ�ʱ��ָ��λ��
//		            mMoveY = ev.getY();
//		            if (mPullRefreshState == OVER_PULL_REFRESH) {
//		                //ע�������mDownY��onScroll�ĵڶ���else�б��ı���
//		                mHeaderLinearLayout.setPadding(mHeaderLinearLayout.getPaddingLeft(),
//		                        (int)((mMoveY - mDownY)/3), //1/3�����ۿ�
//		                        mHeaderLinearLayout.getPaddingRight(),
//		                        mHeaderLinearLayout.getPaddingBottom());
//		            }
//		            break;
//		        case MotionEvent.ACTION_UP:
//		            //when you action up, it will do these:
//		            //1. roll back util header topPadding is 0
//		            //2. hide the header by setSelection(1)
//		            if (mPullRefreshState == OVER_PULL_REFRESH || mPullRefreshState == ENTER_PULL_REFRESH) {
//		                new Thread() {
//		                    public void run() {
//		                        Message msg;
//		                        while(mHeaderLinearLayout.getPaddingTop() > 1) {
////		                            msg = mHandler.obtainMessage();
////		                            msg.what = REFRESH_BACKING;
////		                            mHandler.sendMessage(msg);
//		                            try {
//		                                sleep(5);//��һ�㷴������һ���Ӿ͵���ȥ��
//		                            } catch (InterruptedException e) {
//		                                e.printStackTrace();
//		                            }
//		                        }
////		                        msg = mHandler.obtainMessage();
//		                        if (mPullRefreshState == OVER_PULL_REFRESH) {
////		                            msg.what = REFRESH_BACED;//����������ɣ���������
//		                        } else {
////		                            msg.what = REFRESH_RETURN;//δ�ﵽˢ�½��ޣ�ֱ�ӷ���
//		                        }
////		                        mHandler.sendMessage(msg);
//		                    };
//		                }.start();
//		            }
//		            break;
//		    }
//		    return false;
//		}
//    	   
//       }); 
//        
       list.setOnScrollListener(new OnScrollListener(){
//
//			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//				if (mCurrentScrollState ==SCROLL_STATE_TOUCH_SCROLL
//			            && firstVisibleItem == 0
//			            && (mHeaderLinearLayout.getBottom() >= 0 && mHeaderLinearLayout.getBottom() < mHeaderHeight)) {
//			        //�����ҽ���������ˢ��״̬
//			        if (mPullRefreshState == NONE_PULL_REFRESH) {
//			            mPullRefreshState = ENTER_PULL_REFRESH;
//			        }
//			    } else if (mCurrentScrollState ==SCROLL_STATE_TOUCH_SCROLL
//			            && firstVisibleItem == 0
//			            && (mHeaderLinearLayout.getBottom() >= mHeaderHeight)) {
//			        //�����ﵽ���ޣ���������ˢ��״̬
//			        if (mPullRefreshState == ENTER_PULL_REFRESH || mPullRefreshState == NONE_PULL_REFRESH) {
//			            mPullRefreshState = OVER_PULL_REFRESH;
//			            //�����ǽ�������ˢ��״̬��Ҫ����һ����ʾ�ı�
//			            mDownY = mMoveY;//���ں������������Ч��
//			            mHeaderTextView.setText("����ˢ��");
//			            mHeaderPullDownImageView.setVisibility(View.GONE);
//			            mHeaderReleaseDownImageView.setVisibility(View.VISIBLE);
//			        }
//			    } else if (mCurrentScrollState ==SCROLL_STATE_TOUCH_SCROLL && firstVisibleItem != 0) {
//			        //��ˢ����
//			        if (mPullRefreshState == ENTER_PULL_REFRESH) {
//			            mPullRefreshState = NONE_PULL_REFRESH;
//			        }
//			    } else if (mCurrentScrollState == SCROLL_STATE_FLING && firstVisibleItem == 0) {
//			        //�ɻ�״̬��������ʾ��header��Ҳ����Ӱ�������ķɻ�
//			        //ֻ����������²ž���λ��
//			        if (mPullRefreshState == NONE_PULL_REFRESH) {
//			        	setListSelection(selectIndex);
//			        }
//			    }else if (mCurrentScrollState == SCROLL_STATE_TOUCH_SCROLL
//			            && firstVisibleItem == 0
//			            && (mHeaderLinearLayout.getBottom() >= mHeaderHeight)) {
//			        //�����ﵽ���ޣ���������ˢ��״̬
//			        if (mPullRefreshState == ENTER_PULL_REFRESH || mPullRefreshState == NONE_PULL_REFRESH) {
//			            mPullRefreshState = OVER_PULL_REFRESH;
//			            mDownY = mMoveY; //Ϊ����1/3�ۿ�Ч����¼��ʼλ��
//			            mHeaderTextView.setText("����ˢ��");//��ʾ����ˢ��
//			            mHeaderPullDownImageView.setVisibility(View.GONE);//����"����ˢ��"
//			            mHeaderReleaseDownImageView.setVisibility(View.VISIBLE);//��ʾ���ϵļ�ͷ
//			        }
//			    }
			}
//	
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
//				// TODO Auto-generated method stub
//				 mCurrentScrollState = scrollState;
				 // ������ʱ���浱ǰ��������λ��  
			        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {  
			        	 currentPosition = list.getFirstVisiblePosition(); 
//			        	 scrolledX = list.getScrollX();  
//			             scrolledY = list.getScrollY();  
			        }
//			        scrolledX = list.getScrollX();  
//		            scrolledY = list.getScrollY();  
			}
//    	   
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
            	emailContent.findFocus();
            	emailOk.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						String emailMsg = emailContent.getText().toString();
						String emailContext = dbClickSelectOrder.getFloor()+"F(" +dbClickSelectOrder.getPurchase_code()+ ") "+dbClickSelectOrder.getGoods_attr() +" -"+dbClickSelectOrder.getGoods_number() +"" +
			            		"��*P"+dbClickSelectOrder.getGoods_price() + " [" +dbClickSelectOrder.getShort_order_time()+"]";
		            	ProductDataUtil.updateOnEmailContent(dbClickSelectOrder.getRec_id(), emailMsg);
		            	if("1".equals(onlineModle)){
		            		new SendEmail((ProductListActivity)ctx,dbClickSelectOrder.getEmail(),URLEncoder.encode(emailMsg),URLEncoder.encode(emailContext),dbClickSelectOrder.getOrder_id()).start();
		            		loadView.show();
		            	}
		            	else if ("2".equals(onlineModle)){
		            		CacheEmail.putEmail(dbClickSelectOrder.getEmail(), URLEncoder.encode(emailMsg), URLEncoder.encode(emailContext), dbClickSelectOrder.getOrder_id());
		            		emailDialog.hide();
		            	}
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
		            	ProductDataUtil.updateOnEmailContent(dbClickSelectOrder.getRec_id(), emailMsg);
		            	if("1".equals(onlineModle)){
		            		new SendEmail((ProductListActivity)ctx,dbClickSelectOrder.getEmail(),URLEncoder.encode(emailMsg),URLEncoder.encode(emailContext),dbClickSelectOrder.getOrder_id()).start();
		            		loadView.show();
		            	}
		            	else if ("2".equals(onlineModle)){
		            		CacheEmail.putEmail(dbClickSelectOrder.getEmail(), URLEncoder.encode(emailMsg), URLEncoder.encode(emailContext), dbClickSelectOrder.getOrder_id());
		            		emailDialog.hide();
		            	}
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
		            	ProductDataUtil.updateOnEmailContent(dbClickSelectOrder.getRec_id(), emailMsg);
		            	if("1".equals(onlineModle)){
		            		new SendEmail((ProductListActivity)ctx,dbClickSelectOrder.getEmail(),URLEncoder.encode(emailMsg),URLEncoder.encode(emailContext),dbClickSelectOrder.getOrder_id()).start();
		            		loadView.show();
		            	}else if ("2".equals(onlineModle)){
		            		CacheEmail.putEmail(dbClickSelectOrder.getEmail(), URLEncoder.encode(emailMsg), URLEncoder.encode(emailContext), dbClickSelectOrder.getOrder_id());
		            		emailDialog.hide();
		            	}
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
		            	ProductDataUtil.updateOnEmailContent(dbClickSelectOrder.getRec_id(), emailMsg);
		            	if("1".equals(onlineModle)){
		            		new SendEmail((ProductListActivity)ctx,dbClickSelectOrder.getEmail(),URLEncoder.encode(emailMsg),URLEncoder.encode(emailContext),dbClickSelectOrder.getOrder_id()).start();
		            		loadView.show();
		            	}else if ("2".equals(onlineModle)){
		            		CacheEmail.putEmail(dbClickSelectOrder.getEmail(), URLEncoder.encode(emailMsg), URLEncoder.encode(emailContext), dbClickSelectOrder.getOrder_id());
		            		emailDialog.hide();
		            	}
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
        
       productListTitle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if("2".equals(onlineModle)){//offline 
				 	Intent filterIntent = new Intent(ctx,FilterActivity.class);
//				 	startActivity(filterIntent);
					int requestCode =0;
					Bundle  Bundle =null;
					startActivityForResult(filterIntent, requestCode, Bundle);
				 	overridePendingTransition(R.anim.enteralpha, R.anim.exitalpha); 
//				 	((ProductListActivity)ctx).finish();
				}
			}
	   });
       
        synBtn.setOnClickListener(new Button.OnClickListener() {//ͬ����ť
			@Override
			public void onClick(View arg0) {
				try {
//					ProductObject cacheObj = getCache();
//					System.out.println(cacheObj);
//					System.out.println(cacheObj.getOrder_list());
//					if(cacheObj.getOrder_list()!=null)
//						System.out.println(cacheObj.getOrder_list());
					loadView.show();

					 if("1".equals(onlineModle)){//Online Modle
							setCache(ProductDataUtil.getProductObject());
							ToastView toast = new ToastView(ctx,"���߳ɹ�");
						    toast.setGravity(Gravity.CENTER, 0, 0);
						    toast.show();
						    onlineModle="2";
						    synBtn.setText(R.string.upload_btn);
							filterBtn.setText(R.string.download_btn);
							productListTitle.setText(R.string.offlineModle);
							loadView.hide();
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
				    	
				    	List<EmailBean> emailList = CacheEmail.getCache();
				    	ToastView toast = new ToastView(ctx,"�����ʼ�������"+emailList.size());
					    toast.setGravity(Gravity.CENTER, 0, 0);
					    toast.show();
				    	if(emailList!=null && emailList.size()>0){
				    		for(int j =0 ; j<emailList.size();j++){
				    			EmailBean tmpEmailB = emailList.get(j);
				    			Thread.currentThread().sleep(100);
					    		new SendEmail((ProductListActivity)ctx,tmpEmailB.getEmail(),tmpEmailB.getEmailMsg(),tmpEmailB.getEmailContext(),tmpEmailB.getOrder_id()).start();
				    		}
				    		CacheEmail.clearCache();
				    	}else{
				    		ToastView toast2 = new ToastView(ctx,"�������ʼ���");
						    toast2.setGravity(Gravity.CENTER, 0, 0);
						    toast2.show();
				    	}
				    	loadView.hide();
				    }
				} catch (AppException e) {
					ToastView toast = new ToastView(ctx,e.getMessage());
				    toast.setGravity(Gravity.CENTER, 0, 0);
				    toast.show();
				} catch (InterruptedException e) {
					ToastView toast = new ToastView(ctx,"�����ʼ����󣡣�");
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
//					 	Intent loginIntent = new Intent(ctx,MainActivity.class);
//					 	startActivity(loginIntent);
//					 	((ProductListActivity)ctx).finish();
					   new GetProductThread((ProductListActivity)ctx).start(); //ͨ�����̻߳�ȡ�������ݣ��������߳�
					   loadView.show();
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
    
    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
     
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
                    MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0,
                    MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
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
//		if(mHeaderLinearLayout!=null)list.removeHeaderView(mHeaderLinearLayout);
		
//		mHeaderLinearLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.refresh_list_header, null);
//        list.addHeaderView(mHeaderLinearLayout);
        
		
    	SimpleAdapter listItemAdapter= new SimpleAdapter(context,listItem,R.layout.list_items,new String[] {"ItemImage","ItemTitle", "ItemText"},new int[] {R.id.ItemImage,R.id.ItemTitle,R.id.ItemText});  
//    	listItemAdapter.notifyDataSetChanged();
//    	listItemAdapter.notifyDataSetInvalidated();
    	list.setAdapter(listItemAdapter);
    	ProductDataUtil.clearCacheListViewData();
    	
    	ProductDataUtil.cacheListViewData(0, null);
//    	map = new HashMap<String, Object>();  
////		map.put("ItemImage", R.drawable.checkbox_selected);//ͼ����Դ��ID  
//        map.put("ItemTitle", "");  
//        map.put("ItemText", "ˢ���С�����");  
    	
//        mHeaderTextView = (TextView) findViewById(R.id.refresh_list_header_text);
//        mHeaderUpdateText = (TextView) findViewById(R.id.refresh_list_header_last_update);
//        mHeaderPullDownImageView = (ImageView) findViewById(R.id.refresh_list_header_pull_down);
//        mHeaderReleaseDownImageView = (ImageView) findViewById(R.id.refresh_list_header_release_up);
//        mHeaderProgressBar = (ProgressBar) findViewById(R.id.refresh_list_header_progressbar);
//        measureView(mHeaderLinearLayout);
//        mHeaderHeight = mHeaderLinearLayout.getMeasuredHeight();
    	
    	
//        listItem.add(map);
    	
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
// 	    list.setSelection(selectIndex);
 	    list.setSelection(currentPosition);
// 	    list.scrollTo(scrolledX, scrolledY);
	}
	
	public void reDrawListView(Context context,Map<String,String> condition){
		ProductObject  product = ProductDataUtil.searchProduct(condition);
		drawListView(product,listItem,context,list);
		
	}
	
	public void reDrawListView2(Context context,Map<String,String> condition){
		ProductObject  product = ProductDataUtil.searchProduct(condition);
//		drawListView(product,listItem,context,list);
		listItemAdapter = (SimpleAdapter)list.getAdapter();
		
		List<OrderList> orderList  = product.getOrder_list();
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
//		listItemAdapter
		listItemAdapter.notifyDataSetChanged();
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
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		 PackageManager pm = getPackageManager();    
	        ResolveInfo homeInfo =   
	            pm.resolveActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0);   
	        if (keyCode == KeyEvent.KEYCODE_BACK) {  
	            ActivityInfo ai = homeInfo.activityInfo;    
	            Intent startIntent = new Intent(Intent.ACTION_MAIN);    
	            startIntent.addCategory(Intent.CATEGORY_LAUNCHER);    
	            startIntent.setComponent(new ComponentName(ai.packageName, ai.name));    
	            startActivitySafely(startIntent);    
	            return true;    
	        } 
	        else if(keyCode == KeyEvent.KEYCODE_MENU){
	        	super.openOptionsMenu();
	        	return true;
	        }
	        else {
	            return super.onKeyDown(keyCode, event);  
	        }
		
	}
	
	@Override
	 public void onOptionsMenuClosed(Menu menu) {
		super.onOptionsMenuClosed(menu);
	 }
	
	@Override      

	public void openOptionsMenu() {        
	   // TODO Auto-generated method stub    
	   super.openOptionsMenu();   
	}
	
    @Override   
    public boolean onCreateOptionsMenu(Menu menu) {    
	      // TODO Auto-generated method stub          
		    super.onCreateOptionsMenu(menu);  
		    int group1 = 1;        
		    int gourp2 = 2;    
		    menu.add(group1, 1, 1, "��������"); 
		    menu.add(group1, 2, 2, "��������"); 
	        menu.add(group1, 3, 3, "ע���û�");
	        menu.add(group1, 4, 4, "���µ�¼");
	        menu.add(group1, 5, 5, "�˳�����");  
	        return true;   
	} 
    
    @Override     
    public boolean onOptionsItemSelected(MenuItem item) {   
          // TODO Auto-generated method stub    
    	ToastView toast =null;	
       switch (item.getItemId()) {   
	     case 1: // do something here         
	    	 	loadView.show();
	    	 	new GetProductThread(this).start(); //ͨ�����̻߳�ȡ�������ݣ��������߳�
	    	 	toast = new ToastView(ctx,"���߳ɹ�");
			    toast.setGravity(Gravity.CENTER, 0, 0);
			    toast.show();
			    onlineModle="1";
		    	synBtn.setText(R.string.product_cache);
		    	filterBtn.setText(R.string.product_filter);
				productListTitle.setText(R.string.onlineModle);
	           break;       
	     case 2: // do something here       
				try {
					setCache(ProductDataUtil.getProductObject());
				} catch (AppException e1) {
					toast = new ToastView(ctx,"����ʧ��:"+e1.getMessage());
				    toast.setGravity(Gravity.CENTER, 0, 0);
				    toast.show();
				}
	    	 	toast = new ToastView(ctx,"���߳ɹ�");
			    toast.setGravity(Gravity.CENTER, 0, 0);
			    toast.show();
			    onlineModle="2";
			    synBtn.setText(R.string.upload_btn);
				filterBtn.setText(R.string.download_btn);
				productListTitle.setText(R.string.offlineModle);
	    	 break;
	     case 3: //logoff
	    	 try{
					Utils.setProperties(Utils.USERNAME, "");
					Utils.setProperties(Utils.PASSWORD, "");
					Utils.setProperties(Utils.AUTOLOGIN,"");
					Utils.setProperties(Utils.REMEBERME,"");
					Utils.setProperties("SESSIONID", "");
					toast = new ToastView(ctx, "ע���ɹ�");
			        toast.setGravity(Gravity.CENTER, 0, 0);
			        toast.show();
				}catch(AppException e){
					toast = new ToastView(ctx, "ע������"+e.getMessage());
			        toast.setGravity(Gravity.CENTER, 0, 0);
			        toast.show();
				}       
    	 	break;
	     case 4: //  relogin
	    	 	reloginWin.show(); 
	    	 	loginconform = (Button) reloginWin.findViewById(R.id.reloginConfirm);
	    		loginconcel = (Button) reloginWin.findViewById(R.id.reLoginCancel);
	    		userName = (EditText) reloginWin.findViewById(R.id.relogin_name);
	    		password = (EditText) reloginWin.findViewById(R.id.relogin_password);
	    		
	    		final String usern=resource.getString(R.string.user_name_cannot_be_empty);
	    	    final String pass=resource.getString(R.string.password_cannot_be_empty);
	    		loginconform.setOnClickListener(new OnClickListener(){
	    			@Override
	    			public void onClick(View arg0) {
	    				name = userName.getText().toString();
	    				psd = password.getText().toString();
	    				
	    				if("".equals(name)) {				
	    					ToastView toast = new ToastView(ctx, usern);
	    			        toast.setGravity(Gravity.CENTER, 0, 0);
	    			        toast.show();
	    				} else if("".equals(psd)) {				
	    					ToastView toast = new ToastView(ctx, pass);
	    			        toast.setGravity(Gravity.CENTER, 0, 0);
	    			        toast.show();
	    				} else {
	    					new LoginThread(name, psd,(ProductListActivity)ctx).start();
	    					loadView.show();
	    				}
	    			}
	    		});
	    		loginconcel.setOnClickListener(new OnClickListener(){
	    			@Override
	    			public void onClick(View arg0) {
	    				// TODO Auto-generated method stub
	    				reloginWin.dismiss();
	    			}
	    		});
	    	 	break;	 	
	     case 5: // exit app       
	    	 this.finish();
	    	 break;  	 	
	     default:   
	            return super.onOptionsItemSelected(item);   
          } 
       return true; 
    }  
    
	
	public void  setListSelection(int index){
		list.setSelection(selectIndex);
	}
	
	private void startActivitySafely(Intent intent) {    
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);    
        try {    
            startActivity(intent);    
        } catch (ActivityNotFoundException e) {    
            Toast.makeText(this, "null",    
                    Toast.LENGTH_SHORT).show();    
        } catch (SecurityException e) {    
            Toast.makeText(this, "null",    
                    Toast.LENGTH_SHORT).show();     
        }    
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
			Log.v("sendEmail returnstr:", returnStr);
			JSONObject  jsonObject = JsonUtils.strConvert2Json(returnStr);
			String returnCode =jsonObject.getString("error");
			String msg = jsonObject.getString("msg");
	    	message.obj = new MessageHandleBean(MessageHandleBean.PRODUCT_SENDEMAIL_CODE,returnCode+"|"+msg);
	    	context.getHandler().sendMessage(message) ;    
		} catch (AppException e) {
			message.obj = new MessageHandleBean(MessageHandleBean.PRODUCT_SENDEMAIL_CODE,"2|"+msg);
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
	    	Log.v("GETALL PRODUCT:", productStr);
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
