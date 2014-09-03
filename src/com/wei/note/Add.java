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
	
	public static void focus(EditText view,Boolean b){		//ʧȥ���õ�������
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
	private void save()			//�������
	{
		String n_title=titleTxt.getText().toString().trim();
		if(n_title.length()==0)//���δ�ڱ������������ݣ�����±��Ᵽ��Ϊ�ޱ���
			n_title="�ޱ���";
		String n_content=noteTxt.getText().toString().trim();
		if(n_content.trim().length()>0){//��Ӽ��º�Ҫ�����ݿ�����ӣ���ͬ�����º��ļ��б�
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
	private void back(){		//����������
		Intent intent=new Intent(Add.this,SlidingActivity.class);
		String title=titleTxt.getText().toString().trim();
		String content=noteTxt.getText().toString().trim();
		if(title.length()>0||content.length()>0)		
		{
		        //����Add��δ��������ݣ����´ν���ʱ�ָ�
			Bundle data=new Bundle();
			data.putString("title",title);
			data.putString("content",content);
			intent.putExtras(data);
		}
		startActivity(intent);//�ص�������
		finish();
	}
	@Override
	public boolean onKeyDown(int keyCode,KeyEvent event)		//�����¼���д�������ֻ��İ��������أ�����Ļ���˵��������¼�
	{
		if(keyCode==KeyEvent.KEYCODE_BACK){
			back();
			return true;
		}
		return false;
	}
	private OnClickListener click=new OnClickListener(){		//��Ļ��ť����¼�����

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
	public boolean onCreateOptionsMenu(Menu menu) {            //Ĭ�ϵ��ֻ��˵����������µĴ�����
		return true;
	}

}
