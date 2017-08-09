package com.example.administrator.downloaddemo.downloadutils.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Jim on 2017/7/24.
 */

public class NetWorkUtil {

    public static boolean isNetworkAvailable(Context context){
        ConnectivityManager connectivity=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity!=null){
            NetworkInfo networkinfo=connectivity.getActiveNetworkInfo();
            if (networkinfo!=null&&networkinfo.isConnected()){
                if (networkinfo.getState()==NetworkInfo.State.CONNECTED){
                    return true;
                }
            }
        }
        return false;
    }

}
