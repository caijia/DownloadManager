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

    public DownloadCallable(File saveFile, Connection connection, FileRequest fileRequest,
                            long startPosition, long endPosition, int threadIndex) {
        this.fileRequest = fileRequest;
        this.threadIndex = threadIndex;
        this.connection = connection;
        this.saveFile = saveFile;
        this.startPosition = startPosition;

        this.fileRequest = fileRequest.newBuilder()
                .header("Range", "bytes=" + startPosition + "-" + endPosition)
                .build();
        Utils.log("startPosition = " + startPosition
                + "--endPosition=" + endPosition + "threadIndex = " + threadIndex);
    }

    @Override
    public Boolean call() throws Exception {
        FileResponse fileResponse = connection.connect(fileRequest);
        if (fileResponse != null) {
            InputStream inStream = fileResponse.getByteStream();
            int errorCode = writeData(inStream);
        }
        return null;
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
        try {
            partFile = new RandomAccessFile(saveFile, "rws");
            partFile.seek(startPosition);
            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            int totalLength = 0;
            while ((len = inStream.read(buffer)) != -1) {
                partFile.write(buffer, 0, len);
                totalLength += len;
            }
            Utils.log("threadIndex=" + threadIndex + "---complete--" + totalLength);

        } catch (Exception e) {
            e.printStackTrace();
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
                e.printStackTrace();
                code = -1;
            }
        }
        return code;
    }
}
