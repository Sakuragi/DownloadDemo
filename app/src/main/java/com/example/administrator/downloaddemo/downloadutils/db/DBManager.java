package com.example.administrator.downloaddemo.downloadutils.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;

import com.example.administrator.downloaddemo.downloadutils.bean.DownloadInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jim on 2017/7/20.
 */

public class DBManager {

    private DBHelper helper;
    public static DBManager sDBManager;

    public static DBManager getInstance(Context context) {
        if (sDBManager == null) {
            synchronized (DBManager.class) {
                if (sDBManager == null) {
                    sDBManager = new DBManager(context);
                }
            }
        }
        return sDBManager;
    }

    private DBManager(Context context) {
        helper = new DBHelper(context);
    }

    public void saveInfos(List<DownloadInfo> infos) {
        SQLiteDatabase db = helper.getWritableDatabase();
        for (DownloadInfo info : infos) {
            db.execSQL("insert into downloadinfo(thread_id,start_pos, end_pos,compelete_size,url) values (?,?,?,?,?)",
                    new Object[]{info.getThreadId(), info.getStartPos(), info.getEndPos(), info.getCompeleteSize(), info.getUrl()});
        }
    }


    public void saveInfo(DownloadInfo info) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("insert into downloadinfo(thread_id,start_pos, end_pos,compelete_size,url) values (?,?,?,?,?)",
                new Object[]{info.getThreadId(), info.getStartPos(), info.getEndPos(), info.getCompeleteSize(), info.getUrl()});
    }


    /**
     * 查看数据库中是否有数据
     */
    public boolean isHasInfos(String urlstr) {
        SQLiteDatabase db = helper.getWritableDatabase();
//        String sql = "select count(*) from downloadinfo where url=?";
        Cursor cursor = db.query("downloadinfo", null, "url = ?", new String[]{urlstr},
                null, null, null);
        boolean exists = cursor.moveToNext();
        cursor.close();
        return exists;
    }


    public void closeDb() {
        helper.close();
    }

    public void delete(final String url) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete("downloadinfo", "url=?", new String[]{url});
        db.close();
    }

    public void updataInfos(int threadId, int compeleteSize, String urlstr) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "update downloadinfo set compelete_size=? where thread_id=? and url=?";
        Object[] bindArgs = {compeleteSize, threadId, urlstr};
        db.execSQL(sql, bindArgs);
    }

    /**
     * 得到下载具体信息
     */
    public List<DownloadInfo> getInfos(String urlstr) {
        SQLiteDatabase db = helper.getWritableDatabase();
        List<DownloadInfo> list = new ArrayList<DownloadInfo>();
        String sql = "select thread_id, start_pos, end_pos,compelete_size,url from downloadinfo where url=?";
        Cursor cursor = db.rawQuery(sql, new String[]{urlstr});
        while (cursor.moveToNext()) {
            DownloadInfo info = new DownloadInfo(cursor.getInt(0),
                    cursor.getInt(1), cursor.getInt(2), cursor.getInt(3),
                    cursor.getString(4));
            list.add(info);
        }
        cursor.close();
        return list;
    }

    public DownloadInfo getInfo(String urlstr, int threadid) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "select thread_id, start_pos, end_pos,compelete_size,url from downloadinfo where url=? and thread_id=?";
        Cursor cursor = db.rawQuery(sql, new String[]{urlstr, String.valueOf(threadid)});
        cursor.moveToFirst();
        DownloadInfo info = new DownloadInfo(cursor.getInt(cursor.getColumnIndex("thread_id")),
                cursor.getInt(cursor.getColumnIndex("start_pos")), cursor.getInt(cursor.getColumnIndex("end_pos")),
                cursor.getInt(cursor.getColumnIndex("compelete_size")), cursor.getString(cursor.getColumnIndex("url")));
        Log.d("DB", "get db info print: " + cursor.getInt(cursor.getColumnIndex("thread_id")) + " " +
                cursor.getInt(cursor.getColumnIndex("start_pos")) + " " + cursor.getInt(cursor.getColumnIndex("end_pos")) + " " + cursor.getInt(cursor.getColumnIndex("compelete_size")) + " ");
        cursor.close();
        return info;
    }


}
