package com.caijia.download;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.Callable;

/**
 * Created by cai.jia on 2018/6/26.
 */
public class DownloadCallable implements Callable<Boolean> {

    private static final int BUFFER_SIZE = 1024 * 8;

    private FileRequest fileRequest;
    private int threadIndex;
    private File saveFile;
    private Connection connection;
    private long startPosition;
    private long endPosition;
    private int threadCount;
    private BreakPointManager breakPointManager;

    public DownloadCallable(File saveFileDir, String fileName, long fileSize, Connection connection,
                            FileRequest fileRequest, int threadIndex, int threadCount,
                            BreakPointManager breakPointManager) {

        this.fileRequest = fileRequest;
        this.threadIndex = threadIndex;
        this.connection = connection;
        this.threadCount = threadCount;
        this.breakPointManager = breakPointManager;

        long avgLength = fileSize / threadCount;
        startPosition = threadIndex + threadIndex * avgLength;
        endPosition = threadIndex == threadCount - 1 ? fileSize - 1 : startPosition + avgLength;

        saveFile = new File(saveFileDir, fileName);
        startPosition = breakPointManager.getBreakPoint(startPosition,
                endPosition, saveFile.getAbsolutePath(), threadIndex, fileName,
                fileSize, fileRequest,threadCount);

        this.fileRequest = fileRequest.newBuilder()
                .header("Range", "bytes=" + startPosition + "-" + endPosition)
                .build();

        Utils.log("startPosition = " + startPosition
                + "--endPosition=" + endPosition + "threadIndex = " + threadIndex);
    }

    @Override
    public Boolean call() throws Exception {
        if (startPosition >= endPosition) {
            return true;
        }
        FileResponse fileResponse = connection.connect(fileRequest);
        InputStream inStream = fileResponse.getByteStream();
        int errorCode = writeData(inStream);
        if (errorCode == 0) {
            return true;
        }
        return false;
    }

    public int getThreadIndex() {
        return threadIndex;
    }

    /**
     * write data to file
     *
     * @param inStream
     * @return code  error = -1, right = 0
     */
    private int writeData(InputStream inStream) {
        int code = 0;
        RandomAccessFile partFile = null;
        long start = System.currentTimeMillis();
        try {
            partFile = new RandomAccessFile(saveFile, "rw");
            partFile.seek(startPosition);
            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            int downloadSize = 0;
            while ((len = inStream.read(buffer)) != -1) {
                partFile.write(buffer, 0, len);
                downloadSize += len;
                breakPointManager.saveBreakPoint(threadIndex, downloadSize,
                        saveFile.getAbsolutePath(), startPosition, endPosition,
                        fileRequest,threadCount);
            }
            Utils.log("threadIndex=" + threadIndex + "---complete--" + downloadSize
                    + "--time = " + (System.currentTimeMillis() - start));

        } catch (Exception e) {
            e.printStackTrace();
            code = -1;
            Utils.log("Exception");

        } finally {
            try {
                if (partFile != null) {
                    partFile.close();
                }

                if (inStream != null) {
                    inStream.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
                code = -1;
            }
        }
        return code;
    }
}
