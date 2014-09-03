package com.wei.note;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.swimmi.windnote.R;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Add extends Activity {

	private LinearLayout add;			
	private EditText noteTxt;			
	private EditText titleTxt;			
	private ImageButton backBtn;		       
	private ImageButton saveBtn;		       	
	private int color;			      

	private SharedPreferences sp;		       
	private SQLiteDatabase wn;			
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add);		          
		wn=Database.ConnectToDatabase(this);		
        sp = getSharedPreferences("setting", 0);		
        color=sp.getInt("color", getResources().getColor(R.color.blue));		
        
		add=(LinearLayout)findViewById(R.id.add);      
		add.setBackgroundColor(color);                 
		noteTxt=(EditText)findViewById(R.id.note_txt); 
		titleTxt=(EditText)findViewById(R.id.title_txt);
		EditText[] txts={titleTxt,noteTxt};             
	        for(EditText txt:txts){                        
	    		focus(txt,true);                      
		        txt.setTextColor(color);                
		        txt.setBackgroundResource(R.color.white);
	        }
		if(getIntent().hasExtra("title"))		
		{
			Bundle data=getIntent().getExtras();
			if(data.containsKey("title"))
				titleTxt.setText(data.getString("title"));
			if(data.containsKey("content"))
				noteTxt.setText(data.getString("content"));
		}
        
		backBtn = (ImageButton)findViewById(R.id.back_btn);
		saveBtn = (ImageButton)findViewById(R.id.save_btn);
		ImageButton[] btns={backBtn,saveBtn};
		for(ImageButton btn:btns)		         
			btn.setOnClickListener(click);
	}
	
	public static void focus(EditText view,Boolean b){		//失去（得到）焦点
		view.setCursorVisible(b);
		view.setFocusable(b);
	    view.setFocusableInTouchMode(b);
	    if(b==true)
	    	view.requestFocus();
	    else
	    	view.clearFocus();
		Spannable text = (Spannable)view.getText();
		Selection.setSelection(text, text.length());
	}
	private void save()			//保存记事
	{
		String n_title=titleTxt.getText().toString().trim();
		if(n_title.length()==0)//如果未在标题栏输入内容，则记事标题保存为无标题
			n_title="无标题";
		String n_content=noteTxt.getText().toString().trim();
		if(n_content.trim().length()>0){//添加记事后，要向数据库中添加，以同步记事和文件列表
			wn.execSQL("insert into notes(n_title,n_content) values(?,?)",new Object[]{n_title,n_content});
			wn.execSQL("update file set n_counts=n_counts+1 where f_id="+SlidingActivity.f_id);
			Toast.makeText(Add.this, R.string.note_saved, Toast.LENGTH_SHORT).show();
			Intent intent=new Intent(Add.this,SlidingActivity.class);
			startActivity(intent);
			finish();
		}
		else
			Toast.makeText(Add.this, R.string.note_null, Toast.LENGTH_SHORT).show();
	}
	private void back(){		//返回主界面
		Intent intent=new Intent(Add.this,SlidingActivity.class);
		String title=titleTxt.getText().toString().trim();
		String content=noteTxt.getText().toString().trim();
		if(title.length()>0||content.length()>0)		
		{
		        //传递Add中未保存的数据，供下次进入时恢复
			Bundle data=new Bundle();
			data.putString("title",title);
			data.putString("content",content);
			intent.putExtras(data);
		}
		startActivity(intent);//回到主界面
		finish();
	}
	@Override
	public boolean onKeyDown(int keyCode,KeyEvent event)		//返回事件重写，监听手机的按键（返回，主屏幕，菜单）按下事件
	{
		if(keyCode==KeyEvent.KEYCODE_BACK){
			back();
			return true;
		}
		return false;
	}
	private OnClickListener click=new OnClickListener(){		//屏幕或按钮点击事件监听

		@Override
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.back_btn:
				back();
				break;
			case R.id.save_btn:
				save();
				break;
			}
		}
	};
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {            //默认的手机菜单按键被按下的处理方案
		return true;
	}

}
