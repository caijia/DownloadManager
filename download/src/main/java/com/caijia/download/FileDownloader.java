package com.caijia.download;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntRange;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by cai.jia on 2018/6/26.
 */
public class FileDownloader {

    private static final int MSG_DOWNLOAD_FILE_INFO = 1;
    private Connection connection;
    private int threadCount = 3;
    private ExecutorService executorService;
    //    private InternalHandler handler;
    private FileRequest fileRequest;
    private String saveFileDirPath;
    private List<DownloadCallable> downloadCallables = new ArrayList<>();
    private BreakPointManager breakPointManager;

    private FileDownloader() {

    }

    private FileDownloader(Builder builder) {
        this.connection = builder.connection;
        this.threadCount = builder.threadCount;
        this.saveFileDirPath = builder.saveFileDirPath;
        this.breakPointManager = builder.breakPointManager;
        if (this.connection == null) {
            this.connection = new OkHttpConnection();
        }

        if (this.breakPointManager == null) {
            this.breakPointManager = new FileBreakPointManager();
        }

        if (this.threadCount < 1 || this.threadCount > 5) {
            this.threadCount = 3;
        }

        if (Utils.isEmpty(saveFileDirPath)) {
//            this.saveFileDirPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        }

        executorService = Executors.newCachedThreadPool();
//        handler = new InternalHandler(this);
    }

    public void download(FileRequest fileRequest) {
        this.fileRequest = fileRequest;
        requestDownFileInfo();
    }

    private void requestDownFileInfo() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    FileRequest headRequest = fileRequest.newBuilder()
                            .method(HttpMethod.GET)
                            .build();
                    FileResponse response = connection.connect(headRequest);
                    long fileSize = response.getContentLength();
                    String fileName = response.getFileName();
                    DownloadFileInfo downloadFile = new DownloadFileInfo(fileName, fileSize);
//                        Message message = handler.obtainMessage(MSG_DOWNLOAD_FILE_INFO, downloadFile);
//                        handler.sendMessage(message);
                    realDownload(downloadFile);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        executorService.submit(runnable);
    }

    /**
     * length = 10 , threadCount = 3; Range:bytes=0-9; avg = 10 / 3 = 3 ;
     * thread[0] = 0-3 , thread[1] = 4-7,thread[2] = 8-(length - 1)
     *
     * @param downloadFile
     */
    private void realDownload(DownloadFileInfo downloadFile) {
        if (downloadFile == null) {
            return;
        }

        long fileSize = downloadFile.fileSize;
        String fileName = downloadFile.fileName;

        if (fileSize < 0) {
            Utils.log("download file size < 0");
            return;
        }

        if (Utils.isEmpty(fileName)) {
            Utils.log("download file name is null");
            return;
        }

        try {
            File saveFileDir = new File(saveFileDirPath);
            saveFileDir.mkdirs();

            if (fileSize < threadCount) {
                threadCount = (int) fileSize;
            }

            for (int i = 0; i < threadCount; i++) {
                DownloadCallable downloadCallable = new DownloadCallable(
                        saveFileDir, downloadFile.fileName,downloadFile.fileSize,
                        connection, fileRequest, i,threadCount, breakPointManager);
                downloadCallables.add(downloadCallable);
            }

            executorService.invokeAll(downloadCallables);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class DownloadFileInfo implements Parcelable {
        private String fileName;
        private long fileSize;

        public DownloadFileInfo(String fileName, long fileSize) {
            this.fileName = fileName;
            this.fileSize = fileSize;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.fileName);
            dest.writeLong(this.fileSize);
        }

        protected DownloadFileInfo(Parcel in) {
            this.fileName = in.readString();
            this.fileSize = in.readLong();
        }

        public static final Parcelable.Creator<DownloadFileInfo> CREATOR = new Parcelable.Creator<DownloadFileInfo>() {
            @Override
            public DownloadFileInfo createFromParcel(Parcel source) {
                return new DownloadFileInfo(source);
            }

            @Override
            public DownloadFileInfo[] newArray(int size) {
                return new DownloadFileInfo[size];
            }
        };
    }

    private static class InternalHandler extends Handler {

        private FileDownloader fileDownloader;

        InternalHandler(FileDownloader fileDownloader) {
            super(Looper.getMainLooper());
            this.fileDownloader = fileDownloader;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DOWNLOAD_FILE_INFO: {
                    DownloadFileInfo downloadFile = (DownloadFileInfo) msg.obj;
                    fileDownloader.realDownload(downloadFile);
                    break;
                }
            }
        }
    }

    public static class Builder {

        private Connection connection;

        private int threadCount;

        private String saveFileDirPath;

        private BreakPointManager breakPointManager;

        public Builder connection(Connection connection) {
            this.connection = connection;
            return this;
        }

        public Builder threadCount(@IntRange(from = 1, to = 5) int threadCount) {
            this.threadCount = threadCount;
            return this;
        }

        public Builder saveFileDirPath(String saveFileDirPath) {
            this.saveFileDirPath = saveFileDirPath;
            return this;
        }

        public Builder breakPointManager(BreakPointManager breakPointManager) {
            this.breakPointManager = breakPointManager;
            return this;
        }

        public FileDownloader build() {
            return new FileDownloader(this);
        }
    }
}
