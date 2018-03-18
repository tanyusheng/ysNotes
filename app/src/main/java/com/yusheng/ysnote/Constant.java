package com.yusheng.ysnote; 

import java.util.Calendar;  

import android.R.integer;
public class Constant 
{
	final static int DIALOG_SET_SEARCH_RANGE=1;//设置搜索日期范围对话框
	final static int DIALOG_SET_DATETIME=2;//设置日期时间对话框
	final static int DIALOG_SCH_DEL_CONFIRM=3;//日程删除确认
	final static int DIALOG_CHECK=4;//查看日程
	final static int DIALOG_ALL_DEL_CONFIRM=5;//删除全部过期日程
	final static int DIALOG_ABOUT=6;//关于对话框
	 
	final static int MENU_HELP=1;//菜单帮助  
	final static int MENU_ABOUT=2;//菜单关于
	final static int MENU_WEB=3;//菜单网站
	
	public static enum WhoCall
	{//判断谁调用了dialogSetRange，以决定哪个控件该gone或者visible 
		SETTING_ALARM,//表示设置闹钟 按钮
		SETTING_DATE,//表示设置日期按钮
		SETTING_RANGE,//表示设置日程查找范围按钮
		NEW,//表示新建日程按钮
		EDIT,//表示修改日程按钮
		SEARCH_RESULT//表示查找按钮
	}
	
	public static enum Layout
	{
		WELCOME_VIEW,
		MAIN,//主界面
		SETTING,//日程设置
		TYPE_MANAGER,//类型管理
		SEARCH,//查找
		SEARCH_RESULT,//查找结果界面
		HELP,//帮助界面
		ABOUT,
		WEB
	}
	
	public static String getNowDateString()//获得当前日期方法并转换格式YYYY/MM/DD
	{
		Calendar c=Calendar.getInstance();
		String nowDate=Schedule.toDateString(c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DAY_OF_MONTH));
		return nowDate;
		
	}
	public static String getNowTimeString()//获得当前时间，并转换成格式HH:MM
	{
		Calendar c=Calendar.getInstance();
		int nowh=c.get(Calendar.HOUR_OF_DAY);
		int nowm=c.get(Calendar.MINUTE);
		String nowTime=(nowh<10?"0"+nowh:""+nowh)+":"+(nowm<10?"0"+nowm:""+nowm);
		return nowTime;
	}
}