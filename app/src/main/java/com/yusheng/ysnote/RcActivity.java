package com.yusheng.ysnote;


import java.util.ArrayList;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import static com.yusheng.ysnote.Constant.*;
import static com.yusheng.ysnote.DBUtil.*;

@SuppressLint("ResourceAsColor") public class RcActivity extends IatBasicActivity
{
	String[] defultType=new String[]{"Meeting","Homework","Examination"};//软件的三个不能删除的默认类型
	Dialog dialogSetRange;//日程查找时设置日期起始范围的对话框
	Dialog dialogSetDatetime;//新建或修改日程时设置日期和时间的对话框
	Dialog dialogSchDelConfirm;//删除日程时的确认对话框
	Dialog dialogCheck;//主界面中查看日程详细内容的对话框
	Dialog dialogAllDelConfirm;//删除全部过期日程时的确认对话框
	Dialog dialogAbout;//关于对话框
	static ArrayList<String> alType=new ArrayList<String>();//存储所有日程类型的arraylist
	static ArrayList<Schedule> alSch=new ArrayList<Schedule>();//存储所有schedule对象的ArrayList
	Schedule schTemp;//临时的schedule
	ArrayList<Boolean> alSelectedType=new ArrayList<Boolean>();//记录查找界面中类型前面checkbox状态的
	String rangeFrom=getNowDateString();//查找日程时设置的起始日期，默认当前日期
	String rangeTo=rangeFrom;//查找日程时设置的终止日期，默认当前日期
	Layout curr=null;//记录当前界面的枚举类型
	WhoCall wcSetTimeOrAlarm;//用来判断调用时间日期对话框的按钮是设置时间还是设置闹钟,以便更改对话框中的一些控件该设置为visible还是gone
	WhoCall wcNewOrEdit;//用来判断调用日程编辑界面的是新建日程按钮还是在修改日程按钮，以便设置对应的界面标题
	int sel=0;
	/*临时记录新建日程界面里的类型spinner的position，因为设置时间的对话框cancel后
	     回到新建日程界面时会刷新所有控件，spinner中以选中的项目也会回到默认*/ 
	Handler hd=new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
				case 0:
					gotoMain();
				break;
			}
		}
	};
	@Override    
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//无标题
        goToWelcomeView();
    }
    //欢迎界面
    public void goToWelcomeView()
    {
    	MySurfaceView mview=new MySurfaceView(this);
    	getWindow().setFlags//全屏
    	(
    			WindowManager.LayoutParams.FLAG_FULLSCREEN, 
    			WindowManager.LayoutParams.FLAG_FULLSCREEN
    	);
    	setContentView(mview);
    	curr=Layout.WELCOME_VIEW;
    }
    //===================================主界面start===========================================
    public void gotoMain()//初始化主界面
    {
    	getWindow().setFlags
    	(//非全屏
    			WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, 
    			WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN
    	);	
    	setContentView(R.layout.main);
    	curr=Layout.MAIN;
    	sel=0;
    	
    	final ArrayList<Boolean> alIsSelected=new ArrayList<Boolean>();//记录ListView中哪项选中了的标志位
    	
    	final ImageButton bEdit=(ImageButton)findViewById(R.id.ibmainEdit);//修改日程按钮
    	final ImageButton bCheck=(ImageButton)findViewById(R.id.ibmainCheck);//查看日程详细内容的按钮
    	final ImageButton bDel=(ImageButton)findViewById(R.id.ibmainDel);//删除当前选中日程的按钮
    	ImageButton bNew=(ImageButton)findViewById(R.id.ibmainNew);//新建日程按钮
    	ImageButton bDelAll=(ImageButton)findViewById(R.id.ibmainDelAll);//删除所有过期日程按钮
    	ImageButton bSearch=(ImageButton)findViewById(R.id.ibmainSearch);//查找日程按钮
    	final ListView lv=(ListView)findViewById(R.id.lvmainSchedule);//日程列表
        
        bCheck.setEnabled(false);//这三个按钮分别为主界面的日程查看、日程修改、日程删除,
    	bEdit.setEnabled(false);//默认设为不可用状态
    	bDel.setEnabled(false);
        
    	alSch.clear();//从数据库读取之前清空存储日程的arraylist
		loadSchedule(this);//从数据库中读取日程
		loadType(this);//从数据库中读取类型
		
        if(alSch.size()==0)//如果没有任何日程，则删除全部过期日程按钮设为禁用
        {
        	bDelAll.setEnabled(false);
        }
        else
        {
        	bDelAll.setEnabled(true);
        }
        
        alIsSelected.clear();
    	
        for(int i=0;i<alSch.size();i++)//全部设置为false，即没有一项选中
    	{
    		alIsSelected.add(false);
    	}
        
        //以下是ListView设置
        lv.setAdapter
        (
        		new BaseAdapter()
        		{
					@Override
					public int getCount() 
					{
						return alSch.size();
					}
					@Override
					public Object getItem(int position) 
					{
						return alSch.get(position);
					}
					@Override
					public long getItemId(int position) 
					{
						return 0;
					}
					@Override
					public View getView(int position, View convertView, ViewGroup parent) 
					{
						LinearLayout ll=new LinearLayout(RcActivity.this);
						ll.setOrientation(LinearLayout.VERTICAL);
						ll.setPadding(5, 5, 5, 5);
						
						LinearLayout llUp=new LinearLayout(RcActivity.this);
						llUp.setOrientation(LinearLayout.HORIZONTAL);
						LinearLayout llDown=new LinearLayout(RcActivity.this);
						llDown.setOrientation(LinearLayout.HORIZONTAL);
						
						//ListView中日期TextView
						TextView tvDate=new TextView(RcActivity.this);
						tvDate.setText(alSch.get(position).getDate1()+"   ");
						tvDate.setTextSize(17);
						tvDate.setTextColor(Color.parseColor("#129666"));
						llUp.addView(tvDate);
						
						//ListView时间TextView
						TextView tvTime=new TextView(RcActivity.this);
						tvTime.setText(alSch.get(position).timeForListView());
						tvTime.setTextSize(17);
						tvTime.setTextColor(Color.parseColor("#925301"));
						llUp.addView(tvTime);
						
						//若日程已过期，则日期和时间颜色、背景色设置为过期的颜色
						if(alSch.get(position).isPassed())
						{
							tvDate.setTextColor(getResources().getColor(R.color.passedschtext));
							tvTime.setTextColor(getResources().getColor(R.color.passedschtext));
							ll.setBackgroundColor(getResources().getColor(R.color.passedschbg));
						}
						//如果该项被选中了，背景色设置为选中的背景色
						if(alIsSelected.get(position))
						{
							ll.setBackgroundColor(getResources().getColor(R.color.selectedsch));
						}
						//如果有闹钟，则加上闹钟的图标
						if(alSch.get(position).getAlarmSet())
						{
							ImageView iv=new ImageView(RcActivity.this);
							iv.setImageDrawable(getResources().getDrawable(R.drawable.alarm));
							iv.setLayoutParams(new LayoutParams(20, 20));
							llUp.addView(iv);
						}
						//日程类型TextView
						TextView tvType=new TextView(RcActivity.this);
						tvType.setText(alSch.get(position).typeForListView());
						tvType.setTextSize(17);
						tvType.setTextColor(Color.parseColor("#b20000"));
						llDown.addView(tvType);
						//日程标题TextView
						TextView tvTitle=new TextView(RcActivity.this);
						tvTitle.setText(alSch.get(position).getTitle());
						tvTitle.setTextSize(17);
						tvTitle.setTextColor(Color.parseColor("#000000"));
						llDown.addView(tvTitle);
						ll.addView(llUp);
						ll.addView(llDown);
						return ll;
					}
		        }
        );
        lv.setOnItemClickListener
        (
        		new OnItemClickListener()
        		{
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
					{
						bCheck.setEnabled(true);//这三个按钮分别为主界面的日程查看、日程修改、日程删除,
				    	bEdit.setEnabled(true);//如果用户在日程列表中选中了某个日程时，设为可用状态
				    	bDel.setEnabled(true);
				    	
						schTemp=alSch.get(arg2);//选中该项目时，把该项目对象赋给schTemp
						
						//把标志位全部设为false后再把当前选中项的对应的标志位设为true
						for(int i=0;i<alIsSelected.size();i++)
						{
							alIsSelected.set(i,false);
						}
						alIsSelected.set(arg2,true);
					}
		        }
        );
        
        //bNew设置
        bNew.setOnClickListener
        (
        		new OnClickListener()
		        {
		
					@Override
					public void onClick(View v) {
						Calendar c=Calendar.getInstance();
						int t1=c.get(Calendar.YEAR);
						int t2=c.get(Calendar.MONTH)+1;
						int t3=c.get(Calendar.DAY_OF_MONTH);
						schTemp=new Schedule(t1,t2,t3);//临时新建一个日程对象，年月日设为当前日期
						wcNewOrEdit=WhoCall.NEW;//调用日程编辑界面的是新建按钮
						gotoSetting();//去日程编辑界面
					}        	
		        }
        );       
        //bEdit设置
        bEdit.setOnClickListener
        (
        		new OnClickListener()
		        {
					@Override
					public void onClick(View v) {
						wcNewOrEdit=WhoCall.EDIT;//调用日程编辑界面的是修改按钮
						gotoSetting();//去日程编辑界面
					}        	
		        }
        ); 
        
        //删除选中的日程按钮
        bDel.setOnClickListener
        (
        		new OnClickListener()
		        {
					@Override
					public void onClick(View v) {
						showDialog(DIALOG_SCH_DEL_CONFIRM);
					}
		        }
        );
        
        //删除所有过期日程按钮
        bDelAll.setOnClickListener
        (
        		new OnClickListener()
		        {
					@Override
					public void onClick(View v) {
						showDialog(DIALOG_ALL_DEL_CONFIRM);
					}
		        }
        );
        
        //日程查找按钮
        bSearch.setOnClickListener
        (
        		new OnClickListener()
		        {
					@Override
					public void onClick(View v) {
						gotoSearch();
					}
		        }
        );
        
      //日程查看按钮
        bCheck.setOnClickListener
        (
        		new OnClickListener()
		        {
					@Override
					public void onClick(View v) {
						showDialog(DIALOG_CHECK);
					}
		        }
        ); 
    }
    //===================================自定义详情界面start===================================
    public void gotoCustom()
    {
        setContentView(R.layout.customcontent);

        final TextView cctvTitle = (TextView)findViewById(R.id.cctvtitle);
        TextView cctvDate = (TextView)findViewById(R.id.cctvdate);
        TextView cctvTime = (TextView)findViewById(R.id.cctvtime);
        final TextView cctvContent = (TextView)findViewById(R.id.cctvcontent);

        cctvTitle.setText(schTemp.getTitle());
        cctvDate.setText(schTemp.getDate1());
        cctvTime.setText(schTemp.getTime1());
        cctvContent.setText(schTemp.getNote());

        //如果备注为空，显示无备注
        if(schTemp.getNote().equals(""))
        {
            cctvContent.setText("(无备注)");
        }
        String time1=schTemp.getTime1();



        Button ccbtCancer = (Button)findViewById(R.id.ccbtcancer);
        ccbtCancer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoMain();

            }
        });

    }

    //===================================日程编辑界面start=====================================
    public void gotoSetting()//初始化新建日程界面
    {
    	setContentView(R.layout.newschedule);
    	curr=Layout.SETTING;
    	
    	TextView tvTitle=(TextView)findViewById(R.id.tvnewscheduleTitle);
    	if(wcNewOrEdit==WhoCall.NEW)
    	{
    		tvTitle.setText("新建日程");
    	}
    	else if(wcNewOrEdit==WhoCall.EDIT)
    	{
    		tvTitle.setText("修改日程");
    	}
    	final Spinner spType=(Spinner)findViewById(R.id.spxjrcType);
    	Button bNewType=(Button)findViewById(R.id.bxjrcNewType);
    	final EditText etTitle=(EditText)findViewById(R.id.etxjrcTitle);
    	final EditText etNote=(EditText)findViewById(R.id.etxjrcNote);
    	TextView tvDate=(TextView)findViewById(R.id.tvnewscheduleDate);
    	Button bSetDate=(Button)findViewById(R.id.bxjrcSetDate);
    	TextView tvTime=(TextView)findViewById(R.id.tvnewscheduleTime);
    	TextView tvAlarm=(TextView)findViewById(R.id.tvnewscheduleAlarm);
    	final Button bSetAlarm=(Button)findViewById(R.id.bxjrcSetAlarm);
    	Button bDone=(Button)findViewById(R.id.bxjrcDone);
    	Button bCancel=(Button)findViewById(R.id.bxjrcCancel);





		etTitle.setText(schTemp.getTitle());
		etNote.setText(schTemp.getNote());
		tvDate.setText(schTemp.getDate1());
		tvTime.setText(schTemp.getTimeSet()?schTemp.getTime1():"无具体时间");
		tvAlarm.setText(schTemp.getAlarmSet()?schTemp.getDate2()+"  "+schTemp.getTime2():"无闹钟");
		
		//类型spinner设置
		spType.setAdapter
		(
				new BaseAdapter()
				{
					@Override
					public int getCount() 
					{
						return alType.size();
					}
		
					@Override
					public Object getItem(int position) 
					{
						return alType.get(position);
					}
					@Override
					public long getItemId(int position) {return 0;}
		
					@Override
					public View getView(int position, View convertView, ViewGroup parent) 
					{
						LinearLayout ll=new LinearLayout(RcActivity.this);
						ll.setOrientation(LinearLayout.HORIZONTAL);	
						TextView tv=new TextView(RcActivity.this);
						tv.setText(alType.get(position));
						tv.setTextSize(17);
						tv.setTextColor(R.color.black);
						return tv;
					}			
				}
		);
		spType.setSelection(sel);

		//新建日程类型按钮
		bNewType.setOnClickListener
		(
				new OnClickListener()
				{
					@SuppressLint("ResourceAsColor") @Override
					public void onClick(View v) {
						schTemp.setTitle(etTitle.getText().toString());//将已经输入的title和note存入schTemp，以防返回时被清空
						schTemp.setNote(etNote.getText().toString());
						sel=spType.getSelectedItemPosition();//存储spType的当前选择
						gotoTypeManager();//进入日程类型管理界面
					}
				}
		);
		
		//
		bSetDate.setOnClickListener
		(
				new OnClickListener()
				{

					@Override
					public void onClick(View v) {
						schTemp.setTitle(etTitle.getText().toString());//将已经输入的主题和备注存入schTemp，以防设置完时间或闹钟返回时被清空
						schTemp.setNote(etNote.getText().toString());
						sel=spType.getSelectedItemPosition();
						wcSetTimeOrAlarm=WhoCall.SETTING_DATE;//调用设置日期时间对话框的是设置日程日期按钮
						showDialog(DIALOG_SET_DATETIME);
					}
				}
		);
		bSetAlarm.setOnClickListener
		(
				new OnClickListener()
				{
					@Override
					public void onClick(View v) {
						schTemp.setTitle(etTitle.getText().toString());//将已经输入的主题和备注存入schTemp，以防设置完时间或闹钟返回时被清空
						schTemp.setNote(etNote.getText().toString());
						sel=spType.getSelectedItemPosition();
						wcSetTimeOrAlarm=WhoCall.SETTING_ALARM;//调用设置日期时间对话框的是设置闹钟按钮
						showDialog(DIALOG_SET_DATETIME);
					}
				}
		);




		//完成按钮设置
		bDone.setOnClickListener(
			new OnClickListener()
			{
				@Override
				public void onClick(View v) {
					//让新建的日程时间和当前时间比较看是否过期
					if(schTemp.isPassed())
					{
						Toast.makeText(RcActivity.this, "不能创建过期日程", Toast.LENGTH_SHORT).show();
						return;
					}
					
					if(schTemp.getAlarmSet())//如果设置了闹钟，则检查闹钟时间是否合理
					{
						//如果日程日期在闹钟日期之前,
						//或者在日程时间已设置的前提下，日程日期和闹钟日期相同，但是日程时间在闹钟时间之前，
						//弹出提示
						if(schTemp.getDate1().compareTo(schTemp.getDate2())<0||
								schTemp.getTimeSet()&&
								schTemp.getDate1().compareTo(schTemp.getDate2())==0&&
								schTemp.getTime1().compareTo(schTemp.getTime2())<0)
						{
							Toast.makeText(RcActivity.this,"闹钟时间不能在日程时间之后", Toast.LENGTH_SHORT).show();
							return;
						}
					}
					
					String title=etTitle.getText().toString().trim();
					if(title.equals(""))//如果日程标题没有输入，默认为未命名
					{
						title="未命名";
					}
					schTemp.setTitle(title);
					String note=etNote.getText().toString();
					schTemp.setNote(note);
					String type=(String) spType.getSelectedItem();
					schTemp.setType(type);
					
			    	if(wcNewOrEdit==WhoCall.NEW)//如果当前界面是新建日程，调用插入日程方法
			    	{
			    		insertSchedule(RcActivity.this);
			    	}
			    	else if(wcNewOrEdit==WhoCall.EDIT)//如果当前界面是修改日程，调用更新日程方法
			    	{
			    		updateSchedule(RcActivity.this);
			    	}
					
					gotoMain();
				}
				
			}
		);
		//取消按钮设置
		bCancel.setOnClickListener
		(
			new OnClickListener()
			{
				@Override
				public void onClick(View v) {					
					gotoMain();
				}
				
			}
		);



		//语音识别尝试
		etTitle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				clickMethod();
				initIatData(etTitle);
			}
		});

		etNote.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				clickMethod();
				initIatData(etNote);
			}
		});

    }




    //===================================类型管理界面start=====================================
	public void gotoTypeManager()
	{
		setContentView(R.layout.typemanager);
		curr=Layout.TYPE_MANAGER;
		final ListView lvType=(ListView)findViewById(R.id.lvtypemanagerType);//列表列出所有已有类型
		final EditText etNew=(EditText)findViewById(R.id.ettypemanagerNewType);//输入新类型名称的TextView
		final Button bNew=(Button)findViewById(R.id.btypemanagerNewType);//新建类型按钮
		final Button bBack=(Button)findViewById(R.id.btypemanagerBack);//返回上一页按钮
		
		bBack.setOnClickListener
		(
				new OnClickListener()
				{
					@Override
					public void onClick(View v) {
						gotoSetting();
					}
				}
		);
		
		lvType.setAdapter
		(
				new BaseAdapter()
				{
					@Override
					public int getCount() 
					{
						return alType.size();
					}
					@Override
					public Object getItem(int position) 
					{
						return alType.get(position);
					}
					@Override
					public long getItemId(int position) 
					{
						return 0;
					}
					@Override
					public View getView(final int position, View convertView, ViewGroup parent) 
					{
						LinearLayout ll=new LinearLayout(RcActivity.this);
						ll.setOrientation(LinearLayout.HORIZONTAL);
						ll.setGravity(Gravity.CENTER_VERTICAL);
						TextView tv=new TextView(RcActivity.this);
						tv.setText(alType.get(position));
						tv.setTextSize(17);
						tv.setTextColor(Color.BLACK);
						tv.setPadding(20, 0, 0, 0);
						ll.addView(tv);
						
						//软件自带的类型不能删除，其他自建类型后面添加一个红叉用来删除自建类型
						if(position>=defultType.length)
						{
							ImageButton ib=new ImageButton(RcActivity.this);
							ib.setBackgroundDrawable(RcActivity.this.getResources().getDrawable(R.drawable.cross));
							ib.setLayoutParams(new LayoutParams(24, 24));
							ib.setPadding(20, 0, 0, 0);
							
							ib.setOnClickListener
							(
									new OnClickListener()
									{
										@Override
										public void onClick(View v) 
										{
											deleteType(RcActivity.this,lvType.getItemAtPosition(position).toString());
											loadType(RcActivity.this);
											gotoTypeManager();
										}
									}
							);
							ll.addView(ib);
						}
						return ll;
					}
				}
		);

		bNew.setOnClickListener
		(
				new OnClickListener()
				{
					@Override
					public void onClick(View v) {
						String newType=etNew.getText().toString().trim();
						if(newType.equals(""))
						{
							Toast.makeText(RcActivity.this, "类型名称不能为空。", Toast.LENGTH_SHORT).show();
							return;
						}
						insertType(RcActivity.this,newType);
						gotoTypeManager();
					}
				}
		);
	}
    //===================================查找界面start=========================================
    public void gotoSearch()
    {
		setContentView(R.layout.search);
    	curr=Layout.SEARCH;
    	final Button bChange=(Button)findViewById(R.id.bsearchChange);//改变查找范围按钮
		final Button bSearch=(Button)findViewById(R.id.bsearchGo);//开始查找
		final Button bCancel=(Button)findViewById(R.id.bsearchCancel);//取消
		final CheckBox cbDateRange=(CheckBox)findViewById(R.id.cbsearchDateRange);//查找是否限制范围的CheckBox
		final CheckBox cbAllType=(CheckBox)findViewById(R.id.cbsearchType);//是否在在所有类型中查找的CheckBox
		final ListView lv=(ListView)findViewById(R.id.lvSearchType);//所有类型列在lv中
		final TextView tvFrom=(TextView)findViewById(R.id.tvsearchFrom);//查找起始时期的tv
		final TextView tvTo=(TextView)findViewById(R.id.tvsearchTo);////查找终止时期的tv
		tvFrom.setText(rangeFrom);
		tvTo.setText(rangeTo);
		
		final ArrayList<String> type=getAllType(RcActivity.this);//获取已存日程中的所有类型和用户自建的所有类型
		
		alSelectedType.clear();
		for(int i=0;i<type.size();i++)//默认为所有类型设置状态位false
		{
			alSelectedType.add(false);
		}
		
		cbDateRange.setOnCheckedChangeListener
		(
				new OnCheckedChangeListener()
				{//根据是否限制日期范围的CheckBox决定更改日期范围的按钮是否可用
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						bChange.setEnabled(isChecked);
					}
				}
		);
		
		//设置“在全部类型中搜索”的CheckBox改变状态时的行为
		cbAllType.setOnCheckedChangeListener
		(
				new OnCheckedChangeListener()
				{
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						for(int i=0;i<type.size();i++)//选中“全部选中”后把listview里的所有类型后面的checkbox设成选中状态
						{
							alSelectedType.set(i, isChecked);
						}
						lv.invalidateViews();//刷新ListView??
					}
				}
		);
		
		bChange.setOnClickListener
		(
				new OnClickListener()
				{
					@Override
					public void onClick(View v) {
						showDialog(DIALOG_SET_SEARCH_RANGE);
					}
				}
		);
		
		lv.setAdapter
		(
				new BaseAdapter()
				{
					@Override
					public int getCount() 
					{
						return type.size();
					}
		
					@Override
					public Object getItem(int position) 
					{
						return type.get(position);
					}
					@Override
					public long getItemId(int position) 
					{
						return 0;
					}
		
					@Override
					public View getView(final int position, View convertView, ViewGroup parent) {
						LinearLayout ll=new LinearLayout(RcActivity.this);
						ll.setOrientation(LinearLayout.HORIZONTAL);	
						ll.setGravity(Gravity.CENTER_VERTICAL);
						LinearLayout llin=new LinearLayout(RcActivity.this);
						llin.setPadding(20, 0, 0, 0);
						ll.addView(llin);
						CheckBox cb=new CheckBox(RcActivity.this);
						cb.setButtonDrawable(R.drawable.checkbox);
						llin.addView(cb);
						cb.setChecked(alSelectedType.get(position));//按ArrayList里面存储的状态设置CheckBox状态
						
						cb.setOnCheckedChangeListener
						(
								new OnCheckedChangeListener()
								{
									@Override
									public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) 
									{
										alSelectedType.set(position, isChecked);//改变ArrayList里面对应位置boolean值
									}
								}
						);
						
						TextView tv=new TextView(RcActivity.this);
						tv.setText(type.get(position));
						tv.setTextSize(18);
						tv.setTextColor(R.color.black);
						ll.addView(tv);
						return ll;
					}			
				}	
		);
		
		bSearch.setOnClickListener
		(
				new OnClickListener()
				{
					@Override
					public void onClick(View v) 
					{ 
						//如果没有一个类型被选中则提示
						boolean tmp=false;
						for(boolean b:alSelectedType)
						{
							tmp=tmp|b;
						}
						if(tmp==false)
						{
							Toast.makeText(RcActivity.this, "请至少选中一个类型", Toast.LENGTH_SHORT).show();
							return;
						}
						
						searchSchedule(RcActivity.this,type);
						gotoSearchResult();
					}
				}
		);
		
		bCancel.setOnClickListener
		(
				new OnClickListener()
				{
					@Override
					public void onClick(View v) 
					{
						gotoMain();
					}
				}
		);
    }
    //===================================查找结果界面start=====================================
    public void gotoSearchResult()//该界面和主界面除了少了几个按钮其他完全一样
    {
    	setContentView(R.layout.searchresult);
    	curr=Layout.SEARCH_RESULT;

    	sel=0;
    	
    	final ImageButton bCheck=(ImageButton)findViewById(R.id.ibsearchresultCheck);
    	final ImageButton bEdit=(ImageButton)findViewById(R.id.ibsearchresultEdit);
    	final ImageButton bDel=(ImageButton)findViewById(R.id.ibsearchresultDel);
    	ImageButton bBack=(ImageButton)findViewById(R.id.ibsearchresultBack);
    	ListView lv=(ListView)findViewById(R.id.lvsearchresultSchedule);
        
        bCheck.setEnabled(false);
    	bEdit.setEnabled(false);
    	bDel.setEnabled(false);
        
        
        //以下是查找结果的ListView设置
        lv.setAdapter
        (
        		new BaseAdapter()
		        {
					@Override
					public int getCount() 
					{
						return alSch.size();
					}
		
					@Override
					public Object getItem(int position) 
					{
						return alSch.get(position);
					}
		
					@Override
					public long getItemId(int position) 
					{
						return 0;
					}
		
					@Override
					public View getView(int position, View convertView, ViewGroup parent) 
					{
						LinearLayout ll=new LinearLayout(RcActivity.this);
						ll.setOrientation(LinearLayout.VERTICAL);
						ll.setPadding(5, 5, 5, 5);
						
						LinearLayout llUp=new LinearLayout(RcActivity.this);
						llUp.setOrientation(LinearLayout.HORIZONTAL);
						LinearLayout llDown=new LinearLayout(RcActivity.this);
						llDown.setOrientation(LinearLayout.HORIZONTAL);
								
						TextView tvDate=new TextView(RcActivity.this);
						tvDate.setText(alSch.get(position).getDate1()+"   ");
						tvDate.setTextSize(17);
						tvDate.setTextColor(Color.parseColor("#129666"));
						llUp.addView(tvDate);
						
						TextView tvTime=new TextView(RcActivity.this);
						tvTime.setText(alSch.get(position).timeForListView());
						tvTime.setTextSize(17);
						tvTime.setTextColor(Color.parseColor("#925301"));
						llUp.addView(tvTime);
						
						if(alSch.get(position).isPassed())//若日程已过期，则日期和时间颜色、背景色变灰
						{
							tvDate.setTextColor(Color.parseColor("#292929"));
							tvTime.setTextColor(Color.parseColor("#292929"));
							ll.setBackgroundColor(Color.parseColor("#818175"));
						}
						
						if(alSch.get(position).getAlarmSet())
						{
							ImageView iv=new ImageView(RcActivity.this);
							iv.setImageDrawable(getResources().getDrawable(R.drawable.alarm));
							iv.setLayoutParams(new LayoutParams(20, 20));
							llUp.addView(iv);
						}
						
						TextView tvType=new TextView(RcActivity.this);
						tvType.setText(alSch.get(position).typeForListView());
						tvType.setTextSize(17);
						tvType.setTextColor(Color.parseColor("#b20000"));
						llDown.addView(tvType);
						
						TextView tvTitle=new TextView(RcActivity.this);
						tvTitle.setText(alSch.get(position).getTitle());
						tvTitle.setTextSize(17);
						tvTitle.setTextColor(Color.parseColor("#000000"));
						llDown.addView(tvTitle);
		
						
						ll.addView(llUp);
						ll.addView(llDown);
						return ll;
					}
		        }
        );
        
        lv.setOnItemClickListener
        (
        		new OnItemClickListener()
        		{
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
					{
				        bCheck.setEnabled(true);
				    	bEdit.setEnabled(true);
				    	bDel.setEnabled(true);
						schTemp=alSch.get(arg2);//选中某个项目时，把该项目对象赋给schTemp
					}
		        }
        );
        
        //修改日程按钮设置
        bEdit.setOnClickListener
        (
        		new OnClickListener()
		        {
		
					@Override
					public void onClick(View v) {
						wcSetTimeOrAlarm=WhoCall.EDIT;
						gotoSetting();
					}        	
		        }
        ); 
        //删除选中日程按钮设置
        bDel.setOnClickListener
        (
        		new OnClickListener()
		        {
					@Override
					public void onClick(View v) {
						showDialog(DIALOG_SCH_DEL_CONFIRM);
					}
		        }
        );
        
        //查找日程按钮设置
        bBack.setOnClickListener
        (
        		new OnClickListener()
		        {
					@Override
					public void onClick(View v) 
					{
						gotoSearch();
					}
		        	
		        }
        );
        
      //查看日程按钮设置
        bCheck.setOnClickListener
        (
        		new OnClickListener()
		        {
					@Override
					public void onClick(View v) {
						showDialog(DIALOG_CHECK);
					}
		        }
        );
    }
	//=========================帮助界面start==================================
	public void gotoHelp()
	{ 
		getWindow().setFlags//全屏
    	(
    			WindowManager.LayoutParams.FLAG_FULLSCREEN, 
    			WindowManager.LayoutParams.FLAG_FULLSCREEN
    	);
		setContentView(R.layout.help);
		curr=Layout.HELP;
		Button bBack=(Button)this.findViewById(R.id.bhelpback);
		bBack.setOnClickListener
		(
				new OnClickListener()
				{
					@Override
					public void onClick(View v) 
					{
						gotoMain();
					}
					
				}
		);
	}
	//创建对话框
    @Override
	public Dialog onCreateDialog(int id) 
    {
    	Dialog dialog=null;
    	switch(id)
    	{
    		case DIALOG_SET_SEARCH_RANGE:
    			AlertDialog.Builder b=new AlertDialog.Builder(this); 
  			  	b.setItems(null,null);
  			  	dialogSetRange=b.create();
  			  	dialog=dialogSetRange;	
    		break;
    		
    		case DIALOG_SET_DATETIME:
    			AlertDialog.Builder abSetDatetime=new AlertDialog.Builder(this); 
    			abSetDatetime.setItems(null,null);
    			dialogSetDatetime=abSetDatetime.create();
  			  	dialog=dialogSetDatetime;	
    		break;
    		
    		case DIALOG_SCH_DEL_CONFIRM:
    			AlertDialog.Builder abSchDelConfirm=new AlertDialog.Builder(this); 
    			abSchDelConfirm.setItems(null,null);
    			dialogSchDelConfirm=abSchDelConfirm.create();
  			  	dialog=dialogSchDelConfirm;	
    		break;
    		
    		case DIALOG_CHECK:
    			AlertDialog.Builder abCheck=new AlertDialog.Builder(this); 
    			abCheck.setItems(null,null);
    			dialogCheck=abCheck.create();
  			  	dialog=dialogCheck;	
    		break;
    		
    		case DIALOG_ALL_DEL_CONFIRM:
    			AlertDialog.Builder abAllDelConfirm=new AlertDialog.Builder(this); 
    			abAllDelConfirm.setItems(null,null);
    			dialogAllDelConfirm=abAllDelConfirm.create();
  			  	dialog=dialogAllDelConfirm;	
    		break;
    		
    		case DIALOG_ABOUT:
    			AlertDialog.Builder abAbout=new AlertDialog.Builder(this); 
    			abAbout.setItems(null,null);
    			dialogAbout=abAbout.create();
  			  	dialog=dialogAbout;	
    		break;
    	}
		return dialog;
	}
    //每次弹出Dialog对话框时更新对话框的内容
	@Override
	public void onPrepareDialog(int id, final Dialog dialog)
	{
		super.onPrepareDialog(id, dialog);
    	switch(id)
    	{
			case DIALOG_SET_SEARCH_RANGE://设置搜索范围对话框		
				dialog.setContentView(R.layout.dialogsetrange);
				//year month day后面是1的表示关于起始时间设置，2表示关于终止时间设置，P表示plus加号，M表示minus建号
				final ImageButton bYear1P=(ImageButton)dialog.findViewById(R.id.bdialogsetrangeYear1P);
				final ImageButton bYear1M=(ImageButton)dialog.findViewById(R.id.bdialogsetrangeYear1M);
				final ImageButton bMonth1P=(ImageButton)dialog.findViewById(R.id.bdialogsetrangeMonth1P);
				final ImageButton bMonth1M=(ImageButton)dialog.findViewById(R.id.bdialogsetrangeMonth1M);
				final ImageButton bDay1P=(ImageButton)dialog.findViewById(R.id.bdialogsetrangeDay1P);
				final ImageButton bDay1M=(ImageButton)dialog.findViewById(R.id.bdialogsetrangeDay1M);
				final EditText etYear1=(EditText)dialog.findViewById(R.id.etdialogsetrangeYear1);
				final EditText etMonth1=(EditText)dialog.findViewById(R.id.etdialogsetrangeMonth1);
				final EditText etDay1=(EditText)dialog.findViewById(R.id.etdialogsetrangeDay1);
				
				final ImageButton bYear2P=(ImageButton)dialog.findViewById(R.id.bdialogsetrangeYear2P);
				final ImageButton bYear2M=(ImageButton)dialog.findViewById(R.id.bdialogsetrangeYear2M);
				final ImageButton bMonth2P=(ImageButton)dialog.findViewById(R.id.bdialogsetrangeMonth2P);
				final ImageButton bMonth2M=(ImageButton)dialog.findViewById(R.id.bdialogsetrangeMonth2M);
				final ImageButton bDay2P=(ImageButton)dialog.findViewById(R.id.bdialogsetrangeDay2P);
				final ImageButton bDay2M=(ImageButton)dialog.findViewById(R.id.bdialogsetrangeDay2M);
				final EditText etYear2=(EditText)dialog.findViewById(R.id.etdialogsetrangeYear2);
				final EditText etMonth2=(EditText)dialog.findViewById(R.id.etdialogsetrangeMonth2);
				final EditText etDay2=(EditText)dialog.findViewById(R.id.etdialogsetrangeDay2);
				
				Button bSetRangeOk=(Button)dialog.findViewById(R.id.bdialogsetrangeOk);
				Button bSetRangeCancel=(Button)dialog.findViewById(R.id.bdialogsetrangeCancel);
				
				//把YYYY/MM/DD格式的年月日分离出来,并且填到显示年月日的TextView中
				String[] from=splitYMD(rangeFrom);
				String[] to=splitYMD(rangeTo);
				
				etYear1.setText(from[0]);
				etMonth1.setText(from[1]);
				etDay1.setText(from[2]);
				etYear2.setText(to[0]);
				etMonth2.setText(to[1]);
				etDay2.setText(to[2]);
				
				
				bYear1P.setOnClickListener
				(
						new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								int year=Integer.parseInt(etYear1.getText().toString().trim());
								year++;
								etYear1.setText(""+year);
							}
						}
				);
				bYear1M.setOnClickListener
				(
						new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								int year=Integer.parseInt(etYear1.getText().toString().trim());
								year--;
								etYear1.setText(""+year);
							}
						}
				);
				bMonth1P.setOnClickListener
				(
						new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								int month=Integer.parseInt(etMonth1.getText().toString().trim());
								if(++month>12)
								{
									month=1;
								}
								etMonth1.setText(month<10?"0"+month:""+month);
							}
						}
				);
				bMonth1M.setOnClickListener
				(
						new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								int month=Integer.parseInt(etMonth1.getText().toString().trim());
								if(--month<1)
								{
									month=12;
								}
								etMonth1.setText(month<10?"0"+month:""+month);
							}
						}
				);
				
				bDay1P.setOnClickListener
				(
						new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								int year=Integer.parseInt(etYear1.getText().toString().trim());
								int month=Integer.parseInt(etMonth1.getText().toString().trim());
								int day=Integer.parseInt(etDay1.getText().toString().trim());
								if(++day>getMaxDayOfMonth(year,month))
								{
									day=1;
								}
								etDay1.setText(day<10?"0"+day:""+day);
							}
						}
				);
				bDay1M.setOnClickListener
				(
						new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								int year=Integer.parseInt(etYear1.getText().toString().trim());
								int month=Integer.parseInt(etMonth1.getText().toString().trim());
								int day=Integer.parseInt(etDay1.getText().toString().trim());
								if(--day<1)
								{
									day=getMaxDayOfMonth(year,month);
								}
								etDay1.setText(day<10?"0"+day:""+day);
							}
						}
				);
				//================分割线，以上为设置起始时间的按钮监听，一下为设置终止时间的按钮监听==================
				bYear2P.setOnClickListener
				(
						new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								int year=Integer.parseInt(etYear2.getText().toString().trim());
								year++;
								etYear2.setText(""+year);
							}
						}	
				);
				bYear2M.setOnClickListener
				(
						new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								int year=Integer.parseInt(etYear2.getText().toString().trim());
								year--;
								etYear2.setText(""+year);
							}
						}
				);
				bMonth2P.setOnClickListener
				(
						new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								int month=Integer.parseInt(etMonth2.getText().toString().trim());
								if(++month>12)
								{
									month=1;
								}
								etMonth2.setText(month<10?"0"+month:""+month);
							}
						}
				);
				bMonth2M.setOnClickListener
				(
						new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								int month=Integer.parseInt(etMonth2.getText().toString().trim());
								if(--month<1)
								{
									month=12;
								}
								etMonth2.setText(month<10?"0"+month:""+month);
							}
						}
				);
				
				bDay2P.setOnClickListener
				(
						new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								int year=Integer.parseInt(etYear2.getText().toString().trim());
								int month=Integer.parseInt(etMonth2.getText().toString().trim());
								int day=Integer.parseInt(etDay2.getText().toString().trim());
								if(++day>getMaxDayOfMonth(year,month))
								{
									day=1;
								}
								etDay2.setText(day<10?"0"+day:""+day);
							}
						}
				);
				bDay2M.setOnClickListener
				(
						new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								int year=Integer.parseInt(etYear2.getText().toString().trim());
								int month=Integer.parseInt(etMonth2.getText().toString().trim());
								int day=Integer.parseInt(etDay2.getText().toString().trim());
								if(--day<1)
								{
									day=getMaxDayOfMonth(year,month);
								}
								etDay2.setText(day<10?"0"+day:""+day);
							}
						}
				);
				
				bSetRangeOk.setOnClickListener
				(
						new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								int year1=Integer.parseInt(etYear1.getText().toString().trim());
								int month1=Integer.parseInt(etMonth1.getText().toString().trim());
								int day1=Integer.parseInt(etDay1.getText().toString().trim());
								int year2=Integer.parseInt(etYear2.getText().toString().trim());
								int month2=Integer.parseInt(etMonth2.getText().toString().trim());
								int day2=Integer.parseInt(etDay2.getText().toString().trim());
								
								if(day1>getMaxDayOfMonth(year1,month1)||day2>getMaxDayOfMonth(year2,month2))
								{
									Toast.makeText(RcActivity.this, "日期设置错误", Toast.LENGTH_SHORT).show();
									return;
								}
								rangeFrom=Schedule.toDateString(year1, month1, day1);
								rangeTo=Schedule.toDateString(year2, month2, day2);
								if(rangeFrom.compareTo(rangeTo)>0)
								{
									Toast.makeText(RcActivity.this, "起始日期不能大于终止日期", Toast.LENGTH_SHORT).show();
									return;
								}
								dialogSetRange.cancel();
								gotoSearch();
							}
						}
				);
				
				//点取消则对话框关闭
				bSetRangeCancel.setOnClickListener
				(
						new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								dialogSetRange.cancel();
							}
						}
				);

			break;
			
			case DIALOG_SET_DATETIME://设置时间日期对话框
				dialog.setContentView(R.layout.dialogdatetime);
				final ImageButton bYearP=(ImageButton)dialog.findViewById(R.id.bdialogdatetimeYearP);
				final ImageButton bYearM=(ImageButton)dialog.findViewById(R.id.bdialogdatetimeYearM);
				final ImageButton bMonthP=(ImageButton)dialog.findViewById(R.id.bdialogdatetimeMonthP);
				final ImageButton bMonthM=(ImageButton)dialog.findViewById(R.id.bdialogdatetimeMonthM);
				final ImageButton bDayP=(ImageButton)dialog.findViewById(R.id.bdialogdatetimeDayP);
				final ImageButton bDayM=(ImageButton)dialog.findViewById(R.id.bdialogdatetimeDayM);
				final ImageButton bHourP=(ImageButton)dialog.findViewById(R.id.bdialogdatetimeHourP);
				final ImageButton bHourM=(ImageButton)dialog.findViewById(R.id.bdialogdatetimeHourM);
				final ImageButton bMinP=(ImageButton)dialog.findViewById(R.id.bdialogdatetimeMinP);
				final ImageButton bMinM=(ImageButton)dialog.findViewById(R.id.bdialogdatetimeMinM);
				final EditText etYear=(EditText)dialog.findViewById(R.id.etdialogdatetimeYear);
				final EditText etMonth=(EditText)dialog.findViewById(R.id.etdialogdatetimeMonth);
				final EditText etDay=(EditText)dialog.findViewById(R.id.etdialogdatetimeDay);
				final EditText etHour=(EditText)dialog.findViewById(R.id.etdialogdatetimeHour);
				final EditText etMin=(EditText)dialog.findViewById(R.id.etdialogdatetimeMin);
				final CheckBox cbSetTime=(CheckBox)dialog.findViewById(R.id.cbdialogdatetimeSettime);
				final CheckBox cbSetAlarm=(CheckBox)dialog.findViewById(R.id.cbdialogdatetimeSetAlarm);
				Button bSetDateOk=(Button)dialog.findViewById(R.id.bdialogdatetimeOk);
				Button bSetDateCancel=(Button)dialog.findViewById(R.id.bdialogdatetimeCancel);
				
				LinearLayout llSetTime=(LinearLayout)dialog.findViewById(R.id.lldialogdatetimeSetTime);
				LinearLayout llCheckBox=(LinearLayout)dialog.findViewById(R.id.lldialogdatetimeCheckBox);
				LinearLayout llAlarmCheckBox=(LinearLayout)dialog.findViewById(R.id.lldialogdatetimeAlarmCheckBox);
				
				if(wcSetTimeOrAlarm==WhoCall.SETTING_DATE)//如果是设置日期按钮调用的本对话框
				{
					llSetTime.setVisibility(LinearLayout.VISIBLE);//设置具体时间的LinearLayout显示出来
					llCheckBox.setVisibility(LinearLayout.VISIBLE);//是否设置具体时间的CheckBox显示出来
					llAlarmCheckBox.setVisibility(LinearLayout.GONE);//是否设置闹钟的CheckBox不显示
					
					//把schTemp中的year month day显示在EditText中
					etYear.setText(""+schTemp.getYear());
					etMonth.setText(schTemp.getMonth()<10?"0"+schTemp.getMonth():""+schTemp.getMonth());
					etDay.setText(schTemp.getDay()<10?"0"+schTemp.getDay():""+schTemp.getDay());
					
					//如果schTemp中表示是否设置具体时间的布尔值timeSet为true，即设置了具体时间，则把已设置的时分显示在EditText中
					if(schTemp.getTimeSet())
					{
						etHour.setText(schTemp.getHour()<10?"0"+schTemp.getHour():""+schTemp.getHour());
						etMin.setText(schTemp.getMinute()<10?"0"+schTemp.getMinute():""+schTemp.getMinute());
					}
					else//否则默认显示八点
					{
						etHour.setText("08");
						etMin.setText("00");
					}
					
					//是否设置具体时间的CheckBox决定有关设置时间的控件可不可用
					cbSetTime.setOnCheckedChangeListener
					(
							new OnCheckedChangeListener()
							{
								@Override
								public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) 
								{
									etHour.setEnabled(isChecked);
									etMin.setEnabled(isChecked);
									bHourP.setEnabled(isChecked);
									bHourM.setEnabled(isChecked);
									bMinP.setEnabled(isChecked);
									bMinM.setEnabled(isChecked);
								}
							}
					);
					
					//这三条语句确保触发cbSetTime的OnCheckedChangeListener
					cbSetTime.setChecked(schTemp.getTimeSet());
					cbSetTime.setChecked(!schTemp.getTimeSet());
					cbSetTime.setChecked(schTemp.getTimeSet());
				}
				
				//如果调用该界面的是设置闹钟按钮
				if(wcSetTimeOrAlarm==WhoCall.SETTING_ALARM)
				{
					llSetTime.setVisibility(LinearLayout.VISIBLE);//设置具体时间的LinearLayout显示
					llCheckBox.setVisibility(LinearLayout.GONE);//是否设置具体时间的CheckBox不显示
					llAlarmCheckBox.setVisibility(LinearLayout.VISIBLE);//是否设置闹钟的CheckBox显示
					
					//是否设置闹钟CheckBox决定有关设置闹钟的控件可不可用
					cbSetAlarm.setOnCheckedChangeListener
					(
							new OnCheckedChangeListener()
							{
								@Override
								public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) 
								{
									bYearP.setEnabled(isChecked);
									bYearM.setEnabled(isChecked);
									bMonthP.setEnabled(isChecked);
									bMonthM.setEnabled(isChecked);
									bDayP.setEnabled(isChecked);
									bDayM.setEnabled(isChecked);
									bHourP.setEnabled(isChecked);
									bHourM.setEnabled(isChecked);
									bMinP.setEnabled(isChecked);
									bMinM.setEnabled(isChecked);
									etYear.setEnabled(isChecked);
									etMonth.setEnabled(isChecked);
									etDay.setEnabled(isChecked);
									etHour.setEnabled(isChecked);
									etMin.setEnabled(isChecked);
								}
							}
					);
					
					//确保OnCheckedChangeListener被触发
					cbSetAlarm.setChecked(schTemp.getAlarmSet());
					cbSetAlarm.setChecked(!schTemp.getAlarmSet());
					cbSetAlarm.setChecked(schTemp.getAlarmSet());
					
					if(cbSetAlarm.isChecked())//如果表示是否设置闹钟的Checkbox是选中，说明有闹钟设置，则读取闹钟数据填入EditText
					{
						etYear.setText(""+schTemp.getAYear());
						etMonth.setText(schTemp.getAMonth()<10?"0"+schTemp.getAMonth():""+schTemp.getAMonth());
						etDay.setText(schTemp.getADay()<10?"0"+schTemp.getADay():""+schTemp.getADay());
						etHour.setText(schTemp.getAHour()<10?"0"+schTemp.getAHour():""+schTemp.getAHour());
						etMin.setText(schTemp.getAMin()<10?"0"+schTemp.getAMin():""+schTemp.getAMin());
					}
					else//如果没选中，说明没有闹钟设置，默认读取日程时间设置到闹钟的EditText
					{
						etYear.setText(""+schTemp.getYear());
						etMonth.setText(schTemp.getMonth()<10?"0"+schTemp.getMonth():""+schTemp.getMonth());
						etDay.setText(schTemp.getDay()<10?"0"+schTemp.getDay():""+schTemp.getDay());
						if(schTemp.getTimeSet())//如果日程设置了具体时间，闹钟的小时分钟设置为具体时间的小时分钟
						{
							etHour.setText(schTemp.getHour()<10?"0"+schTemp.getHour():""+schTemp.getHour());
							etMin.setText(schTemp.getMinute()<10?"0"+schTemp.getMinute():""+schTemp.getMinute());
						}
						else//如果日程没设具体时间，则闹钟的小时分钟默认设置8点
						{
							etHour.setText("08");
							etMin.setText("00");
						}
					}
				}				
				
				
				bYearP.setOnClickListener
				(
						new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								int year=Integer.parseInt(etYear.getText().toString().trim());
								year++;
								etYear.setText(""+year);
							}
						}
				);
				bYearM.setOnClickListener
				(
						new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								int year=Integer.parseInt(etYear.getText().toString().trim());
								year--;
								etYear.setText(""+year);
							}
						}
				);
				bMonthP.setOnClickListener
				(
						new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								int month=Integer.parseInt(etMonth.getText().toString().trim());
								if(++month>12)
								{
									month=1;
								}
								etMonth.setText(month<10?"0"+month:""+month);
							}
						}
				);
				bMonthM.setOnClickListener
				(
						new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								int month=Integer.parseInt(etMonth.getText().toString().trim());
								if(--month<1)
								{
									month=12;
								}
								etMonth.setText(month<10?"0"+month:""+month);
							}
						}
				);
				
				bDayP.setOnClickListener
				(
						new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								int year=Integer.parseInt(etYear.getText().toString().trim());
								int month=Integer.parseInt(etMonth.getText().toString().trim());
								int day=Integer.parseInt(etDay.getText().toString().trim());
								if(++day>getMaxDayOfMonth(year,month))
								{
									day=1;
								}
								etDay.setText(day<10?"0"+day:""+day);
							}
						}
				);
				bDayM.setOnClickListener
				(
						new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								int year=Integer.parseInt(etYear.getText().toString().trim());
								int month=Integer.parseInt(etMonth.getText().toString().trim());
								int day=Integer.parseInt(etDay.getText().toString().trim());
								if(--day<1)
								{
									day=getMaxDayOfMonth(year,month);
								}
								etDay.setText(day<10?"0"+day:""+day);
							}
						}
				);
				
				bHourP.setOnClickListener
				(
						new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								int hour=Integer.parseInt(etHour.getText().toString().trim());
								if(++hour>23)
								{
									hour=0;
								}
								etHour.setText(hour<10?"0"+hour:""+hour);
							}
						}
				);
				bHourM.setOnClickListener
				(
						new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								int hour=Integer.parseInt(etHour.getText().toString().trim());
								if(--hour<0)
								{
									hour=23;
								}
								etHour.setText(hour<10?"0"+hour:""+hour);
							}
						}
				);
				bMinP.setOnClickListener
				(
						new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								int min=Integer.parseInt(etMin.getText().toString().trim());
								if(++min>59)
								{
									min=0;
								}
								etMin.setText(min<10?"0"+min:""+min);
							}
						}
				);
				bMinM.setOnClickListener
				(
						new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								int min=Integer.parseInt(etMin.getText().toString().trim());
								if(--min<0)
								{
									min=59;
								}
								etMin.setText(min<10?"0"+min:""+min);
							}
						}
				);
				
				bSetDateOk.setOnClickListener
				(
						new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								String year=etYear.getText().toString().trim();
								String month=etMonth.getText().toString().trim();
								String day=etDay.getText().toString().trim();
								//最后再检查一下是否有年月日设置错误，比如2月30号等等
								if(Integer.parseInt(day)>getMaxDayOfMonth(Integer.parseInt(year),Integer.parseInt(month)))
								{
									Toast.makeText(RcActivity.this, "日期设置错误", Toast.LENGTH_SHORT).show();
									return;
								}
								
								//如果此对话框是被设置日期按钮调用的，把年月日赋给Schedule中的Date1，即日程日期
								if(wcSetTimeOrAlarm==WhoCall.SETTING_DATE)
								{
									schTemp.setDate1(year, month, day);
									schTemp.setTimeSet(cbSetTime.isChecked());
									if(cbSetTime.isChecked())//如果设置了具体时间，把时分赋给Schedule中的Time1，即日程时间
									{							
										String hour=etHour.getText().toString().trim();
										String min=etMin.getText().toString().trim();
										schTemp.setTime1(hour, min);
									}
									
								}
								//如果此对话框是被设置闹钟按钮调用的，把年月日赋给Schedule中的Date2，即闹钟日期，时分赋给Time2，即闹钟时间
								else if(wcSetTimeOrAlarm==WhoCall.SETTING_ALARM)
								{
									schTemp.setAlarmSet(cbSetAlarm.isChecked());
									if(cbSetAlarm.isChecked())
									{
                                        schTemp	.setDate2(year, month, day);
										String hour=etHour.getText().toString().trim();
										String min=etMin.getText().toString().trim();
										schTemp.setTime2(hour, min);
									}
								}
								dialogSetDatetime.cancel();
								gotoSetting();		
							}
						}
				);
				bSetDateCancel.setOnClickListener
				(
						new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								dialogSetDatetime.cancel();
							}
						}
				);
				break;
			case DIALOG_SCH_DEL_CONFIRM://删除日程对话框
				dialog.setContentView(R.layout.dialogschdelconfirm);
				Button bDelOk=(Button)dialog.findViewById(R.id.bdialogschdelconfirmOk);
				Button bDelCancel=(Button)dialog.findViewById(R.id.bdialogschdelconfirmCancel);
				
				bDelOk.setOnClickListener
				(
						new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								deleteSchedule(RcActivity.this);
								gotoMain();
								dialogSchDelConfirm.cancel();
							}
						}
				);
				
				bDelCancel.setOnClickListener
				(
						new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								dialogSchDelConfirm.cancel();
							}
						}
				);
				break;
				
			case DIALOG_CHECK://查看日程对话框
				dialog.setContentView(R.layout.dialogcheck);
				TextView tvType=(TextView)dialog.findViewById(R.id.tvdialogcheckType);//显示类型的TextView
				TextView tvTitle=(TextView)dialog.findViewById(R.id.tvdialogcheckTitle);//显示标题的TextView
				TextView tvNote=(TextView)dialog.findViewById(R.id.tvdialogcheckNote);//显示备注的TextView
				TextView tvDatetime1=(TextView)dialog.findViewById(R.id.tvdialogcheckDate1);//显示日程日期和时间的TextView
				TextView tvDatetime2=(TextView)dialog.findViewById(R.id.tvdialogcheckDate2);//显示闹钟日期和时间的TextView
				Button bEdit=(Button)dialog.findViewById(R.id.bdialogcheckEdit);//编辑按钮
				Button bDel=(Button)dialog.findViewById(R.id.bdialogcheckDel);//删除按钮
				Button bBack=(Button)dialog.findViewById(R.id.bdialogcheckBack);//返回按钮
				Button bCustom=(Button)dialog.findViewById(R.id.bdialogcheckCustom);//自定义按钮
				
				tvType.setText(schTemp.typeForListView());
				tvTitle.setText(schTemp.getTitle());
				tvNote.setText(schTemp.getNote());
				
				//如果备注为空，显示无备注
				if(schTemp.getNote().equals(""))
				{
					tvNote.setText("(无备注)");
				}
				String time1=schTemp.getTime1();
				
				//如果具体时间为空，时间显示成--:--
				if(time1.equals("null"))
				{
					time1="- -:- -";
				}
				tvDatetime1.setText(schTemp.getDate1()+"  "+time1);
				
				String date2=schTemp.getDate2();
				String time2=schTemp.getTime2();
				
				//闹钟日期为空的话说明没有闹钟
				if(date2.equals("null"))
				{
					date2="(无闹钟)";
					time2="";
				}
				tvDatetime2.setText(date2+"  "+time2);
				
		        bEdit.setOnClickListener
		        (
		        		new OnClickListener()
				        {
							@Override
							public void onClick(View v) {
								dialogCheck.cancel();
								gotoSetting();
							}        	
				        }
		        ); 
		        
		        bDel.setOnClickListener
		        (
		        		new OnClickListener()
				        {
							@Override
							public void onClick(View v) {
								dialogCheck.cancel();
								showDialog(DIALOG_SCH_DEL_CONFIRM);
							}
				        }
		        );

                bBack.setOnClickListener
                (
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialogCheck.cancel();

                            }
                        }
                );

		        bCustom.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {
						dialogCheck.cancel();
                        gotoCustom();
					}
				});
				break;
			case DIALOG_ALL_DEL_CONFIRM://删除所有过期日程对话框
				dialog.setContentView(R.layout.dialogdelpassedconfirm);
				Button bAllDelOk=(Button)dialog.findViewById(R.id.bdialogdelpassedconfirmOk);
				Button bAllDelCancel=(Button)dialog.findViewById(R.id.bdialogdelpassedconfirmCancel);
				bAllDelOk.setOnClickListener
				(
						new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								deletePassedSchedule(RcActivity.this);
								gotoMain();
								dialogAllDelConfirm.cancel();
							}
						}
				);
				
				bAllDelCancel.setOnClickListener
				(
						new OnClickListener()
						{
							@Override
							public void onClick(View v) 
							{
								dialogAllDelConfirm.cancel();
							}
						}
			    );
				break;
    	}
	}
	//onKeyDown方法
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	//按下手机返回按钮时
    	if(keyCode==4){
        	switch(curr)
        	{
        		case MAIN://在主界面的话退出程序
        			System.exit(0);
        		break;
        		case SETTING://在日程编辑界面的话返回主界面
        			gotoMain();
        		break;
        		case TYPE_MANAGER:////在类型管理界面的话返回日程编辑界面
        			gotoSetting();
        		break;
        		case SEARCH://在日程查找界面的话返回主界面
        			gotoMain();
        		break;
        		case SEARCH_RESULT://在日程查找结果界面的话返回日程查找界面
        			gotoSearch();
        		break;
        		case HELP://在帮助界面的话返回主界面
        			gotoMain();
        		break;
        		case ABOUT:
        			gotoMain();
        		break;
        	}
        	return true;
    	}
    	return false;
	}
    //创建Menu
    @Override
	public boolean onCreateOptionsMenu(Menu menu) 
    {
    	if(curr!=Layout.MAIN)//只允许在主界面调用菜单???????????????????????
    	{
    		return false;  
    	}
    	MenuItem miHelp=menu.add(1, MENU_HELP, 0, "帮助");
    	miHelp.setIcon(R.drawable.help);
		MenuItem miAbout=menu.add(1, MENU_ABOUT, 0, "关于");
		miAbout.setIcon(R.drawable.about);
		MenuItem miWeb=menu.add(1, MENU_WEB,0,"网站");
		miWeb.setIcon(R.drawable.web);
		miAbout.setOnMenuItemClickListener
		(
				new OnMenuItemClickListener()
				{
					@Override
					public boolean onMenuItemClick(MenuItem item) 
					{	
						setContentView(R.layout.rcabout);
						curr=Layout.HELP;
						return true;
					}
					
				}
		);
		
		miHelp.setOnMenuItemClickListener
		(
				new OnMenuItemClickListener()
				{
					@Override
					public boolean onMenuItemClick(MenuItem item) 
					{		
						setContentView(R.layout.rchelp);
						curr=Layout.ABOUT;
						return true;
					}
				}
		);
		
		miWeb.setOnMenuItemClickListener
		(
				new OnMenuItemClickListener() 
				{
			
					private WebView webView;
			@Override
			public boolean onMenuItemClick(MenuItem item)
			{
				setContentView(R.layout.rcweb);
				webView = (WebView)findViewById(R.id.web_view);
				webView.setWebViewClient(new WebViewClient());
				webView.getSettings().setJavaScriptEnabled(true);
				webView.loadUrl("http://www.yusheng123.cn");
				curr=Layout.ABOUT;
				return true;
			}
		});
		return true;
	}
    @Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) 
	{
		return super.onMenuItemSelected(featureId, item);
	}
    //用来得到year年month月的最大天数
	public int getMaxDayOfMonth(int year,int month)
    {
    	int day=0;
    	boolean run=false;
    	if(year%400==0||year%4==0&&year%100!=0)
    	{
    		run=true;
    	}
    	if(month==4||month==6||month==9||month==11)
    	{
    		day=30;
    	}
    	else if(month==2)
    	{
    		if(run)
    		{
    			day=29;
    		}
    		else
    		{
    			day=28;
    		}
    	}
    	else
    	{
    		day=31;
    	}
    	return day;
    }
    //返回把YYYY/MM/DD分隔后的年月日字符串数组
	public String[] splitYMD(String ss)
    {
    	String[] s=ss.split("/");
    	return s;
    }
}