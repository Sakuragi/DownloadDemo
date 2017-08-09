package com.example.administrator.downloaddemo.downloadutils.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

import com.example.administrator.downloaddemo.downloadutils.download.FileDownloader;
import com.example.administrator.downloaddemo.downloadutils.utils.NetWorkUtil;

/**
 * Created by Jim on 2017/7/25.
 */

public class NetWorkChangeReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("TAG","run in onReceive");
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            if (!NetWorkUtil.isNetworkAvailable(context)){
                Toast.makeText(context,"暂停下载，网络不可用",Toast.LENGTH_SHORT).show();
                Log.d("TAG","暂停所有");
                FileDownloader.getInstance().pauseAll();
            }

        }
    }
}
