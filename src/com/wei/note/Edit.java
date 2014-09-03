package com.wei.note;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.swimmi.windnote.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
//*******************记事编辑界面（实现记事删除，置顶，待办等）***************************
public class Edit extends Activity{
    
    static class ViewHolder{
   	TextView title;           //记事标题
   	TextView postdate;        //创建时间，按天计算
   	CheckBox cb;              //复选框，供记事批量删除等处理操作
       }
    private Button cancel;        //取消按钮
    private Button All;           //全选按钮
    private ListView notesLis;		//记事列表
    private LinearLayout edit;		//布局
    private int color;			//当前皮肤颜色
    private TextView selected_num; //当前选中记事数目
    private ImageButton delete;    //删除按钮
    private ImageButton Todo;       //待办按钮，未实现
    private ImageButton Top;       //置顶按钮，未实现
    private SQLiteDatabase wn;		//数据库连接
    private SharedPreferences sp;	//默认设置数据存储
    private HashMap<Integer,Integer> idMap;	//IDMap
    static Boolean flag=false;
    List<Map<String, Object>> list;
    MyAdapter myadapter;
    public static int checkNum=0; // 记录选中的条目数量
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.edit);     //加载编辑界面的布局文件edit
	//连接数据库，加载配置参数
	wn=Database.ConnectToDatabase(this);//连接数据库
	sp = getSharedPreferences("setting", 0);//获取设置数据
	idMap=new HashMap<Integer, Integer>();		                 //获取记事ID列表
        color=sp.getInt("color", getResources().getColor(R.color.blue));//加载默认皮肤颜色
        notesLis=(ListView)findViewById(R.id.notes_lis_edit);//获取记事列表对应的ListView
        notesLis.setVerticalScrollBarEnabled(true);          //允许记事列表在需要的时候滚动
	edit=(LinearLayout)findViewById(R.id.edit);          //获取编辑界面对应的布局容器edit
	edit.setBackgroundColor(color);                      //设置背景颜色
	selected_num=(TextView)findViewById(R.id.selected_num);//获取已选中记事数目对应的文档
	Top=(ImageButton)findViewById(R.id.Top_edit);         //获取置顶按钮
	Todo=(ImageButton)findViewById(R.id.Todo);               //获取待办按钮
	delete=(ImageButton)findViewById(R.id.delete_edit); //获取删除按钮
	cancel=(Button)findViewById(R.id.cacel);             //获取取消按钮
	cancel.setOnClickListener(new OnClickListener(){     //取消按钮设置点击监听器，点击后返回主界面
	    @Override
	    public void onClick(View arg0) {
		// TODO Auto-generated method stub
		Intent data=new Intent(Edit.this,SlidingActivity.class);
		startActivity(data);
		finish();
	    }
	    
	});
	All=(Button)findViewById(R.id.all); //获取全选按钮
	All.setOnClickListener(new OnClickListener(){//设置点击监听器，点击后选择全部的记事
	    public void onClick(View v) {
                for (int i = 0; i < list.size(); i++) {
                    MyAdapter.getIsSelected().put(i, true);
                }
                myadapter.notifyDataSetChanged();
                checkNum = list.size();
                selected_num.setText("已选中"+checkNum+"项");
}
	});
	delete.setOnClickListener(new OnClickListener(){//删除按钮监听，点击后删除选中的记事

	    @Override
	    public void onClick(View arg0) {
		// TODO Auto-generated method stub
		Delete();
	    }
	    
	});
	notesLis.setOnItemClickListener(new OnItemClickListener(){ //对记事列表的Item设置监听事件，点击则选中，可以进行后续删除、置顶、待办等操作 
	    @Override
	    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
		    long arg3) {
                flag=true;
		CheckBox cb=(CheckBox)arg1.findViewById(R.id.e_cb);
		cb.toggle();
		if(cb.isChecked())
		{
		MyAdapter.getIsSelected().put(arg2, true);
		checkNum++;
		}
		else
		{
		   MyAdapter.getIsSelected().put(arg2, false);
		   checkNum--;
		   
		}
                // 用TextView显示
		 selected_num.setText("已选中"+checkNum+"项");
	    }
	    
	});
	showItem();
    }
    private List<Map<String, Object>> getData() {	//从数据库中获取记事数据，并填充到当前的记事列表的布局文件中	
	list = new ArrayList<Map<String, Object>>();
	//从数据库中获取当前文件夹下的全部记事信息（ID，标题，内容，创建时间）
	Cursor cursor=wn.rawQuery("select id,n_title,n_content,julianday(date('now','localtime'))-julianday(date(n_postdate)) as n_postday from notes order by n_postdate desc", null);
	int pos=0;
	while(cursor.moveToNext())//填充数据到list中，供后面showitem函数调用
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
private void showItem(){		
  myadapter = new MyAdapter(Edit.this,getData());
		notesLis.setVisibility(View.VISIBLE);	
		notesLis.setAdapter(myadapter);	//生成记事列表
		
	
}
public void Delete()//删除记事列表中选中的记事
{
    for(int i=0;i<list.size();i++)
    {
	if(MyAdapter.getIsSelected().get(i))
	{
	    int id=idMap.get(i);
	    wn.execSQL("delete from notes where id="+id);
	    wn.execSQL("update file set n_counts=n_counts-1 where f_id="+SlidingActivity.f_id);
	   
	}
    }
    checkNum=0;
    Intent data=new Intent(Edit.this,SlidingActivity.class);
    startActivity(data);
    finish();
}
public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu; this adds items to the action bar if it is present.
	return true;
}
}
//自定义的Adapter适配器，供showitem函数调用
class MyAdapter extends BaseAdapter
{
    private Context context;
    private List<Map<String, Object>> list;
    private LayoutInflater layoutInflater=null;
    private static HashMap<Integer,Boolean> isSelected;
    static class ViewHolder{
	TextView title;
	TextView postdate;
	CheckBox cb;
    }
    public MyAdapter(Context context, List<Map<String, Object>> list)
    {
	this.context=context;
	layoutInflater = LayoutInflater.from(context);
	this.list=list;
	isSelected = new HashMap<Integer, Boolean>();
        init();
    }
    private void init(){
        for(int i=0; i<list.size();i++) {
            getIsSelected().put(i,false);
        }
    }
    public static HashMap<Integer,Boolean> getIsSelected() {
        return isSelected;
    }
    public static void setIsSelected(HashMap<Integer,Boolean> isSelected) {
        MyAdapter.isSelected = isSelected;
    }
    public View getView(final int position, View convertView, ViewGroup parent) 
    {
	ViewHolder holder = null;
	if(convertView==null)
	{
	    holder = new ViewHolder();
	    convertView=layoutInflater.inflate(R.layout.editlistitem, null);
	    holder.cb=(CheckBox)convertView.findViewById(R.id.e_cb);
	    holder.title=(TextView)convertView.findViewById(R.id.e_title);
	    holder.postdate=(TextView)convertView.findViewById(R.id.e_postdate);
	    holder.cb.setOnCheckedChangeListener(new OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		    // TODO Auto-generated method stub
		    isSelected.put(position, arg1);
		  
		}
		
	    });
	    convertView.setTag(holder);
	}
	else
	    holder = (ViewHolder) convertView.getTag();
	holder.title.setText(list.get(position).get("title").toString());
	holder.postdate.setText(list.get(position).get("postdate").toString());
	holder.cb.setChecked(isSelected.get(position));
	 return convertView;
    }
    @Override
    public int getCount() {
	// TODO Auto-generated method stub
	return this.list.size();
    }
    @Override
    public Object getItem(int arg0) {
	// TODO Auto-generated method stub
	return list.get(arg0);
    }
    @Override
    public long getItemId(int arg0) {
	// TODO Auto-generated method stub
	return arg0;
    } 
}


