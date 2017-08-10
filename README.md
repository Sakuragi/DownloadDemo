# DownloadDemo
效果图：

![image](http://a3.qpic.cn/psb?/V1486Uno1i1m7T/6blZEm4A.PvNa4qCaKD1mBq0CmmDDFEZj54DYWUsYHM!/b/dD0BAAAAAAAA&bo=QAE6AkABOgICgqY!&rf=viewer_4)

## 断点续传原理
断点续传是指当下载中断后，再次下载时可以从上次的下载进度继续下载。由此我们可以分析得出实现这个功能，我们需要实时保存下载进度，这样在下次继续下载的时候再把下载进度读取出来，继续下载。我们主要需要解决俩个问题：**一、从上次的位置继续下载。二、从上次写入的文件继续写入。** 通过HTTP的GET请求中的setRequestProperty()方法可以告诉服务器，数据从哪里开始到哪里结束，以及RandomAccessFile的seek方法来来对文件的指定位置经行写入。

点击事件触发下载任务，controller.startDownload(MainActivity.this,mHandler,Constant.downloadUrl,filename,1)，
分别传入Context，handler，下载链接，文件名，线程数量：
```
 btn_pauseDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetWorkUtil.isNetworkAvailable(MainActivity.this)){
                    Toast.makeText(MainActivity.this,"请检查网络",Toast.LENGTH_SHORT).show();
                    return;
                }
                controller.setDownloadState(pausestate);
                controller.stopDownload(MainActivity.this,mHandler,Constant.downloadUrl,filename,1);
            }
        });
        btn_startDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetWorkUtil.isNetworkAvailable(MainActivity.this)){
                    Toast.makeText(MainActivity.this,"请检查网络",Toast.LENGTH_SHORT).show();
                    return;
                }
                controller.setDownloadState(startstate);
                controller.startDownload(MainActivity.this,mHandler,Constant.downloadUrl,filename,1);
            }
        });
```

