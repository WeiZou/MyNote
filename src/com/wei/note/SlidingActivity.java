package com.wei.note;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.swimmi.windnote.R;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnKeyListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
public class SlidingActivity extends FragmentActivity {
    private Dialog menuDialog;		//�˵��Ի���
	private GridView menuGrid;		//�˵�ѡ��
	private View menuView;			//�˵�ѡ����ͼ
	
	//���±������水ť
	private ImageButton addBtn;		//���
	private ImageButton menuBtn;	        //�����˵�
	private ImageButton searchBtn;	       //����
	private ImageButton modeBtn;	       //��ʾģʽ
	private ImageButton sortBtn;	      //����
	
	//���²���
	private ListView notesLis;		//�����б�
	private GridView notesGrd;		//��������
	private TextView titleTxt;		//����
	private LinearLayout main;		//����
	
	private EditText keyTxt;		//�����
	private EditText againTxt;		//����ȷ�Ͽ�
	private EditText newTxt;		//�������
	private EditText searchTxt;		//������
	private TextView refreshTxt;	        //ˢ�±�ǩ
	
	//ϵͳ���ò���
	private Integer s_id;			//����ID
	private boolean sort_desc;		//�����ʶ
	private boolean mode_list;		//ģʽ��ʶ
	private long exitTime;			//�˳�ʱ��
	private int color;			//��ǰƤ����ɫ
	
	//����
	private String q_content;		//��������
	private String q_author;		//��������
	private String q_type;			//��������
	private HashMap<Integer,Integer> idMap;	//IDMap
	private HashMap<Integer,Integer> fileidMap;	//IDMap
	
	final int ACTION_SKIN=0;	//�˵�ѡ��
	final int ACTION_KEY=1;
	final int ACTION_SAY=2;
	final int ACTION_HELP=3;
	final int ACTION_ABOUT=4;
	final int ACTION_EXIT=5;
	private float mx;		//��Ļ��������
	private float my;
	
	private ColorPickerDialog cpDialog;		//��ɫѡ��Ի���
	private SharedPreferences sp;			//���ݴ洢
	private Dialog keyDialog;			//����Ի���
	private SQLiteDatabase wn;			//���ݿ�����
	Handler mHandler  = new Handler();
	SlidingMenu mSlidingMenu;
	public static Integer f_id=0;
	private ListView file_lis;                      //�ļ��б�
	private LinearLayout file;                      //file��������
	private TextView f_refresh;               
	List<Map<String, Object>> List;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//�������ݿ⣬�������ò���
		mSlidingMenu = new SlidingMenu(this);
		setContentView(mSlidingMenu);                  //����SlideMenu�����ļ�
		View menu = getLayoutInflater().inflate(R.layout.file, null);//����file�����ļ�����menu��ͼ
		View content = getLayoutInflater().inflate(R.layout.main, null);//����main�����ļ�����menu��ͼ
		mSlidingMenu.setMenu(menu);                                //ΪSlidemenu���Content��Menu��ͼ
		mSlidingMenu.setContent(content);		
	        mHandler.post(new Runnable() {
			
			@Override
			public void run() {
//				mSlidingMenu.showMenu();
			}
		}); 	
		wn=Database.ConnectToDatabase(this);             //�������ݿ�
		sp = getSharedPreferences("setting", 0);         //��ȡĬ����������
		idMap=new HashMap<Integer, Integer>();	
		fileidMap=new HashMap<Integer, Integer>();	//��ȡ����ID�б�
                color=sp.getInt("color", getResources().getColor(R.color.blue));//����Ĭ��Ƥ����ɫ
		main=(LinearLayout)content.findViewById(R.id.main);//��ȡmain��������
		main.setBackgroundColor(color);                    //main���ñ�����ɫ
		//����UI���
		titleTxt=(TextView)content.findViewById(R.id.title_main);
		addBtn=(ImageButton)content.findViewById(R.id.add_btn);
		menuBtn=(ImageButton)content.findViewById(R.id.menu_btn);
		searchBtn=(ImageButton)content.findViewById(R.id.search_btn);
		modeBtn=(ImageButton)content.findViewById(R.id.mode_btn);
		sortBtn=(ImageButton)content.findViewById(R.id.sort_btn);
		notesLis=(ListView)content.findViewById(R.id.notes_lis);
		notesLis.setVerticalScrollBarEnabled(true);
		notesGrd=(GridView)content.findViewById(R.id.notes_grd);
		notesGrd.setVerticalScrollBarEnabled(true);
		file=(LinearLayout)menu.findViewById(R.id.file);
	        file.setBackgroundColor(color);
	        file_lis=(ListView)menu.findViewById(R.id.file_list);
		file_lis.setVerticalScrollBarEnabled(true);
		@SuppressWarnings("deprecation")
		int width=getWindowManager().getDefaultDisplay().getWidth();	//��ȡ��Ļ���
		notesGrd.setNumColumns(width/120);			       //�������񲼾�����
		
