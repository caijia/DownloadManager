package com.caijia.download;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.MessageFormat;
import java.util.concurrent.Callable;

/**
 * Created by cai.jia on 2018/6/26.
 */
class DownloadCallable implements Callable<CallableResult> {

    public static final int PAUSE = 1;
    public static final int COMPLETE = 0;
    public static final int ERROR = -1;

    private static final int BUFFER_SIZE = 1024 * 8;

    private FileRequest fileRequest;
    private int threadIndex;
    private File saveFile;
    private Connection connection;
    private long currentPosition;
    private long startPosition;
    private long endPosition;
    private int threadCount;
    private BreakPointManager breakPointManager;
    private long fileSize;
    private String fileName;
    private CallableResult callableResult;
    private boolean exit;
    private DownloadProgressListener downloadProgressListener;

    public DownloadCallable(File saveFileDir, String fileName, long fileSize, Connection connection,
                            FileRequest fileRequest, int threadIndex, int threadCount,
                            BreakPointManager breakPointManager) {

        this.fileRequest = fileRequest;
        this.threadIndex = threadIndex;
        this.connection = connection;
        this.threadCount = threadCount;
        this.fileSize = fileSize;
        this.fileName = fileName;
        this.breakPointManager = breakPointManager;
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
        Utils.log(threadIndex, "running");
        currentPosition = breakPointManager.getBreakPoint(startPosition,
                endPosition, saveFile.getAbsolutePath(), threadIndex, fileName,
                fileSize, fileRequest, threadCount);

        long preDownloadLength = currentPosition - startPosition;
        if (downloadProgressListener != null) {
            downloadProgressListener.preDownloadLength(threadIndex, preDownloadLength);
        }
        Utils.log(threadIndex, "previous download length = " + preDownloadLength);

        this.fileRequest = fileRequest.newBuilder()
                .header("Range", "bytes=" + currentPosition + "-" + endPosition)
                .build();

        Utils.log(threadIndex, MessageFormat.format(
                "currentPosition={0}--endPosition={1}",
                currentPosition, endPosition));

        if (currentPosition >= endPosition) {
            callableResult.setState(COMPLETE);
            return callableResult;
        }

        try {
            FileResponse fileResponse = connection.connect(fileRequest);
            InputStream inStream = fileResponse.getByteStream();
            int errorCode = writeData(inStream);
            if (errorCode == 0) {
                callableResult.setState(exit ? PAUSE : COMPLETE);
                return callableResult;
            }
            Utils.log(threadIndex, "http write error");

        } catch (Exception e) {
            Utils.log(threadIndex, "http connect error");
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
        int code = 0;
        RandomAccessFile partFile = null;
        try {
            partFile = new RandomAccessFile(saveFile, "rw");
            partFile.seek(currentPosition);
            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            int downloadSize = 0;
            while (!exit && (len = inStream.read(buffer)) != -1) {
                partFile.write(buffer, 0, len);
                downloadSize += len;
                breakPointManager.saveBreakPoint(threadIndex, downloadSize,
                        saveFile.getAbsolutePath(), currentPosition, startPosition, endPosition,
                        fileRequest, threadCount);

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

                if (inStream != null) {
                    inStream.close();
                }

            } catch (Exception e) {
                code = -1;
            }
        }
        return code;
    }

    public void setDownloadProgressListener(DownloadProgressListener downloadProgressListener) {
        this.downloadProgressListener = downloadProgressListener;
    }

    public interface DownloadProgressListener {

        void downloadProgress(int threadIndex, long downloadLength, long totalLength);

        void preDownloadLength(int threadIndex, long preDownloadLength);
    }
}
