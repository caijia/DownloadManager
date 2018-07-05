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

    private Connection connection;
    private int threadCount = 3;
    private ExecutorService executorService;
    private FileRequest fileRequest;
    private String saveFileDirPath;
    private Schedule schedule;
    private List<DownloadCallable> downloadTasks;
    private BreakPointManager breakPointManager;
    private int pauseCount;
    private int completeCount;
    private long totalDownloadLength;
    private CallbackInfo callbackInfo;
    private DownloadListener downloadListener;
    private boolean isPause;
    private long preComputeSpeedTime;
    private long preComputeSpeedLength;
    private long preDownloadCallbackLength;

    private FileDownloader() {

    }

    private FileDownloader(Builder builder) {
        callbackInfo = new CallbackInfo();
        this.connection = builder.connection;
        this.threadCount = builder.threadCount;
        this.saveFileDirPath = builder.saveFileDirPath;
        this.breakPointManager = builder.breakPointManager;
        this.schedule = builder.schedule;
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
            throw new RuntimeException("has not saveFileDirPath");
        }

        downloadTasks = new ArrayList<>();
        executorService = Executors.newCachedThreadPool();
    }

    public void download(FileRequest fileRequest, final DownloadListener downloadListener) {
        if (!canDownload()) {
            return;
        }
        this.downloadListener = downloadListener;
        reset();
        callbackInfo.setState(DownloadState.START);
        callbackInfo.setStartTime(System.currentTimeMillis());
        Runnable r = new Runnable() {
            @Override
            public void run() {
                downloadListener.onStart(callbackInfo);
            }
        };
        schedule(r);
        this.fileRequest = fileRequest;
        requestDownFileInfo();
    }

    private void reset() {
        isPause = false;
        pauseCount = 0;
        completeCount = 0;
        preComputeSpeedTime = 0;
        preComputeSpeedLength = 0;
    }

    private boolean canDownload() {
        return callbackInfo.getState() != DownloadState.DOWNLOADING
                && callbackInfo.getState() != DownloadState.PAUSING;
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
                        e.printStackTrace();
                        pauseCallback();

                    }
                }
            }
        };
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
            downloadTasks.add(callable);
            DownloadFutureTask task = new DownloadFutureTask(callable);
            executorService.submit(task);
        }

        callbackInfo.setState(DownloadState.DOWNLOADING);
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
        downloadingCallback(totalDownloadLength, totalSize);
        long currTime = System.currentTimeMillis();
        if (currTime - preComputeSpeedTime > 1000) {
            if (preComputeSpeedTime != 0) {
                long deltaTime = currTime - preComputeSpeedTime;
                long deltaLength = totalDownloadLength - preComputeSpeedLength;
                int speed = (int) (deltaLength / (deltaTime / 1000f)); //(b/s)
                callbackInfo.setSpeed(speed);
            }
            preComputeSpeedTime = currTime;
            preComputeSpeedLength = totalDownloadLength;
        }
    }

    private void downloadingCallback(long totalDownloadLength, long totalSize) {
        long deltaLength = totalDownloadLength - preDownloadCallbackLength;
        float progress = deltaLength * 1f / totalSize;
        if (progress >= 0.01f || totalDownloadLength == totalSize) {
            callbackInfo.setDownloadSize(this.totalDownloadLength);
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    downloadListener.onDownloading(callbackInfo);
                }
            };
            schedule(r);
            preDownloadCallbackLength = totalDownloadLength;
        }
    }

    private synchronized void tryPauseCallback() {
        if (++pauseCount + completeCount == threadCount) {
            pauseCallback();
        }
    }

    private void pauseCallback() {
        executorService.shutdown();
        computeTotalTime();
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
        if (++completeCount == threadCount) {
            computeTotalTime();
            callbackInfo.setState(DownloadState.COMPLETE);
            callbackInfo.setSavePath(saveFilePath);
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    downloadListener.onComplete(callbackInfo);
                }
            };
            schedule(r);

            executorService.shutdown();
            if (breakPointManager != null) {
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

    private void computeTotalTime() {
        long totalTime = System.currentTimeMillis() - callbackInfo.getStartTime();
        callbackInfo.setTotalTime(totalTime);
    }

    public void pause() {
        this.isPause = true;
        callbackInfo.setState(DownloadState.PAUSING);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                downloadListener.onPausing(callbackInfo);
            }
        };
        schedule(r);
        for (DownloadCallable callable : downloadTasks) {
            callable.exit(true);
        }
    }

    public static class Builder {

        private Connection connection;

        private int threadCount;

        private String saveFileDirPath;

        private BreakPointManager breakPointManager;

        private Schedule schedule;

        public Builder connection(Connection connection) {
            this.connection = connection;
            return this;
        }

        public Builder threadCount(int threadCount) {
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

        public Builder schedule(Schedule schedule) {
            this.schedule = schedule;
            return this;
        }

        public FileDownloader build() {
            return new FileDownloader(this);
        }
    }

    private class DownloadFutureTask extends FutureTask<CallableResult> {

        public DownloadFutureTask(DownloadCallable callable) {
            super(callable);
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
                    switch (state) {
                        case DownloadCallable.COMPLETE:
                            tryCompleteCallback(result.getSaveFilePath());
                            break;

                        case DownloadCallable.ERROR:
                            if (isPause) {
                                tryPauseCallback();
                            }
                            break;

                        case DownloadCallable.PAUSE:
                            tryPauseCallback();
                            break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
