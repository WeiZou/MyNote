package com.wei.note;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.swimmi.windnote.R;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Welcome extends Activity {

	private LinearLayout welcome;		//欢迎界面布局
	private TextView quoteTxt;		//引言标签
	private int color; 

	private SharedPreferences sp;           //默认设置数据存储
	private Dialog keyDialog;               //密码对话框
	private EditText keyTxt;                //密码
	private Boolean needKey=true;		//是否需要密码
	private SQLiteDatabase wn;              //数据库
	private Handler welcomeHand;		//欢迎页停留
	private Runnable welcomeShow;
	
	private String quote;                  //引言
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	    setContentView(R.layout.welcome);           //加载欢迎界面布局文件
	    wn=Database.ConnectToDatabase(this);        //连接数据库
	    sp = getSharedPreferences("setting", 0);//获取默认设置数据
	    String content=getResources().getString(R.string.hello_world);		//引言内容
	    String author=getResources().getString(R.string.app_name);			//引言作者
	    String type=getResources().getString(R.string.app_name);			//引言类型
	    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
	    if(!sp.getString("today","2012-12-21").equals(sdf.format(new Date())))		//控制每天显示一则引言
	    {
		    //从数据库中加载全部的引言
		    Cursor cursor=wn.rawQuery("select * from quotes order by q_count,id limit 1", null);
		    if(cursor.moveToFirst())
		    {
		    	content=cursor.getString(cursor.getColumnIndex("q_content"));
		    	author=cursor.getString(cursor.getColumnIndex("q_author"));
		    	type=cursor.getString(cursor.getColumnIndex("q_type"));
				sp.edit().putString("q_content",content).commit();
				sp.edit().putString("q_author", author).commit();
				sp.edit().putString("q_type", type).commit();
		    	quote=content;
		    	int id=cursor.getInt(cursor.getColumnIndex("id"));
		    	wn.execSQL("update quotes set q_count=q_count+1 where id="+id);
		    	sp.edit().putString("today", sdf.format(new Date())).commit();
		    }
		    cursor.close();
	    }
	    else
	    {
	    	content=sp.getString("q_content", "");
	    	author=sp.getString("q_author", "");
	    	type=sp.getString("q_type", "");
	    	quote=content;
	    }
	    
	    color=sp.getInt("color", getResources().getColor(R.color.blue));
		welcome=(LinearLayout)findViewById(R.id.welcome);
		welcome.setBackgroundColor(color);
		welcome.setOnClickListener(new OnClickListener(){		//点击屏幕跳过引言
			@Override
			public void onClick(View arg0) {
				welcome();
	        	welcomeHand.removeCallbacks(welcomeShow);
			}
		});
		quoteTxt=(TextView)findViewById(R.id.quote_txt);
		quoteTxt.setTextColor(color);
		quoteTxt.setText(content+"\r\n\r\n by"+author);
        
		welcomeHand = new Handler();
		welcomeShow=new Runnable()
	    {
	        @Override
	        public void run()
	        {  
	        	welcome();
	        }
	    };
		welcomeHand.postDelayed(welcomeShow, (quote.length()+7)*100); 
	}
	private void welcome(){		//进入欢迎界面
		Intent data=getIntent();
    	needKey=data.getBooleanExtra("needKey", true);//判断用户是否设置了密码
    	if(needKey&&sp.contains("key"))//如果设置密码，弹出输入密码界面
    		enterKey();
       	else//没有设置密码，直接跳转到主界面
    	{
            Intent intent=new Intent(Welcome.this,SlidingActivity.class);//跳转到主界面
            startActivity(intent);
            finish();
    	}
	}
	private void enterKey()			//输入密码
	{
		View keyView = View.inflate(this, R.layout.cancelkey, null);
		keyDialog=new Dialog(this,R.style.dialog);
		keyDialog.setContentView(keyView);
		keyTxt=(EditText)keyView.findViewById(R.id.key_old);
		keyTxt.addTextChangedListener(change);
		keyDialog.show();
	}
	
	public TextWatcher change = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			String okey=keyTxt.getText().toString();
			String rkey=sp.getString("key", "");
			if(okey.equals(rkey))
			{
				needKey=false;
				keyDialog.dismiss();
	            Intent intent=new Intent(Welcome.this,SlidingActivity.class);
	            startActivity(intent);
	            finish();
			}
		}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}
		@Override
		public void afterTextChanged(Editable s) {
		}
	};
	@Override
	public boolean onKeyDown(int keyCode,KeyEvent event){
		if(keyCode==KeyEvent.KEYCODE_BACK)
		{
			if(needKey){
				finish();
				System.exit(0);
			}
			else
			{return true;}
		}
		if(keyCode==KeyEvent.KEYCODE_MENU)
		{
			welcome();
        	welcomeHand.removeCallbacks(welcomeShow);
		}
		return false;
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}
}
