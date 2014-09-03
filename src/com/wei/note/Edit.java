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
//*******************���±༭���棨ʵ�ּ���ɾ�����ö�������ȣ�***************************
public class Edit extends Activity{
    
    static class ViewHolder{
   	TextView title;           //���±���
   	TextView postdate;        //����ʱ�䣬�������
   	CheckBox cb;              //��ѡ�򣬹���������ɾ���ȴ������
       }
    private Button cancel;        //ȡ����ť
    private Button All;           //ȫѡ��ť
    private ListView notesLis;		//�����б�
    private LinearLayout edit;		//����
    private int color;			//��ǰƤ����ɫ
    private TextView selected_num; //��ǰѡ�м�����Ŀ
    private ImageButton delete;    //ɾ����ť
    private ImageButton Todo;       //���찴ť��δʵ��
    private ImageButton Top;       //�ö���ť��δʵ��
    private SQLiteDatabase wn;		//���ݿ�����
    private SharedPreferences sp;	//Ĭ���������ݴ洢
    private HashMap<Integer,Integer> idMap;	//IDMap
    static Boolean flag=false;
    List<Map<String, Object>> list;
    MyAdapter myadapter;
    public static int checkNum=0; // ��¼ѡ�е���Ŀ����
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.edit);     //���ر༭����Ĳ����ļ�edit
	//�������ݿ⣬�������ò���
	wn=Database.ConnectToDatabase(this);//�������ݿ�
	sp = getSharedPreferences("setting", 0);//��ȡ��������
	idMap=new HashMap<Integer, Integer>();		                 //��ȡ����ID�б�
        color=sp.getInt("color", getResources().getColor(R.color.blue));//����Ĭ��Ƥ����ɫ
        notesLis=(ListView)findViewById(R.id.notes_lis_edit);//��ȡ�����б��Ӧ��ListView
        notesLis.setVerticalScrollBarEnabled(true);          //��������б�����Ҫ��ʱ�����
	edit=(LinearLayout)findViewById(R.id.edit);          //��ȡ�༭�����Ӧ�Ĳ�������edit
	edit.setBackgroundColor(color);                      //���ñ�����ɫ
	selected_num=(TextView)findViewById(R.id.selected_num);//��ȡ��ѡ�м�����Ŀ��Ӧ���ĵ�
	Top=(ImageButton)findViewById(R.id.Top_edit);         //��ȡ�ö���ť
	Todo=(ImageButton)findViewById(R.id.Todo);               //��ȡ���찴ť
	delete=(ImageButton)findViewById(R.id.delete_edit); //��ȡɾ����ť
	cancel=(Button)findViewById(R.id.cacel);             //��ȡȡ����ť
	cancel.setOnClickListener(new OnClickListener(){     //ȡ����ť���õ��������������󷵻�������
	    @Override
	    public void onClick(View arg0) {
		// TODO Auto-generated method stub
		Intent data=new Intent(Edit.this,SlidingActivity.class);
		startActivity(data);
		finish();
	    }
	    
	});
	All=(Button)findViewById(R.id.all); //��ȡȫѡ��ť
	All.setOnClickListener(new OnClickListener(){//���õ���������������ѡ��ȫ���ļ���
	    public void onClick(View v) {
                for (int i = 0; i < list.size(); i++) {
                    MyAdapter.getIsSelected().put(i, true);
                }
                myadapter.notifyDataSetChanged();
                checkNum = list.size();
                selected_num.setText("��ѡ��"+checkNum+"��");
}
	});
	delete.setOnClickListener(new OnClickListener(){//ɾ����ť�����������ɾ��ѡ�еļ���

	    @Override
	    public void onClick(View arg0) {
		// TODO Auto-generated method stub
		Delete();
	    }
	    
	});
	notesLis.setOnItemClickListener(new OnItemClickListener(){ //�Լ����б��Item���ü����¼��������ѡ�У����Խ��к���ɾ�����ö�������Ȳ��� 
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
                // ��TextView��ʾ
		 selected_num.setText("��ѡ��"+checkNum+"��");
	    }
	    
	});
	showItem();
    }
    private List<Map<String, Object>> getData() {	//�����ݿ��л�ȡ�������ݣ�����䵽��ǰ�ļ����б�Ĳ����ļ���	
	list = new ArrayList<Map<String, Object>>();
	//�����ݿ��л�ȡ��ǰ�ļ����µ�ȫ��������Ϣ��ID�����⣬���ݣ�����ʱ�䣩
	Cursor cursor=wn.rawQuery("select id,n_title,n_content,julianday(date('now','localtime'))-julianday(date(n_postdate)) as n_postday from notes order by n_postdate desc", null);
	int pos=0;
	while(cursor.moveToNext())//������ݵ�list�У�������showitem��������
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
private void showItem(){		
  myadapter = new MyAdapter(Edit.this,getData());
		notesLis.setVisibility(View.VISIBLE);	
		notesLis.setAdapter(myadapter);	//���ɼ����б�
		
	
}
public void Delete()//ɾ�������б���ѡ�еļ���
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
//�Զ����Adapter����������showitem��������
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


