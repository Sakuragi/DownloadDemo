package com.example.administrator.downloaddemo.downloadutils.utils;

import android.content.Context;
import android.os.Handler;

import com.example.administrator.downloaddemo.downloadutils.state.DownloadState;

/**
 * Created by Jim on 2017/7/20.
 */

public class DownLoaderController {

    private DownloadState mState;

    public void setDownloadState(DownloadState state){
        mState=state;
    }

    public void startDownload(Context context, Handler handler,String downloadurl, String filename, int threadcount){
        mState.startDownload(context, handler, downloadurl, filename, threadcount);
    }

    public void stopDownload(Context context, Handler handler, String downloadurl, String filename, int threadcount){
        mState.pauseDownload(context, handler, downloadurl, filename);
    }

}
