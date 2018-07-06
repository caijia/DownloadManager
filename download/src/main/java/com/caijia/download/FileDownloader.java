package com.caijia.download;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Created by cai.jia on 2018/6/26.
 */
public class FileDownloader {

    private static final float INTERVAL_DOWNLOAD = 500f;
    private Connection connection;
    private int threadCount = 3;
    private ExecutorService executorService;
    private FileRequest fileRequest;
    private String saveFileDirPath;
    private Schedule schedule;
    private List<DownloadCallable> downloadCallableList;
    private List<FutureTask> downloadTasks;
    private BreakPointManager breakPointManager;
    private int pauseCount;
    private int completeCount;
    private long totalDownloadLength;
    private CallbackInfo callbackInfo;
    private DownloadListener downloadListener;
    private boolean isPause;
    private long preComputeSpeedTime;
    private long preComputeSpeedLength;
    private float intervalDownload = INTERVAL_DOWNLOAD;

    private FileDownloader() {

    }

    private FileDownloader(Builder builder) {
        callbackInfo = new CallbackInfo();
        this.connection = builder.connection;
        this.threadCount = builder.threadCount;
        this.saveFileDirPath = builder.saveFileDirPath;
        this.breakPointManager = builder.breakPointManager;
        this.fileRequest = builder.fileRequest;
        this.schedule = builder.schedule;
        this.intervalDownload = builder.intervalDownload;
        if (this.connection == null) {
            this.connection = new OkHttpConnection();
        }

        if (this.breakPointManager == null) {
            this.breakPointManager = new FileBreakPointManager();
        }

        if (this.threadCount < 1 || this.threadCount > 5) {
            this.threadCount = 3;
        }

        if (this.intervalDownload <= 0) {
            intervalDownload = INTERVAL_DOWNLOAD;
        }

        if (Utils.isAndroidPlatform()) {
            Utils.log("android platform");
            this.schedule = new AndroidSchedule();
        }

        if (Utils.isEmpty(saveFileDirPath)) {
            throw new RuntimeException("has not saveFileDirPath");
        }

        downloadCallableList = new ArrayList<>();
        downloadTasks = new ArrayList<>();
    }

    public void download(DownloadListener downloadListener) {
        download(fileRequest, downloadListener);
    }

    public void download(FileRequest fileRequest, final DownloadListener downloadListener) {
        if (fileRequest == null || Utils.isEmpty(fileRequest.getUrl())) {
            throw new RuntimeException("please set " + FileRequest.class.getCanonicalName());
        }

        if (!canDownload()) {
            return;
        }

        this.downloadListener = downloadListener;
        callbackInfo.setState(DownloadState.DOWNLOADING);
        callbackInfo.setStartTime(System.currentTimeMillis());
        Runnable r = new Runnable() {
            @Override
            public void run() {
                downloadListener.onStart(callbackInfo);
            }
        };
        schedule(r);
        this.fileRequest = fileRequest;

        reset();
        requestDownFileInfo();
    }

    private void reset() {
        isPause = false;
        pauseCount = 0;
        completeCount = 0;
        preComputeSpeedTime = 0;
        preComputeSpeedLength = 0;
        totalDownloadLength = 0;
        downloadCallableList.clear();
        downloadTasks.clear();
        executorService = Executors.newCachedThreadPool();
    }

    public boolean canDownload() {
        int state = callbackInfo.getState();
        return state == DownloadState.COMPLETE ||
                state == DownloadState.PAUSE ||
                state == DownloadState.IDLE;
    }

    private void requestDownFileInfo() {
        Callable<FileResponse> callable = new Callable<FileResponse>() {
            @Override
            public FileResponse call() throws Exception {
                FileRequest headRequest = fileRequest.newBuilder()
                        .method(HttpMethod.GET)
                        .build();
                return connection.connect(headRequest);
            }
        };
        FutureTask<FileResponse> task = new FutureTask<FileResponse>(callable) {
            @Override
            protected void done() {
                if (!isCancelled()) {
                    try {
                        FileResponse response = get();
                        realDownload(response);

                    } catch (Exception e) {
                        Utils.log("thread request length -> error" + e.getMessage());
                        pauseCallback();
                    }

                } else {
                    Utils.log("thread request length -> cancelled");
                    pauseCallback();
                }
            }
        };
        downloadTasks.add(task);
        executorService.submit(task);
    }