		//��������
		q_content=sp.getString("q_content", "");
		q_author=sp.getString("q_author", "");
		q_type=sp.getString("q_type", "");
		
		//Ϊ��ť����¼�������
		ImageButton[] btns={addBtn,menuBtn,searchBtn,modeBtn,sortBtn};
		for(ImageButton btn:btns)
			btn.setOnClickListener(click);
		
		sort_desc=sp.getBoolean("sort", true);		//��ȡ����ʽ
		mode_list=sp.getBoolean("mode", true);		//��ȡ��ʾģʽ
		
		menuDialog = new Dialog(this,R.style.dialog);		//�Զ���˵�
		menuView = View.inflate(this, R.layout.gridmenu, null);
		menuDialog.setContentView(menuView);
		menuDialog.setOnKeyListener(new OnKeyListener(){       //���õ���ֻ��˵���ť��Ӧ�¼�
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_MENU)
				dialog.dismiss();
				return false;
			}
		});
		
		//���˵���
		menuGrid=(GridView)menuView.findViewById(R.id.grid);
		menuGrid.setAdapter(getMenuAdapter());		//���ò˵���
		menuGrid.setOnItemClickListener(itemClick);
		
		//��������
		searchTxt=(EditText)findViewById(R.id.search_txt);
		searchTxt.setBackgroundColor(color);
		searchTxt.addTextChangedListener(search);
		searchTxt.setText(sp.getString("word", ""));
		
		//���µ���¼�
		titleTxt.setOnClickListener(click);
		refreshTxt=(TextView)findViewById(R.id.refresh_txt);
		
		Long lastdate=sp.getLong("lastdate", new Date().getTime());		//���¼��±���ʱ��
		sp.edit().putLong("lastdate",new Date().getTime()).commit();
		showItem(sort_desc,mode_list);
		showItem();
		file_lis.setOnItemClickListener(new OnItemClickListener(){		//��������¼�
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				//f_id=(Integer)list.get(position).get("id");
			        f_id=fileidMap.get(position);
			        titleTxt.setText(List.get(f_id+1).get("title").toString()+"\t["+List.get(f_id+1).get("count")+"]");
				Intent intent=new Intent(SlidingActivity.this,SlidingActivity.class);
				startActivity(intent);
				finish();
			}
		});
	        file_lis.setOnTouchListener(touch);
	}
	 private List<Map<String, Object>> getData() {	//��ȡ��������	
		List = new ArrayList<Map<String, Object>>();
		Cursor cursor=wn.rawQuery("select f_id,f_name,n_counts,julianday(date('now','localtime'))-julianday(date(f_postdate)) as f_postdate from file ", null);
		int pos=0;
		while(cursor.moveToNext())
		{
		       
			int f_id=cursor.getInt(cursor.getColumnIndex("f_id"));
			fileidMap.put(pos, f_id);
			pos++;
			String f_name=cursor.getString(cursor.getColumnIndex("f_name"));
			int n_counts=cursor.getInt(cursor.getColumnIndex("n_counts"));
			Integer f_postdate=cursor.getInt(cursor.getColumnIndex("f_postdate"));
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("id", f_id);
			map.put("name", f_name);
			map.put("count", n_counts);
			map.put("postdate", f_postdate==0?getResources().getString(R.string.word_today):f_postdate+getResources().getString(R.string.word_ago));
			List.add(map);
		}
		cursor.close();
		return List;
	}

	//++++++++++++++++++++++++++��ʾ����+++++++++++++++++++++++++++++++++++++++++++
	private void showItem(){		
		SimpleAdapter myadapter = new SimpleAdapter(SlidingActivity.this,getData(),R.layout.fileitem,
			new String[]{"name","count"},
			new int[]{R.id.f_name,R.id.note_counts});
			file_lis.setVisibility(View.VISIBLE);
			file_lis.setAdapter(myadapter);		                                 //���ɼ����б�
	}
	public OnTouchListener touch = new OnTouchListener(){		//�����¼���������ʾ���ڴ�����
		@Override
		public boolean onTouch(View view, MotionEvent e) {
			float x = e.getX();
			float y = e.getY();
			switch(e.getAction()){
			case MotionEvent.ACTION_DOWN:
				mx=x;
				my=y;
				break;
			case MotionEvent.ACTION_UP:
				float dx = x-mx;
				float dy = y-my;
				if(dy>30&&dx<30){			//����ˢ��
					refreshTxt.setVisibility(View.VISIBLE);
					showItem(sort_desc,mode_list);
					Handler refreshHand = new Handler();
					Runnable refreshShow=new Runnable()		
				    {
				        @Override
				        public void run()
				        {  
				        	refreshTxt.setVisibility(View.GONE);
				        }
				    };
					refreshHand.postDelayed(refreshShow, 500);
				}
			}
			return false;
		}
	};
	@Override
	public boolean onTouchEvent(MotionEvent e){			//�����¼���������ʾ���ⴥ����
		float x = e.getX();
		float y = e.getY();
		switch(e.getAction()){
		case MotionEvent.ACTION_DOWN:
			mx=x;
			my=y;
			break;
		case MotionEvent.ACTION_UP:
			float dx = x-mx;
			float dy = y-my;
			if(dy>30&&dx<30){
				refreshTxt.setVisibility(View.VISIBLE);
				showItem(sort_desc,mode_list);
				Handler refreshHand = new Handler();
				Runnable refreshShow=new Runnable()
			    {
			        @Override
			        public void run()
			        {  
			        	refreshTxt.setVisibility(View.GONE);
			        }
			    };
				refreshHand.postDelayed(refreshShow, 500);
			}
		}
		return true;
	}

	//�������
	public TextWatcher change = new TextWatcher() {		//���������¼�
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			String key=keyTxt.getText().toString();
			String again=againTxt.getText().toString();
			if(key.length()>=6&&key.equals(again))
			{
				sp.edit().putString("key", key).commit();
				Toast.makeText(SlidingActivity.this, getResources().getString(R.string.key_success)+key,Toast.LENGTH_LONG).show();
				keyDialog.dismiss();
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
	public TextWatcher change2 = new TextWatcher() {		//�����޸��¼�
		@Override	
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			String old=keyTxt.getText().toString();
			String key=newTxt.getText().toString();
			String keyAgain=againTxt.getText().toString();
			String rkey=sp.getString("key", "");
			if(old.equals(rkey)&&key.length()>=6&&key.equals(keyAgain))
			{
				sp.edit().putString("key", key).commit();
				Toast.makeText(SlidingActivity.this, getResources().getString(R.string.key_success)+key,Toast.LENGTH_LONG).show();
				keyDialog.dismiss();
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
	public TextWatcher change3 = new TextWatcher() {		//ȡ�������¼�
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			String old=keyTxt.getText().toString();
			String rkey=sp.getString("key", "");
			if(old.equals(rkey))
			{
				sp.edit().remove("key").commit();
				Toast.makeText(SlidingActivity.this, R.string.key_canceled,Toast.LENGTH_SHORT).show();
				keyDialog.dismiss();
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
	
	
	//**********************����ı�***********************************
	//**************************************************************
	public OnFocusChangeListener focusChange=new OnFocusChangeListener(){		//����ı��¼�
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			EditText txt=(EditText)v;
			String rkey=sp.getString("key", "");
			if(!v.hasFocus()&&!txt.getText().toString().equals(rkey)&&!txt.getText().toString().equals(""))
				Toast.makeText(SlidingActivity.this, R.string.wrong_key, Toast.LENGTH_SHORT).show();		//��ʾԭ�������
		}
	};
	
	@Override
	public boolean onKeyDown(int keyCode,KeyEvent event)
	{
		if(keyCode==KeyEvent.KEYCODE_BACK){
			if((System.currentTimeMillis()-exitTime)>2000){
				Toast.makeText(SlidingActivity.this, R.string.exit_hint, Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis(); 
				return true;
			}
			else 
			{
	            finish();
	            System.exit(0);
	        }
		}
		return false;
	}
	
	
	//*************************�˵�*****************************************************
	//*********************************************************************************
	private SimpleAdapter getMenuAdapter()			//��ȡ�˵��б�
	{
		SimpleAdapter adapter = new SimpleAdapter(this,getMenu(),R.layout.menuitem,
				new String[]{"img","txt"},
				new int[]{R.id.item_img,R.id.item_txt});
		return adapter;
	}
	private List<Map<String, Object>> getMenu() {		//��ȡ�˵����������	
		int[] imgs={R.drawable.skin,R.drawable.key,R.drawable.say,R.drawable.help,R.drawable.about,R.drawable.exit};
		int[] txts={R.string.action_skin,R.string.action_key,R.string.action_say,R.string.action_help,R.string.action_about,R.string.action_exit};
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for(int i=0;i<imgs.length;i++)
		{
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("img", imgs[i]);
			map.put("txt", getResources().getString(txts[i]));
			list.add(map);
		}
		return list;
	}
	
	private OnItemClickListener itemClick=new OnItemClickListener(){			//�˵�����¼�
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			switch(position){
			case ACTION_SKIN:
				chooseColor();
				break;
			case ACTION_KEY:
				if(!sp.contains("key"))
					setKey();
				else
					editKey();
				break;
			case ACTION_SAY:
				say();
				break;
			case ACTION_HELP:
				help();
				break;
			case ACTION_ABOUT:
				about();
				break;
			case ACTION_EXIT:
				finish();
				System.exit(0);
				break;
			}
		}
	};
	
	        //++++++++++++++++++++++++++++++++++�˵��е�ѡ��Ƥ��+++++++++++++++++++++++++++++++++++	
		private void chooseColor(){		
			Dialog dialog=new Dialog(this,R.style.dialog);
			View colorView = View.inflate(this, R.layout.gridmenu, null);
			dialog.setContentView(colorView);
			GridView colorGrid=(GridView)colorView.findViewById(R.id.grid);
			colorGrid.setNumColumns(2);
			colorGrid.setAdapter(getColorAdapter());		//����Ƥ��ѡ��
			colorGrid.setOnItemClickListener(new OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> adapter, View view, int position, long id) 
				{
					if(getResources().getColor(My.colors[position])!=color)
					{
						if(position<My.colors.length-1)			//ѡ���˵�ǰƤ��
						{						
							sp.edit().putInt("color", getResources().getColor(My.colors[position])).commit();
							Intent intent=new Intent(SlidingActivity.this,SlidingActivity.class);
							intent.putExtra("needKey", false);
							startActivity(intent);
							finish();
						}
						else if(position==My.colors.length-1)		//ѡ�����µ�Ƥ��
						{
							cpDialog = new ColorPickerDialog(SlidingActivity.this, color,   
			                        getResources().getString(R.string.word_confirm),   
			                        new ColorPickerDialog.OnColorChangedListener() { 
			                    @Override  
			                    public void colorChanged(int c) 
			                    {  
									sp.edit().putInt("color", c).commit();
									Intent intent=new Intent(SlidingActivity.this,SlidingActivity.class);
									intent.putExtra("needKey", false);
									startActivity(intent);
									finish();
			                    }
			                });
							cpDialog.getWindow().setBackgroundDrawableResource(R.drawable.list_focused);
			                cpDialog.show();  
						}
					}
					else
					{
						Toast.makeText(SlidingActivity.this, R.string.now_skin, Toast.LENGTH_SHORT).show();
					}
				}
			});
			dialog.show();
		}
		private SimpleAdapter getColorAdapter()			//��ȡƤ���б�
		{
			SimpleAdapter adapter = new SimpleAdapter(this,getColor(),R.layout.menuitem,
					new String[]{"txt"},
					new int[]{R.id.item_txt});
			return adapter;
		}
		private List<Map<String, Object>> getColor() {			//��ȡ��ɫ�б�
			String[] txts=My.cs;
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
			for(int i=0;i<txts.length;i++)
			{
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("txt", txts[i]);
				list.add(map);
			}
			return list;
		}
		
		//++++++++++++++++++++++++++++�˵��е���������++++++++++++++++++++++++++++++++++++++++
		private void setKey(){			
			keyDialog=new Dialog(this,R.style.dialog);
			View keyView = View.inflate(this, R.layout.setkey, null);
			keyDialog.setContentView(keyView);
			keyTxt=(EditText)keyView.findViewById(R.id.key_txt);
			againTxt=(EditText)keyView.findViewById(R.id.again_txt);
			keyTxt.addTextChangedListener(change);
			againTxt.addTextChangedListener(change);
			keyDialog.show();
		}
		private void editKey(){			//�޸�����
			View keyView = View.inflate(this, R.layout.editkey, null);
			final Dialog dialog=new Dialog(this,R.style.dialog);
			dialog.setContentView(keyView);
			Button resetBtn=(Button)keyView.findViewById(R.id.reset_key);
			Button cancelBtn=(Button)keyView.findViewById(R.id.cancel_key);
			resetBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view) {
					resetKey();
					dialog.dismiss();
				}
			});
			cancelBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View view) {
					cancelKey();
					dialog.dismiss();
				}
			});
			dialog.show();
		}
		private void resetKey(){		//��������
			keyDialog=new Dialog(this,R.style.dialog);
			View keyView = View.inflate(this, R.layout.resetkey, null);
			keyDialog.setContentView(keyView);
			keyTxt=(EditText)keyView.findViewById(R.id.key_old);
			newTxt=(EditText)keyView.findViewById(R.id.key_new);
			againTxt=(EditText)keyView.findViewById(R.id.key_new_again);
			keyTxt.addTextChangedListener(change2);
			keyTxt.setOnFocusChangeListener(focusChange);
			newTxt.addTextChangedListener(change2);		
			againTxt.addTextChangedListener(change2);
			keyDialog.show();
		}
		private void cancelKey()			//ȡ������
		{
			keyDialog=new Dialog(this,R.style.dialog);
			View keyView = View.inflate(this, R.layout.cancelkey, null);
			keyDialog.setContentView(keyView);
			keyTxt=(EditText)keyView.findViewById(R.id.key_old);
			keyTxt.addTextChangedListener(change3);
			keyDialog.show();
		}
		
		//++++++++++++++++++++++++++�˵��еİ�������++++++++++++++++++++++++++++++++++	
	private void help(){		
		wn.execSQL("update notes set n_count=1,n_postdate=datetime('now','localtime') where id=1");
		showItem(sort_desc,mode_list);		//��ʾʹ��˵��
		menuDialog.dismiss();
	}
	
	//+++++++++++++++++++++++�˵��еĹ��ڽ���+++++++++++++++++++++++++++
	private void about(){		
		Dialog aboutDialog=new Dialog(this,R.style.dialog);
		View aboutView = View.inflate(this, R.layout.aboutme, null);
		aboutDialog.setContentView(aboutView);
		aboutDialog.show();
	}
	
	//+++++++++++++++++++++++����++++++++++++++++++++++++++++++++++++++++++++++
	@SuppressLint("SimpleDateFormat")
	private void say(){		
		Intent intent= new Intent(SlidingActivity.this,Add.class);
		Bundle data = new Bundle();
		data.putString("title",getResources().getString(R.string.word_my)+q_type+getResources().getString(R.string.action_say)+"\t\t"+new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		data.putString("content","        "+q_author+getResources().getString(R.string.word_said)+q_content+"\r\n");
		intent.putExtras(data);
		startActivity(intent);
		finish();
	}
	
	//****************************************�����б�***************************************************
	//************************************************************************************************
		private List<Map<String, Object>> getData(Boolean desc, String word) {	//��ȡ��������	
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();//�����ݿ��л�ȡ��Ӧ�ļ����µļ���
			Cursor cursor=wn.rawQuery("select id,n_title,n_content,julianday(date('now','localtime'))-julianday(date(n_postdate)) as n_postday from notes where f_id="+f_id+" order by n_postdate "+(desc!=true?"":"desc"), null);
			
			if(word.length()>0)//�ж��Ƿ����������������������е����������ݿ��в��ң�����ȫ��ȡ������
				cursor=wn.rawQuery("select id,n_title,n_content,julianday(date('now','localtime'))-julianday(date(n_postdate)) as n_postday from notes where (n_title||'`'||n_content||'`'||n_postdate||'`'||n_postday) like '%"+word+"%',f_id="+f_id+" order by n_postdate "+(desc!=true?"":"desc"), null);
			if(word.equals("#all"))
				cursor=wn.rawQuery("select id,n_title,n_content,julianday(date('now','localtime'))-julianday(date(n_postdate)) as n_postday from notes where f_id= "+f_id+"order by n_postdate "+(desc!=true?"":"desc"), null);
			sp.edit().putString("word", word).commit();
			int pos=0;
			while(cursor.moveToNext())
			{
				int n_id=cursor.getInt(cursor.getColumnIndex("id"));
				idMap.put(pos, n_id);
				pos+=1;
				String n_title=cursor.getString(cursor.getColumnIndex("n_title"));
				String n_content=cursor.getString(cursor.getColumnIndex("n_content"));
				Integer n_postdate=cursor.getInt(cursor.getColumnIndex("n_postday"));
				
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("id", n_id);
				map.put("title", n_title);
				map.put("content", n_content);
				map.put("postdate", n_postdate==0?getResources().getString(R.string.word_today):n_postdate+getResources().getString(R.string.word_ago));
				list.add(map);
			}
			cursor.close();
			return list;
		}
		
		//++++++++++++++++++++++++++��ʾ����+++++++++++++++++++++++++++++++++++++++++++
		private void showItem(Boolean desc,Boolean l){		
			String word=searchTxt.getText().toString().trim();//��ȡ����������
			SimpleAdapter myadapter = new SimpleAdapter(SlidingActivity.this,getData(desc,word),l?R.layout.listitem:R.layout.griditem,
				new String[]{"id","title","content","postdate"},
				new int[]{R.id.id,R.id.title,R.id.content,R.id.postdate});//��ȡ�����������������
			sortBtn.setImageResource(desc?R.drawable.asc:R.drawable.desc);
			modeBtn.setImageResource(l?R.drawable.grid:R.drawable.list);
			if(l)//�ж��ļ��б����ʾģʽ������ʽ�����ն�Ӧ�ķ�ʽ���ƣ�lΪtrue�����б���ʽ��ʾ
			{	
				notesLis.setVisibility(View.VISIBLE);                                   //�����б�����ʾ
				notesGrd.setVisibility(View.GONE);                                       //����������ʾ
				notesLis.setAdapter(myadapter);		                                 //���ɼ����б�
				notesLis.setOnItemClickListener(new OnItemClickListener(){		//���õ�������¼�����
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position,
							long id) {
						ListView listView =(ListView)parent;		
						@SuppressWarnings("unchecked")
						HashMap<String, Object> map = (HashMap<String, Object>) listView.getItemAtPosition(position);  
						Intent intent=new Intent(SlidingActivity.this,Note.class);
						intent.putExtra("data", map);
						startActivity(intent);
						finish();
					}
				});
			        notesLis.setOnTouchListener(touch);
				titleTxt.setText(getResources().getString(R.string.app_name)+"\t["+notesLis.getCount()+"]");
				notesLis.setOnItemLongClickListener(longClick);			//���³����¼�
			}
			else{//lΪfalse������������ʽ��ʾ�����б�
				notesGrd.setVisibility(View.VISIBLE);
				notesLis.setVisibility(View.GONE);
				notesGrd.setAdapter(myadapter);		//���ɼ�������
				notesGrd.setOnItemClickListener(new OnItemClickListener(){
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position,
							long id) {
						GridView gridView =(GridView)parent;		
						@SuppressWarnings("unchecked")
						HashMap<String, Object> map = (HashMap<String, Object>) gridView.getItemAtPosition(position);  
						Intent intent=new Intent(SlidingActivity.this,Note.class);
						intent.putExtra("data", map);
						startActivity(intent);
						finish();
					}
				});
				notesGrd.setOnTouchListener(touch);
				//titleTxt.setText(list.get(f_id).get("title").toString()+"\t["+list.get(f_id).get("count")+"]");
				titleTxt.setText(getResources().getString(R.string.app_name)+"\t["+notesLis.getCount()+"]");
				notesGrd.setOnItemLongClickListener(longClick);			//���³����¼�
			}
		}
	      //++++++++++++++++++++++++++����ɾ��+++++++++++++++++++++++++
	private OnItemLongClickListener longClick= new OnItemLongClickListener()//���������б�ĳ����¼�������������±༭����Edit		
	{
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,int position, long id) {
			Intent intent=new Intent(SlidingActivity.this,Edit.class);
			startActivity(intent);
			finish();
		  return false;
		        
		}
	};
	
	//*********************************����¼�����*********************************************
	//**************************************************************************************
	private OnClickListener click=new OnClickListener(){			//��ť����¼���������

		@Override
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.add_btn:		//�½����£�����Add����
				Intent intent= new Intent(SlidingActivity.this,Add.class);
				if(getIntent().hasExtra("title"))
					intent.putExtras(getIntent().getExtras());
				startActivity(intent);
				finish();
				break;
			case R.id.menu_btn:		        //����˵�����
				if (menuDialog == null) 
				{
					menuDialog = new Dialog(SlidingActivity.this,R.style.dialog);
					menuDialog.show();
				}
				else
				{
					menuDialog.show();
				}
				break;
			case R.id.search_btn:		        //���������������
				showHide(searchTxt);
				Add.focus(searchTxt,true);
				break;
			case R.id.mode_btn:			//����ı�����б��ģʽ
				mode_list=!mode_list;
				sp.edit().putBoolean("mode", mode_list).commit();
				showItem(sort_desc,mode_list);
				break;
			case R.id.sort_btn:			//����
				sort_desc=!sort_desc;
				sp.edit().putBoolean("sort", sort_desc).commit();
				showItem(sort_desc,mode_list);
				break;
			case R.id.title_main:		//���������
				searchTxt.setText("");
				sp.edit().remove("word").commit();
				showItem(sort_desc, mode_list);
			}
		}
	};
	
	//*************************��������****************************************
	//**********************************************************************
	private TextWatcher search=new TextWatcher(){		//�������������¼�
		@Override
		public void afterTextChanged(Editable arg0) {
		}
		@Override
		public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
		}
		@Override
		public void onTextChanged(CharSequence arg0, int arg1, int arg2,
				int arg3) {
			showItem(sort_desc, mode_list);
		}
	};
	private void showHide(View view){		//��ʾ���ص�Ԫ��
		if(view.getVisibility()==View.VISIBLE)
			view.setVisibility(View.INVISIBLE);
		else
			view.setVisibility(View.VISIBLE);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("menu");
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {		//�����ֻ�ʵ��˵��������µ��������ʾ�˵�
		menu.removeItem(0);
		if (menuDialog == null) 
		{
			menuDialog = new Dialog(SlidingActivity.this,R.style.dialog);
			menuDialog.show();
		}
		else 
		{
			menuDialog.show();
		}
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem mi){
		return super.onOptionsItemSelected(mi);
	}
}
