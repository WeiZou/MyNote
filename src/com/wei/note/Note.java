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
//�༭ĳ���ض��ļ���
public class Note extends Activity {

	private LinearLayout note;		//����
	private TextView titleTxt;		//������
	private EditText noteTxt;		//�����
	
	private ImageButton deleteBtn;	//ɾ����ť
	private ImageButton confirmBtn;	//ȷ�ϰ�ť
	
	private Dialog delDialog;		//ɾ���Ի���
	private Integer s_id;			//����ID
	private String title;			//����
	private String content;			//����
	private int color;			//��ǰƤ����ɫ
	private SharedPreferences sp;//���ݿ�����
	private SQLiteDatabase wn;//�������ݴ洢
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note); //���ر༭����Ĳ����ļ�note
		
        wn=Database.ConnectToDatabase(this);     //�������ݿ�
        sp = getSharedPreferences("setting", 0);//��ȡĬ�ϵ������ļ�
        color=sp.getInt("color", getResources().getColor(R.color.blue));//���ñ�����ɫ
         
		note=(LinearLayout)findViewById(R.id.note);//��ȡ��������note
		note.setBackgroundColor(color);
		
        titleTxt=(TextView)findViewById(R.id.title_note);
        noteTxt=(EditText)findViewById(R.id.note_txt); 
        deleteBtn=(ImageButton)findViewById(R.id.delete_btn);
        confirmBtn=(ImageButton)findViewById(R.id.confirm_btn);
        
        Intent intent=getIntent();		//�ָ�δ��������
	HashMap<String, Object> map=(HashMap<String, Object>) intent.getSerializableExtra("data");
        title=(String) map.get("title");
        content=(String) map.get("content");
        s_id=(Integer)map.get("id");
        titleTxt.setText(title);
        noteTxt.setText(content);
        ImageButton[] btns={deleteBtn,confirmBtn}; //Ϊɾ����ȷ�ϰ�ť��Ӽ��������Դ����Ӧ���¼�
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
	private void delete(){		         //��յ�ǰ���µ�ȫ������
	    noteTxt.setText("");
		
	}
	private void save()			//�������
	{
		String n_content=noteTxt.getText().toString().trim();
		if(n_content.trim().length()>0)
		{
		        //�����Ӧ�ļ������ݵ����ݿ�ˣ�ʵ������ͬ��
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
