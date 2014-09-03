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
    private Dialog menuDialog;		//菜单对话框
	private GridView menuGrid;		//菜单选项
	private View menuView;			//菜单选项视图
	
	//记事本主界面按钮
	private ImageButton addBtn;		//添加
	private ImageButton menuBtn;	        //弹出菜单
	private ImageButton searchBtn;	       //搜索
	private ImageButton modeBtn;	       //显示模式
	private ImageButton sortBtn;	      //排序
	
	//纪事布局
	private ListView notesLis;		//记事列表
	private GridView notesGrd;		//记事网格
	private TextView titleTxt;		//标题
	private LinearLayout main;		//布局
	
	private EditText keyTxt;		//密码框
	private EditText againTxt;		//密码确认框
	private EditText newTxt;		//新密码框
	private EditText searchTxt;		//搜索框
	private TextView refreshTxt;	        //刷新标签
	
	//系统配置参数
	private Integer s_id;			//记事ID
	private boolean sort_desc;		//排序标识
	private boolean mode_list;		//模式标识
	private long exitTime;			//退出时间
	private int color;			//当前皮肤颜色
	
	//引言
	private String q_content;		//引言内容
	private String q_author;		//引言作者
	private String q_type;			//引言类型
	private HashMap<Integer,Integer> idMap;	//IDMap
	private HashMap<Integer,Integer> fileidMap;	//IDMap
	
	final int ACTION_SKIN=0;	//菜单选项
	final int ACTION_KEY=1;
	final int ACTION_SAY=2;
	final int ACTION_HELP=3;
	final int ACTION_ABOUT=4;
	final int ACTION_EXIT=5;
	private float mx;		//屏幕触点坐标
	private float my;
	
	private ColorPickerDialog cpDialog;		//颜色选择对话框
	private SharedPreferences sp;			//数据存储
	private Dialog keyDialog;			//密码对话框
	private SQLiteDatabase wn;			//数据库连接
	Handler mHandler  = new Handler();
	SlidingMenu mSlidingMenu;
	public static Integer f_id=0;
	private ListView file_lis;                      //文件列表
	private LinearLayout file;                      //file布局容器
	private TextView f_refresh;               
	List<Map<String, Object>> List;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//连接数据库，加载配置参数
		mSlidingMenu = new SlidingMenu(this);
		setContentView(mSlidingMenu);                  //加载SlideMenu布局文件
		View menu = getLayoutInflater().inflate(R.layout.file, null);//按照file布局文件绘制menu视图
		View content = getLayoutInflater().inflate(R.layout.main, null);//按照main布局文件绘制menu视图
		mSlidingMenu.setMenu(menu);                                //为Slidemenu添加Content和Menu视图
		mSlidingMenu.setContent(content);		
	        mHandler.post(new Runnable() {
			
			@Override
			public void run() {
//				mSlidingMenu.showMenu();
			}
		}); 	
		wn=Database.ConnectToDatabase(this);             //连接数据库
		sp = getSharedPreferences("setting", 0);         //获取默认设置数据
		idMap=new HashMap<Integer, Integer>();	
		fileidMap=new HashMap<Integer, Integer>();	//获取记事ID列表
                color=sp.getInt("color", getResources().getColor(R.color.blue));//加载默认皮肤颜色
		main=(LinearLayout)content.findViewById(R.id.main);//获取main布局容器
		main.setBackgroundColor(color);                    //main设置背景颜色
		//加载UI组件
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
		int width=getWindowManager().getDefaultDisplay().getWidth();	//获取屏幕宽度
		notesGrd.setNumColumns(width/120);			       //设置网格布局列数
		
		//加载引言
		q_content=sp.getString("q_content", "");
		q_author=sp.getString("q_author", "");
		q_type=sp.getString("q_type", "");
		
		//为按钮添加事件监听器
		ImageButton[] btns={addBtn,menuBtn,searchBtn,modeBtn,sortBtn};
		for(ImageButton btn:btns)
			btn.setOnClickListener(click);
		
		sort_desc=sp.getBoolean("sort", true);		//获取排序方式
		mode_list=sp.getBoolean("mode", true);		//获取显示模式
		
		menuDialog = new Dialog(this,R.style.dialog);		//自定义菜单
		menuView = View.inflate(this, R.layout.gridmenu, null);
		menuDialog.setContentView(menuView);
		menuDialog.setOnKeyListener(new OnKeyListener(){       //设置点击手机菜单按钮响应事件
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_MENU)
				dialog.dismiss();
				return false;
			}
		});
		
		//填充菜单项
		menuGrid=(GridView)menuView.findViewById(R.id.grid);
		menuGrid.setAdapter(getMenuAdapter());		//设置菜单项
		menuGrid.setOnItemClickListener(itemClick);
		
		//纪事搜索
		searchTxt=(EditText)findViewById(R.id.search_txt);
		searchTxt.setBackgroundColor(color);
		searchTxt.addTextChangedListener(search);
		searchTxt.setText(sp.getString("word", ""));
		
		//纪事点击事件
		titleTxt.setOnClickListener(click);
		refreshTxt=(TextView)findViewById(R.id.refresh_txt);
		
		Long lastdate=sp.getLong("lastdate", new Date().getTime());		//更新记事保存时间
		sp.edit().putLong("lastdate",new Date().getTime()).commit();
		showItem(sort_desc,mode_list);
		showItem();
		file_lis.setOnItemClickListener(new OnItemClickListener(){		//点击记事事件
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
	 private List<Map<String, Object>> getData() {	//获取记事数据	
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

	//++++++++++++++++++++++++++显示记事+++++++++++++++++++++++++++++++++++++++++++
	private void showItem(){		
		SimpleAdapter myadapter = new SimpleAdapter(SlidingActivity.this,getData(),R.layout.fileitem,
			new String[]{"name","count"},
			new int[]{R.id.f_name,R.id.note_counts});
			file_lis.setVisibility(View.VISIBLE);
			file_lis.setAdapter(myadapter);		                                 //生成记事列表
	}
	public OnTouchListener touch = new OnTouchListener(){		//触摸事件（记事显示区内触摸）
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
				if(dy>30&&dx<30){			//下拉刷新
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
	public boolean onTouchEvent(MotionEvent e){			//触摸事件（记事显示区外触摸）
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

	//密码操作
	public TextWatcher change = new TextWatcher() {		//密码设置事件
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
	public TextWatcher change2 = new TextWatcher() {		//密码修改事件
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
	public TextWatcher change3 = new TextWatcher() {		//取消密码事件
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
	
	
	//**********************焦点改变***********************************
	//**************************************************************
	public OnFocusChangeListener focusChange=new OnFocusChangeListener(){		//焦点改变事件
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			EditText txt=(EditText)v;
			String rkey=sp.getString("key", "");
			if(!v.hasFocus()&&!txt.getText().toString().equals(rkey)&&!txt.getText().toString().equals(""))
				Toast.makeText(SlidingActivity.this, R.string.wrong_key, Toast.LENGTH_SHORT).show();		//提示原密码错误
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
	
	
	//*************************菜单*****************************************************
	//*********************************************************************************
	private SimpleAdapter getMenuAdapter()			//获取菜单列表
	{
		SimpleAdapter adapter = new SimpleAdapter(this,getMenu(),R.layout.menuitem,
				new String[]{"img","txt"},
				new int[]{R.id.item_img,R.id.item_txt});
		return adapter;
	}
	private List<Map<String, Object>> getMenu() {		//获取菜单，绘制填充	
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
	
	private OnItemClickListener itemClick=new OnItemClickListener(){			//菜单点击事件
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
	
	        //++++++++++++++++++++++++++++++++++菜单中的选择皮肤+++++++++++++++++++++++++++++++++++	
		private void chooseColor(){		
			Dialog dialog=new Dialog(this,R.style.dialog);
			View colorView = View.inflate(this, R.layout.gridmenu, null);
			dialog.setContentView(colorView);
			GridView colorGrid=(GridView)colorView.findViewById(R.id.grid);
			colorGrid.setNumColumns(2);
			colorGrid.setAdapter(getColorAdapter());		//设置皮肤选项
			colorGrid.setOnItemClickListener(new OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> adapter, View view, int position, long id) 
				{
					if(getResources().getColor(My.colors[position])!=color)
					{
						if(position<My.colors.length-1)			//选择了当前皮肤
						{						
							sp.edit().putInt("color", getResources().getColor(My.colors[position])).commit();
							Intent intent=new Intent(SlidingActivity.this,SlidingActivity.class);
							intent.putExtra("needKey", false);
							startActivity(intent);
							finish();
						}
						else if(position==My.colors.length-1)		//选择了新的皮肤
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
		private SimpleAdapter getColorAdapter()			//获取皮肤列表
		{
			SimpleAdapter adapter = new SimpleAdapter(this,getColor(),R.layout.menuitem,
					new String[]{"txt"},
					new int[]{R.id.item_txt});
			return adapter;
		}
		private List<Map<String, Object>> getColor() {			//获取颜色列表
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
		
		//++++++++++++++++++++++++++++菜单中的设置密码++++++++++++++++++++++++++++++++++++++++
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
		private void editKey(){			//修改密码
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
		private void resetKey(){		//重置密码
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
		private void cancelKey()			//取消密码
		{
			keyDialog=new Dialog(this,R.style.dialog);
			View keyView = View.inflate(this, R.layout.cancelkey, null);
			keyDialog.setContentView(keyView);
			keyTxt=(EditText)keyView.findViewById(R.id.key_old);
			keyTxt.addTextChangedListener(change3);
			keyDialog.show();
		}
		
		//++++++++++++++++++++++++++菜单中的帮助界面++++++++++++++++++++++++++++++++++	
	private void help(){		
		wn.execSQL("update notes set n_count=1,n_postdate=datetime('now','localtime') where id=1");
		showItem(sort_desc,mode_list);		//显示使用说明
		menuDialog.dismiss();
	}
	
	//+++++++++++++++++++++++菜单中的关于界面+++++++++++++++++++++++++++
	private void about(){		
		Dialog aboutDialog=new Dialog(this,R.style.dialog);
		View aboutView = View.inflate(this, R.layout.aboutme, null);
		aboutDialog.setContentView(aboutView);
		aboutDialog.show();
	}
	
	//+++++++++++++++++++++++感悟++++++++++++++++++++++++++++++++++++++++++++++
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
	
	//****************************************记事列表***************************************************
	//************************************************************************************************
		private List<Map<String, Object>> getData(Boolean desc, String word) {	//获取记事数据	
			List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();//从数据库中获取对应文件夹下的记事
			Cursor cursor=wn.rawQuery("select id,n_title,n_content,julianday(date('now','localtime'))-julianday(date(n_postdate)) as n_postday from notes where f_id="+f_id+" order by n_postdate "+(desc!=true?"":"desc"), null);
			
			if(word.length()>0)//判断是否有搜索，有则按照搜索框中的内容在数据库中查找，否则全部取出数据
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
		
		//++++++++++++++++++++++++++显示记事+++++++++++++++++++++++++++++++++++++++++++
		private void showItem(Boolean desc,Boolean l){		
			String word=searchTxt.getText().toString().trim();//获取搜索框内容
			SimpleAdapter myadapter = new SimpleAdapter(SlidingActivity.this,getData(desc,word),l?R.layout.listitem:R.layout.griditem,
				new String[]{"id","title","content","postdate"},
				new int[]{R.id.id,R.id.title,R.id.content,R.id.postdate});//获取适配器，并填充数据
			sortBtn.setImageResource(desc?R.drawable.asc:R.drawable.desc);
			modeBtn.setImageResource(l?R.drawable.grid:R.drawable.list);
			if(l)//判断文件列表的显示模式和排序方式，按照对应的方式绘制，l为true，则按列表形式显示
			{	
				notesLis.setVisibility(View.VISIBLE);                                   //设置列表型显示
				notesGrd.setVisibility(View.GONE);                                       //隐藏网格显示
				notesLis.setAdapter(myadapter);		                                 //生成记事列表
				notesLis.setOnItemClickListener(new OnItemClickListener(){		//设置点击记事事件监听
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
				notesLis.setOnItemLongClickListener(longClick);			//记事长按事件
			}
			else{//l为false，按照网格形式显示记事列表
				notesGrd.setVisibility(View.VISIBLE);
				notesLis.setVisibility(View.GONE);
				notesGrd.setAdapter(myadapter);		//生成记事网格
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
				notesGrd.setOnItemLongClickListener(longClick);			//记事长按事件
			}
		}
	      //++++++++++++++++++++++++++长按删除+++++++++++++++++++++++++
	private OnItemLongClickListener longClick= new OnItemLongClickListener()//监听记事列表的长按事件，长按进入记事编辑界面Edit		
	{
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,int position, long id) {
			Intent intent=new Intent(SlidingActivity.this,Edit.class);
			startActivity(intent);
			finish();
		  return false;
		        
		}
	};
	
	//*********************************点击事件监听*********************************************
	//**************************************************************************************
	private OnClickListener click=new OnClickListener(){			//按钮点击事件监听处理

		@Override
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.add_btn:		//新建记事，进入Add界面
				Intent intent= new Intent(SlidingActivity.this,Add.class);
				if(getIntent().hasExtra("title"))
					intent.putExtras(getIntent().getExtras());
				startActivity(intent);
				finish();
				break;
			case R.id.menu_btn:		        //进入菜单界面
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
			case R.id.search_btn:		        //进入记事搜索界面
				showHide(searchTxt);
				Add.focus(searchTxt,true);
				break;
			case R.id.mode_btn:			//点击改变记事列表的模式
				mode_list=!mode_list;
				sp.edit().putBoolean("mode", mode_list).commit();
				showItem(sort_desc,mode_list);
				break;
			case R.id.sort_btn:			//排序
				sort_desc=!sort_desc;
				sp.edit().putBoolean("sort", sort_desc).commit();
				showItem(sort_desc,mode_list);
				break;
			case R.id.title_main:		//点击标题栏
				searchTxt.setText("");
				sp.edit().remove("word").commit();
				showItem(sort_desc, mode_list);
			}
		}
	};
	
	//*************************搜索纪事****************************************
	//**********************************************************************
	private TextWatcher search=new TextWatcher(){		//处理搜索记事事件
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
	private void showHide(View view){		//显示隐藏的元素
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
	public boolean onMenuOpened(int featureId, Menu menu) {		//处理手机实体菜单键被按下的情况，显示菜单
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
