package com.caijia.download;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.MessageFormat;
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

    public DownloadCallable(File saveFile,Connection connection,FileRequest fileRequest,
                            long startPosition, long endPosition, int threadIndex) {
        this.fileRequest = fileRequest;
        this.threadIndex = threadIndex;
        this.connection = connection;
        this.saveFile = saveFile;

        this.fileRequest = fileRequest.newBuilder()
                .header("Range", convertRangeHeader(startPosition,endPosition))
                .build();
    }

    private String convertRangeHeader(long startPosition, long endPosition) {
        return MessageFormat.format("bytes={0}-{1}", startPosition, endPosition);
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
     * @param inStream
     * @return code  error = -1, right = 0
     */
    private int writeData(InputStream inStream) {
        RandomAccessFile outStream = null;
        int code = 0;
        try {
             outStream = new RandomAccessFile(saveFile, "rws");
            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }

        } catch (Exception e) {
            code = -1;

        }finally {

            try {
                if (outStream != null) {
                    outStream.close();
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
}
