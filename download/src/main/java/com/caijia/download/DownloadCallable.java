package com.caijia.download;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by cai.jia on 2018/6/26.
 */
class DownloadCallable implements Callable<CallableResult> {

    public static final int PAUSE = 1;
    public static final int COMPLETE = 0;
    public static final int ERROR = -1;

    private static final int BUFFER_SIZE = 1024 * 8;

    private File saveFileDir;
    private String fileName;
    private long fileSize;
    private Connection connection;
    private FileRequest fileRequest;
    private int threadIndex;
    private int threadCount;
    private BreakPointManager breakPointManager;
    private boolean debug;
    private File saveFile;
    private long currentPosition;
    private long startPosition;
    private long endPosition;
    private CallableResult callableResult;
    private boolean exit;
    private DownloadProgressListener downloadProgressListener;

    public DownloadCallable(File saveFileDir, String fileName, long fileSize, Connection connection,
                            FileRequest fileRequest, int threadIndex, int threadCount,
                            BreakPointManager breakPointManager, boolean debug) {

        this.saveFileDir = saveFileDir;
        this.fileRequest = fileRequest;
        this.threadIndex = threadIndex;
        this.connection = connection;
        this.threadCount = threadCount;
        this.fileSize = fileSize;
        this.fileName = fileName;
        this.breakPointManager = breakPointManager;
        this.debug = debug;

        long avgLength = fileSize / threadCount;
        startPosition = threadIndex + threadIndex * avgLength;
        endPosition = threadIndex == threadCount - 1 ? fileSize - 1 : startPosition + avgLength;
        saveFile = new File(saveFileDir, fileName);

        callableResult = new CallableResult();
        callableResult.setSaveFilePath(saveFile.getAbsolutePath());
        callableResult.setThreadIndex(threadIndex);
    }

    public static String stateToString(int state) {
        return state == COMPLETE ? "complete" : (state == PAUSE ? "pause" : "error");
    }

    @Override
    public CallableResult call() {
        Utils.log(debug, threadIndex, "running");
        long previousDownloadLength = breakPointManager != null ?
                breakPointManager.getDownloadLength(threadIndex,threadCount,
                        saveFileDir.getAbsolutePath(), fileRequest) : 0;
        Utils.log(debug, threadIndex, "previous download length = " + previousDownloadLength);

        currentPosition = startPosition + previousDownloadLength;
        this.fileRequest = fileRequest.newBuilder()
                .header("Range", "bytes=" + currentPosition + "-" + endPosition)
                .build();

        Utils.log(debug, threadIndex, MessageFormat.format(
                "currentPosition={0}--endPosition={1}",
                currentPosition, endPosition));

        if (currentPosition >= endPosition) {
            callableResult.setState(COMPLETE);
            return callableResult;
        }

        try {
            FileResponse fileResponse = connection.connect(fileRequest);
            Utils.log(debug, threadIndex, "httpCode = " + fileResponse.getHttpCode());
            if (fileResponse.isSuccessful()) {
                InputStream inStream = fileResponse.getByteStream();
                int errorCode = writeData(inStream);
                if (errorCode == 0) {
                    callableResult.setState(exit ? PAUSE : COMPLETE);
                    return callableResult;

                } else {
                    Utils.log(debug, threadIndex, "error write data");
                }
            }

        } catch (Exception e) {
            Utils.log(debug, threadIndex, "error connection");
        }
        callableResult.setState(ERROR);
        return callableResult;
    }

    public int getThreadIndex() {
        return threadIndex;
    }

    public void exit(boolean exit) {
        this.exit = exit;
    }

    /**
     * write data to file
     *
     * @param inStream
     * @return code  error = -1, right = 0
     */
    private int writeData(InputStream inStream) {
        if (exit) {
            return 0;
        }

        if (inStream == null) {
            return -1;
        }

        int code = 0;
        RandomAccessFile partFile = null;
        try {
            partFile = new RandomAccessFile(saveFile, "rw");
            partFile.seek(currentPosition);
            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            long downloadSize = currentPosition - startPosition;
            while (!exit && (len = inStream.read(buffer)) != -1) {
                partFile.write(buffer, 0, len);
                downloadSize += len;
                if (breakPointManager != null) {
                    breakPointManager.saveDownloadLength(threadIndex, threadCount, downloadSize,
                            saveFileDir.getAbsolutePath(), fileRequest);
                }

                if (downloadProgressListener != null) {
                    downloadProgressListener.downloadProgress(threadIndex, len, fileSize);
                }
            }

        } catch (Exception e) {
            code = -1;

        } finally {
            try {
                if (partFile != null) {
                    partFile.close();
                }
                inStream.close();

            } catch (Exception e) {
                code = -1;
            }
        }
        return code;
    }

    public void setDownloadProgressListener(DownloadProgressListener downloadProgressListener) {
        this.downloadProgressListener = downloadProgressListener;
    }

    public DownloadCallable newCallable() {
        Map<String, List<String>> headers = fileRequest.getHeaders();
        if (headers != null) {
            headers.remove("Range");
        }
        return new DownloadCallable(saveFileDir, fileName, fileSize, connection, fileRequest,
                threadIndex, threadCount, breakPointManager, debug);
    }

    public interface DownloadProgressListener {

        void downloadProgress(int threadIndex, long downloadLength, long totalLength);

    }
}
