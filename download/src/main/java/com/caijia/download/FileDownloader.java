package com.caijia.download;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Created by cai.jia on 2018/6/26.
 */
public class FileDownloader {

    private static final float INTERVAL_DOWNLOAD = 1000f;
    private static final int MAX_THREAD_COUNT = 6;
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
    private boolean debug;

    private FileDownloader() {

    }

    private FileDownloader(Builder builder) {
        callbackInfo = new CallbackInfo();
        this.connection = builder.connection;
        this.debug = builder.debug;
        int maxThreadCount = builder.maxThreadCount;
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

        if (maxThreadCount == 0 || maxThreadCount < 1) {
            maxThreadCount = MAX_THREAD_COUNT;
        }

        if (this.threadCount < 1) {
            this.threadCount = 1;
        }

        if (this.threadCount > maxThreadCount) {
            this.threadCount = maxThreadCount;
        }

        if (this.intervalDownload <= 0) {
            intervalDownload = INTERVAL_DOWNLOAD;
        }

        if (Utils.isAndroidPlatform()) {
            Utils.log(debug, "android platform");
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
        totalDownloadLength = computePreviousLength();
        downloadCallableList.clear();
        downloadTasks.clear();
        executorService = Executors.newCachedThreadPool();
    }

    private long computePreviousLength() {
        long total = 0;
        for (int i = 0; i < threadCount; i++) {
            long length = breakPointManager.getDownloadLength(i, threadCount, saveFileDirPath,
                    fileRequest);
            total += length;
        }
        return total;
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
                        if (response.isSuccessful()) {
                            realDownload(response);

                        } else {
                            Utils.log(debug, "retry http code =" + response.getHttpCode());
                            retryRequestDownFileInfo(this);
                        }

                    } catch (Exception e) {
                        Utils.log(debug, "retry error=" + e.getMessage());
                        retryRequestDownFileInfo(this);
                    }

                } else {
                    Utils.log(debug, "cancelled");
                    pauseCallback();
                }
            }
        };
        downloadTasks.add(task);
        executorService.submit(task);
    }

    private void retryRequestDownFileInfo(FutureTask<FileResponse> task) {
        if (isPause) {
            pauseCallback();
            return;
        }
        Utils.log(debug, "retry");
        downloadTasks.remove(task);
        requestDownFileInfo();
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
                    threadCount, breakPointManager, debug);
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
                int speed = (int) (deltaLength / (deltaTime / 1000f)); //(b/s)
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

    private synchronized void tryCompleteCallback(final String saveFilePath) {
        if (++completeCount + pauseCount == threadCount) {
            executorService.shutdown();
            final boolean hasPause = pauseCount > 0;
            callbackInfo.setState(hasPause ? DownloadState.PAUSE : DownloadState.COMPLETE);
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    if (hasPause) {
                        downloadListener.onPause(callbackInfo);

                    } else {
                        callbackInfo.setSavePath(saveFilePath);
                        downloadListener.onComplete(callbackInfo);
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

        private boolean debug;

        private int maxThreadCount;

        public Builder connection(Connection connection) {
            this.connection = connection;
            return this;
        }

        public Builder maxThreadCount(int maxThreadCount) {
            this.maxThreadCount = maxThreadCount;
            return this;
        }

        public Builder threadCount(int threadCount) {
            this.threadCount = threadCount;
            return this;
        }

        public Builder debug(boolean debug) {
            this.debug = debug;
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

        private DownloadCallable callable;

        public DownloadFutureTask(DownloadCallable callable) {
            super(callable);
            this.callable = callable;
            callable.setDownloadProgressListener(new DownloadCallable.DownloadProgressListener() {
                @Override
                public void downloadProgress(int threadIndex, long downloadLength, long total) {
                    downloadLength(downloadLength, total);
                }
            });
        }

        @Override
        protected void done() {
            if (!isCancelled()) {
                try {
                    CallableResult result = get();
                    int state = result.getState();
                    Utils.log(debug, callable.getThreadIndex(), DownloadCallable.stateToString(state));
                    switch (state) {
                        case DownloadCallable.COMPLETE:
                            tryCompleteCallback(result.getSaveFilePath());
                            break;

                        case DownloadCallable.ERROR:
                            retry();
                            break;

                        case DownloadCallable.PAUSE:
                            tryPauseCallback();
                            break;
                    }

                } catch (Exception e) {
                    Utils.log(debug, callable.getThreadIndex(), "error");
                    retry();
                }

            } else {
                Utils.log(debug, callable.getThreadIndex(), "cancelled");
                tryPauseCallback();
            }
        }

        private void retry() {
            if (isPause) {
                tryPauseCallback();
                return;
            }

            //remove old
            downloadCallableList.remove(callable);
            downloadTasks.remove(this);

            //add new
            DownloadCallable newCallable = callable.newCallable();
            DownloadFutureTask task = new DownloadFutureTask(newCallable);
            downloadCallableList.add(newCallable);
            downloadTasks.add(task);

            executorService.submit(task);

            Utils.log(debug, callable.getThreadIndex(), "retry");
        }
    }
}
