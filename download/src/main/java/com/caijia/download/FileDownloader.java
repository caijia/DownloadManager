package com.caijia.download;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IntRange;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by cai.jia on 2018/6/26.
 */
public class FileDownloader {

    private static final int MSG_GET_LENGTH = 1;
    private Connection connection;
    private @IntRange(from = 1, to = 5)
    int threadCount = 3;
    private ExecutorService executorService;
    private InternalHandler handler;

    private FileDownloader() {

    }

    private FileDownloader(Builder builder) {
        this.connection = builder.connection;
        this.threadCount = builder.threadCount;
        executorService = Executors.newFixedThreadPool(threadCount);
        handler = new InternalHandler(this);
    }

    public void download(FileRequest fileRequest) {
        requestContentLength(fileRequest);
    }

    private void requestContentLength(final FileRequest fileRequest) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    FileRequest headRequest = fileRequest.newBuilder().method("HEAD").build();
                    FileResponse response = connection.connect(headRequest);
                    if (response != null) {
                        Map<String, List<String>> headers = response.getHeaders();
                        long contentLength = HttpUtils.contentLength(headers);
                        handler.sendMessage(handler.obtainMessage(MSG_GET_LENGTH, contentLength));
                    }

                } catch (Exception e) {
//                    executorService.submit(this);
                }
            }
        };
        executorService.submit(runnable);
    }

    private void realDownload(long contentLength) {

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
                case MSG_GET_LENGTH: {
                    long contentLength = (long) msg.obj;
                    fileDownloader.realDownload(contentLength);
                    break;
                }
            }
        }
    }

    public static class Builder {

        private Connection connection;

        private @IntRange(from = 1, to = 5)
        int threadCount;

        public Builder connection(Connection connection) {
            this.connection = connection;
            return this;
        }

        public Builder threadCount(@IntRange(from = 1, to = 5) int threadCount) {
            this.threadCount = threadCount;
            return this;
        }

        public FileDownloader build() {
            return new FileDownloader(this);
        }
    }
}
