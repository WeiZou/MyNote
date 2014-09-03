package com.wei.note;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.swimmi.windnote.R;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
public class Database
{
    //******************连接数据库*****************
public static SQLiteDatabase ConnectToDatabase(Context context) {
        try {
        	int BUFFER_SIZE = 100000;//一次读取数据最大缓存
        	String DB_NAME = "dribs.db"; //数据库名
        	String PACKAGE_NAME = "com.swimmi.windnote";//包名
        	String DB_PATH = "/data"//数据库地址
                + Environment.getDataDirectory().getAbsolutePath() + "/"
                + PACKAGE_NAME+"/databases/";
        	File destDir = new File(DB_PATH);
        	  if (!destDir.exists()) {
        	   destDir.mkdirs();
        	  }
        	String file=DB_PATH+DB_NAME;
        	if (!(new File(file).exists())) {
                InputStream is = context.getResources().openRawResource(//打开数据库
                        R.raw.wind);
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[BUFFER_SIZE];
                int count = 0;
                while ((count = is.read(buffer)) > 0) {//读取数据库中的数据
                    fos.write(buffer, 0, count);
                }
                fos.close();//关闭数据库连接
                is.close();
            }
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(file,null);
            return db;
        } catch (FileNotFoundException e) {//异常处理机制
            Log.e("Database", "File not found");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("Database", "IO exception");
            e.printStackTrace();
        }
        return null;
    }
}