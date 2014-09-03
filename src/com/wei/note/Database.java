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
    //******************�������ݿ�*****************
public static SQLiteDatabase ConnectToDatabase(Context context) {
        try {
        	int BUFFER_SIZE = 100000;//һ�ζ�ȡ������󻺴�
        	String DB_NAME = "dribs.db"; //���ݿ���
        	String PACKAGE_NAME = "com.swimmi.windnote";//����
        	String DB_PATH = "/data"//���ݿ��ַ
                + Environment.getDataDirectory().getAbsolutePath() + "/"
                + PACKAGE_NAME+"/databases/";
        	File destDir = new File(DB_PATH);
        	  if (!destDir.exists()) {
        	   destDir.mkdirs();
        	  }
        	String file=DB_PATH+DB_NAME;
        	if (!(new File(file).exists())) {
                InputStream is = context.getResources().openRawResource(//�����ݿ�
                        R.raw.wind);
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[BUFFER_SIZE];
                int count = 0;
                while ((count = is.read(buffer)) > 0) {//��ȡ���ݿ��е�����
                    fos.write(buffer, 0, count);
                }
                fos.close();//�ر����ݿ�����
                is.close();
            }
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(file,null);
            return db;
        } catch (FileNotFoundException e) {//�쳣�������
            Log.e("Database", "File not found");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("Database", "IO exception");
            e.printStackTrace();
        }
        return null;
    }
}