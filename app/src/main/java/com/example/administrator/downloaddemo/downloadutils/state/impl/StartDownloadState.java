package com.example.administrator.downloaddemo.downloadutils.state.impl;

import android.content.Context;
import android.os.Handler;

import com.example.administrator.downloaddemo.downloadutils.state.DownloadState;
import com.example.administrator.downloaddemo.downloadutils.download.FileDownloader;
import com.example.administrator.downloaddemo.downloadutils.utils.ThreadPoolsUtil;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Jim on 2017/7/20.
 */

public class StartDownloadState implements DownloadState {


    @Override
    public void startDownload(final Context context, final Handler handler, final String downloadurl, final String filename, final int threadcount) {
        ThreadPoolsUtil.getInstance().getFixedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                int filesize=getDownloadFilesize(downloadurl);
                FileDownloader.getInstance().init(context,handler,downloadurl,filesize,filename,threadcount).startDownload();

            }
        });
    }

    private int getDownloadFilesize(String downloadurl){
        HttpURLConnection connection=null;
        int filesize=-1;
        try {
            URL url=new URL(downloadurl);
            connection= (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            filesize=connection.getContentLength();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (connection!=null){
                    connection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return filesize;
    }

    @Override
    public void pauseDownload(Context context,Handler handler,String downloadurl,String filename) {

    }
}
