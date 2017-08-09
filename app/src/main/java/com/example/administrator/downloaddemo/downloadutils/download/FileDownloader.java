package com.example.administrator.downloaddemo.downloadutils.download;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.administrator.downloaddemo.downloadutils.constant.Constant;
import com.example.administrator.downloaddemo.downloadutils.bean.DownloadInfo;
import com.example.administrator.downloaddemo.downloadutils.db.DBManager;
import com.example.administrator.downloaddemo.downloadutils.receiver.NetWorkChangeReceiver;
import com.example.administrator.downloaddemo.downloadutils.utils.ThreadPoolsUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

/**
 * Created by Jim on 2017/7/21.
 */

public class FileDownloader {

    private final String TAG = FileDownloader.class.getSimpleName();

    public static FileDownloader sFileDownloader;
    private Context context;
    private Handler handler;
    private String downloadurl;
    private int filesize;
    private String filename;
    private int threadCount;
    private HashMap<String, Integer> downloadStatemap;

    private int state = Constant.DOWNLOAD_STATE_INIT;

    public static FileDownloader getInstance() {
        if (sFileDownloader == null) {
            synchronized (FileDownloader.class) {
                if (sFileDownloader == null) {
                    sFileDownloader = new FileDownloader();
                }
            }
        }
        return sFileDownloader;
    }

    public synchronized FileDownloader init(Context context, Handler handler,
                                            String downloadurl, int filesize,
                                            String filename, int threadCount) {
        Log.d(TAG, "Run in init");
        this.context = context;
        this.handler = handler;
        this.downloadurl = downloadurl;
        this.filesize = filesize;
        this.filename = filename;
        this.threadCount = threadCount;
        if (downloadStatemap == null) {
            downloadStatemap = new HashMap<>();
        }
        initDatas();
        return this;
    }


    private void initDatas() {
        RandomAccessFile accessFile = null;
        File file;
        int block = (filesize % threadCount == 0) ? filesize / threadCount : filesize / threadCount + 1;
        try {
            file = new File(filename);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!DBManager.getInstance(context).isHasInfos(downloadurl)) {
                Log.d(TAG, "run download info");
                for (int i = 0; i < threadCount; i++) {
                    DownloadInfo info = new DownloadInfo(i, i * block, (i + 1) * block, 0, downloadurl);
                    DBManager.getInstance(context).saveInfo(info);
                }
            }
            accessFile = new RandomAccessFile(file, "rw");
            if (accessFile.length() == filesize) {
                return;
            }
            accessFile.setLength(filesize);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (accessFile != null) {
                    accessFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void pauseDownload(String downloadurl) {
        downloadStatemap.put(downloadurl, Constant.DOWNLOAD_STATE_PAUSE);
    }

    public void pauseAll() {
        if (downloadStatemap == null) {
            return;
        }
        for (String key:downloadStatemap.keySet()){
            downloadStatemap.put(key,Constant.DOWNLOAD_STATE_PAUSE);
        }

    }

    public int getDownloadState(String downloadurl) {
        int downloadstate = -1;
        if (downloadStatemap != null) {
            downloadstate = downloadStatemap.get(downloadurl);
        }
        return downloadstate;
    }

    public void putDownloadState(String downloadurl, int state) {
        if (downloadStatemap != null) {
            downloadStatemap.put(downloadurl, state);
        }
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
        message.setTarget(handler);
        message.sendToTarget();
    }


    public synchronized void startDownload() {
        if (downloadStatemap.get(downloadurl) != null && downloadStatemap.get(downloadurl) == Constant.DOWNLOAD_STATE_START) {
            Log.d(TAG, "download return");
            return;
        }
        sendMessage(Constant.DOWNLOAD_START, filesize, -1, null);
        for (int i = 0; i < threadCount; i++) {
            ThreadPoolsUtil.getInstance().getCachedThreadPool().execute(new DownloadTask(context, handler, downloadurl, filesize, filename, i));
        }
    }



}



/*public class DownloadTask implements Runnable {

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
        List<DownloadInfo> infos = DBManager.getInstance(context).getInfos(downloadurl);
        if (infos == null) {
            return 0;
        }
        for (DownloadInfo info : infos) {
            compeltesize += info.getCompeleteSize();
        }
        return compeltesize;
    }

    @Override
    public void run() {
        downloadStatemap.put(mDownloadurl, DOWNLOAD_STATE_START);
        HttpURLConnection connection = null;
        BufferedInputStream inputStream = null;
        DownloadInfo info = new DownloadInfo();
        Log.d(TAG, "is has info: " + DBManager.getInstance(context).isHasInfos(downloadurl));
        if (DBManager.getInstance(context).isHasInfos(downloadurl)) {
            info = DBManager.getInstance(context).getInfo(downloadurl, threadId);
        }
        try {
            URL url = new URL(mDownloadurl);
            int compeltesize = info.getCompeleteSize();
            int startPos = info.getStartPos();
            int endPos = info.getEndPos();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);
            inputStream = new BufferedInputStream(connection.getInputStream());
            mRandomAccessFile = new RandomAccessFile(mFilename, "rw");
            mRandomAccessFile.seek(startPos);
            Log.d(TAG, "seek position: " + startPos + "  thread id: " + threadId);
            byte[] buffer = new byte[8 * 1024];
            int length = 0;
            while ((length = inputStream.read(buffer)) > 0) {
                if (downloadStatemap.get(mDownloadurl) == DOWNLOAD_STATE_PAUSE) {
                    return;
                }
                Log.d(TAG, "write file length: " + length);
                mRandomAccessFile.write(buffer, 0, length);
                compeltesize += length;
                DBManager.getInstance(context).updataInfos(threadId, compeltesize, downloadurl);
                sendMessage(Constant.DOWNLOAD_KEEP, calculateCompeltesize(), -1, null);
            }
            Log.d(TAG, "calculateCompeltesize: " + calculateCompeltesize() + " filesize: " + filesize + "threadid: " + threadId);
            if (calculateCompeltesize() >= filesize) {
                sendMessage(Constant.DOWNLOAD_COMPLETE, -1, -1, downloadurl);
            }
        } catch (Exception e) {
            sendMessage(Constant.DOWNLOAD_FAIL, -1, -1, downloadurl);
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
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}*/
