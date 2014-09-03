package com.wei.note;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import com.swimmi.windnote.R;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Selection;
import android.text.Spannable;
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
//编辑某个特定的记事
public class Note extends Activity {

	private LinearLayout note;		//布局
	private TextView titleTxt;		//标题栏
	private EditText noteTxt;		//输入框
	
	private ImageButton deleteBtn;	//删除按钮
	private ImageButton confirmBtn;	//确认按钮
	
	private Dialog delDialog;		//删除对话框
	private Integer s_id;			//记事ID
	private String title;			//标题
	private String content;			//内容
	private int color;			//当前皮肤颜色
	private SharedPreferences sp;//数据库连接
	private SQLiteDatabase wn;//设置数据存储
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note); //加载编辑界面的布局文件note
		
        wn=Database.ConnectToDatabase(this);     //连接数据库
        sp = getSharedPreferences("setting", 0);//获取默认的配置文件
        color=sp.getInt("color", getResources().getColor(R.color.blue));//设置背景颜色
         
		note=(LinearLayout)findViewById(R.id.note);//获取布局容器note
		note.setBackgroundColor(color);
		
        titleTxt=(TextView)findViewById(R.id.title_note);
        noteTxt=(EditText)findViewById(R.id.note_txt); 
        deleteBtn=(ImageButton)findViewById(R.id.delete_btn);
        confirmBtn=(ImageButton)findViewById(R.id.confirm_btn);
        
        Intent intent=getIntent();		//恢复未保存数据
	HashMap<String, Object> map=(HashMap<String, Object>) intent.getSerializableExtra("data");
        title=(String) map.get("title");
        content=(String) map.get("content");
        s_id=(Integer)map.get("id");
        titleTxt.setText(title);
        noteTxt.setText(content);
        ImageButton[] btns={deleteBtn,confirmBtn}; //为删除、确认按钮添加监听器，以处理对应的事件
		for(ImageButton btn:btns)
			btn.setOnClickListener(click);
	}
	
	public void focus(EditText view,Boolean b){
		view.setCursorVisible(b);
		view.setFocusable(b);
	    view.setFocusableInTouchMode(b);
	    if(b==true)
	    	view.requestFocus();
	    else
	    	view.clearFocus();
		Spannable text = (Spannable)view.getText();
		Selection.setSelection(text, b?text.length():0);
	}
	private OnClickListener click=new OnClickListener(){
		@Override
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.delete_btn:
				delete();
				break;
			case R.id.confirm_btn:
				save();
				break;
			}
		}
		
	};
	@Override
	public boolean onKeyDown(int keyCode,KeyEvent event)
	{
		if(keyCode==KeyEvent.KEYCODE_BACK){
			back();
			return true;
		}
		return false;
	}
	private void delete(){		         //清空当前记事的全部内容
	    noteTxt.setText("");
		
	}
	private void save()			//保存记事
	{
		String n_content=noteTxt.getText().toString().trim();
		if(n_content.trim().length()>0)
		{
		        //保存对应的记事数据到数据库端，实现数据同步
			wn.execSQL("update notes set n_content=? where id=?",new Object[]{n_content,s_id});
			if(!n_content.equals(content))
			{
				Toast.makeText(Note.this, R.string.note_saved, Toast.LENGTH_SHORT).show();
			}
			Intent intent=new Intent(Note.this,SlidingActivity.class);
			startActivity(intent);
			finish();
		}
		else
			Toast.makeText(Note.this, R.string.note_null, Toast.LENGTH_SHORT).show();
	}
	private void back(){
		Intent intent=new Intent(Note.this,SlidingActivity.class);
		startActivity(intent);
		finish();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}
}
