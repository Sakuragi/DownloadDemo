package com.example.administrator.downloaddemo.downloadutils.state;

import android.content.Context;
import android.os.Handler;

/**
 * Created by Jim on 2017/7/20.
 */

public interface DownloadState {

    void startDownload(Context context, Handler handler, String downloadurl, String filename, int threadcount);
    void pauseDownload(Context context, Handler handler,String downloadurl, String filename);

}
