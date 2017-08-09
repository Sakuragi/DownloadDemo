package com.example.administrator.downloaddemo.downloadutils.download;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.administrator.downloaddemo.downloadutils.bean.DownloadInfo;
import com.example.administrator.downloaddemo.downloadutils.constant.Constant;
import com.example.administrator.downloaddemo.downloadutils.db.DBManager;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by Jim on 2017/7/25.
 */

public class DownloadTask implements Runnable {

    private final String TAG=DownloadTask.class.getSimpleName();

    private Context mContext;
    private Handler mHandler;
    private String mDownloadurl;
    private RandomAccessFile mRandomAccessFile;
    private String mFilename;
    private int size;
    private int threadId;

    public DownloadTask(Context context, Handler handler, String downloadurl, int filesize, String filename, int threadid) {
        mContext = context;
        mHandler = handler;
        mDownloadurl = downloadurl;
        mFilename = filename;
        threadId = threadid;
        size = filesize;
    }


    private int calculateCompeltesize() {
        int compeltesize = 0;
        List<DownloadInfo> infos = DBManager.getInstance(mContext).getInfos(mDownloadurl);
        if (infos == null) {
            return 0;
        }
        for (DownloadInfo info : infos) {
            compeltesize += info.getCompeleteSize();
        }
        return compeltesize;
    }


    private void sendMessage(int what, int arg1, int arg2, Object obj) {
        Message message = new Message();
        message.what = what;
        if (arg1 != -1) {
            message.arg1 = arg1;
        }
        if (arg2 != -1) {
            message.arg2 = arg2;
        }
        if (obj != null) {
            message.obj = obj;
        }
        message.setTarget(mHandler);
        message.sendToTarget();
    }

    @Override
    public void run() {
        FileDownloader.getInstance().putDownloadState(mDownloadurl, Constant.DOWNLOAD_STATE_START);
        HttpURLConnection connection = null;
        BufferedInputStream inputStream = null;
        DownloadInfo info = new DownloadInfo();
        Log.d(TAG, "is has info: " + DBManager.getInstance(mContext).isHasInfos(mDownloadurl));
        if (DBManager.getInstance(mContext).isHasInfos(mDownloadurl)) {     //判断是否存在未完成的该任务
            info = DBManager.getInstance(mContext).getInfo(mDownloadurl, threadId);
        }
        try {
            URL url = new URL(mDownloadurl);
            int compeltesize = info.getCompeleteSize();
            Log.d(TAG,"first completesize is: "+compeltesize);
            int startPos = info.getStartPos();      //本地数据库中的保存的开始位置跟结束位置
            int endPos = info.getEndPos();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Range", "bytes=" + startPos+compeltesize + "-" + endPos);
            inputStream = new BufferedInputStream(connection.getInputStream());
            mRandomAccessFile = new RandomAccessFile(mFilename, "rw");
            mRandomAccessFile.seek(startPos+compeltesize);         //上次的最后的写入位置
            Log.d(TAG, "seek position: " + startPos + "  thread id: " + threadId);
            byte[] buffer = new byte[8 * 1024];
            int length = 0;
            while ((length = inputStream.read(buffer)) > 0) {
                if (FileDownloader.getInstance().getDownloadState(mDownloadurl) == Constant.DOWNLOAD_STATE_PAUSE) { //下载任务被暂停
                    return;
                }
//                Log.d(TAG, "write file length: " + length);
                mRandomAccessFile.write(buffer, 0, length);
                compeltesize += length;
//                Log.d(TAG,"save completesize is: "+compeltesize);
                DBManager.getInstance(mContext).updataInfos(threadId, compeltesize, mDownloadurl);  //保存数据库中的下载进度
                sendMessage(Constant.DOWNLOAD_KEEP, calculateCompeltesize(), -1, null);     //更新进度条
            }
            Log.d(TAG, "calculateCompeltesize: " + calculateCompeltesize() + " filesize: " + size + "threadid: " + threadId);
            if (calculateCompeltesize() >= size) {      //判断下载是否完成
                sendMessage(Constant.DOWNLOAD_COMPLETE, -1, -1, mDownloadurl);
            }
        } catch (Exception e) {
            sendMessage(Constant.DOWNLOAD_FAIL, -1, -1, mDownloadurl);
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
                if (mRandomAccessFile != null) {
                    mRandomAccessFile.close();
                }
                sendMessage(Constant.DOWNLOAD_ClLEAN, -1, -1, mDownloadurl);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