    private void realDownload(FileResponse response) {
        if (response == null) {
            return;
        }

        long fileSize = response.getContentLength();
        String fileName = response.getFileName();
        String realDownloadUrl = response.getRealDownloadUrl();

        if (fileSize < 0) {
            pauseCallback();
            return;
        }

        if (Utils.isEmpty(fileName)) {
            pauseCallback();
            return;
        }

        File saveFileDir = new File(saveFileDirPath);
        saveFileDir.mkdirs();

        if (fileSize < threadCount) {
            threadCount = (int) fileSize;
        }

        if (isPause) {
            return;
        }

        for (int i = 0; i < threadCount; i++) {
            DownloadCallable callable = new DownloadCallable(
                    saveFileDir, fileName, fileSize, connection, fileRequest, i,
                    threadCount, breakPointManager);
            downloadCallableList.add(callable);
            DownloadFutureTask task = new DownloadFutureTask(callable);
            downloadTasks.add(task);
            executorService.submit(task);
        }

        callbackInfo.setFileSize(fileSize);
        callbackInfo.setDownloadPath(realDownloadUrl);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                downloadListener.onDownloading(callbackInfo);
            }
        };
        schedule(r);
    }

    private void downloadLength(long downloadLength, long totalSize) {
        totalDownloadLength += downloadLength;
        long currTime = System.currentTimeMillis();
        if (currTime - preComputeSpeedTime > intervalDownload ||
                totalDownloadLength == totalSize) {
            if (preComputeSpeedTime != 0) {
                long deltaTime = currTime - preComputeSpeedTime;
                long deltaLength = totalDownloadLength - preComputeSpeedLength;
                int speed = (int) (deltaLength / (deltaTime / intervalDownload)); //(b/s)
                callbackInfo.setSpeed(speed);
                callbackInfo.setDownloadSize(this.totalDownloadLength);
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        downloadListener.onDownloading(callbackInfo);
                    }
                };
                schedule(r);
            }
            preComputeSpeedTime = currTime;
            preComputeSpeedLength = totalDownloadLength;
        }
    }

    private void appendPreviousDownloadLength(long previousDownloadLen) {
        totalDownloadLength += previousDownloadLen;
    }

    private synchronized void tryPauseCallback() {
        if (++pauseCount + completeCount == threadCount) {
            pauseCallback();
        }
    }

    private void pauseCallback() {
        executorService.shutdown();
        callbackInfo.setState(DownloadState.PAUSE);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                downloadListener.onPause(callbackInfo);
            }
        };
        schedule(r);
    }

    private synchronized void tryCompleteCallback(String saveFilePath) {
        if (++completeCount + pauseCount == threadCount) {
            executorService.shutdown();
            final boolean hasPause = pauseCount > 0;
            callbackInfo.setState(hasPause ? DownloadState.PAUSE : DownloadState.COMPLETE);
            callbackInfo.setSavePath(saveFilePath);
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    if (hasPause) {
                        downloadListener.onComplete(callbackInfo);

                    } else {
                        downloadListener.onPause(callbackInfo);
                    }
                }
            };
            schedule(r);

            if (!hasPause) {
                breakPointManager.release();
            }
        }
    }

    private void schedule(Runnable r) {
        if (schedule != null) {
            schedule.schedule(r);
        } else {
            r.run();
        }
    }

    public void pause() {
        int state = callbackInfo.getState();
        if (state == DownloadState.PAUSE ||
                state == DownloadState.COMPLETE ||
                state == DownloadState.PAUSING) {
            return;
        }

        this.isPause = true;
        callbackInfo.setState(DownloadState.PAUSING);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                downloadListener.onPausing(callbackInfo);
            }
        };
        schedule(r);
        for (DownloadCallable callable : downloadCallableList) {
            callable.exit(true);
        }

        for (FutureTask downloadTask : downloadTasks) {
            downloadTask.cancel(false);
        }
    }

    public static class Builder {

        private Connection connection;

        private int threadCount;

        private String saveFileDirPath;

        private BreakPointManager breakPointManager;

        private Schedule schedule;

        private FileRequest fileRequest;

        private float intervalDownload;

        public Builder connection(Connection connection) {
            this.connection = connection;
            return this;
        }

        public Builder threadCount(int threadCount) {
            this.threadCount = threadCount;
            return this;
        }

        public Builder intervalDownload(float intervalDownload) {
            this.intervalDownload = intervalDownload;
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

        public Builder schedule(Schedule schedule) {
            this.schedule = schedule;
            return this;
        }

        public Builder fileRequest(FileRequest fileRequest) {
            this.fileRequest = fileRequest;
            return this;
        }

        public FileDownloader build() {
            return new FileDownloader(this);
        }
    }

    private class DownloadFutureTask extends FutureTask<CallableResult> {

        private int threadIndex;

        public DownloadFutureTask(DownloadCallable callable) {
            super(callable);
            threadIndex = callable.getThreadIndex();
            callable.setDownloadProgressListener(new DownloadCallable.DownloadProgressListener() {
                @Override
                public void downloadProgress(int threadIndex, long downloadLength, long total) {
                    downloadLength(downloadLength, total);
                }

                @Override
                public void preDownloadLength(int threadIndex, long preDownloadLength) {
                    appendPreviousDownloadLength(preDownloadLength);
                }
            });
        }

        @Override
        protected void done() {
            if (!isCancelled()) {
                try {
                    CallableResult result = get();
                    int state = result.getState();
                    switch (state) {
                        case DownloadCallable.COMPLETE:
                            tryCompleteCallback(result.getSaveFilePath());
                            break;

                        case DownloadCallable.ERROR:
                            tryPauseCallback();
                            break;

                        case DownloadCallable.PAUSE:
                            tryPauseCallback();
                            break;
                    }
                    Utils.log(threadIndex, DownloadCallable.stateToString(state));

                } catch (Exception e) {
                    Utils.log(threadIndex, "error");
                    tryPauseCallback();
                }

            } else {
                Utils.log(threadIndex, "cancelled");
                tryPauseCallback();
            }
        }
    }
}
