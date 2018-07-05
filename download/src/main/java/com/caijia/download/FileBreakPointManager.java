package com.caijia.download;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by cai.jia on 2018/7/4.
 */
public class FileBreakPointManager implements BreakPointManager {

    private RandomAccessFile accessFile;
    private ReentrantLock reentrantLock = new ReentrantLock();
    private File saveBreakPointFile;

    @Override
    public void saveBreakPoint(int threadIndex, long downloadSize, String saveFilePath,
                               long startPosition, long endPosition, FileRequest fileRequest,
                               int threadCount) {
        reentrantLock.lock();
        try {
            accessFile.seek(threadIndex * 40);
            accessFile.write((startPosition + downloadSize + "\r\n").getBytes());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            reentrantLock.unlock();
        }
    }

    @Override
    public long getBreakPoint(long startPosition, long endPosition, String saveFilePath,
                              int threadIndex, String fileName, long fileSize,
                              FileRequest fileRequest, int threadCount) {
        File parentFile = new File(saveFilePath).getParentFile();
        String md5String = Utils.fileRequestToMd5String(fileRequest, threadCount);
        saveBreakPointFile = new File(parentFile, md5String + ".txt");
        try {
            if (accessFile == null) {
                accessFile = new RandomAccessFile(saveBreakPointFile, "rw");
            }
            accessFile.seek(threadIndex * 40);
            String length = accessFile.readLine();
            if (!Utils.isEmpty(length)) {
                return Long.parseLong(length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return startPosition;
    }

    @Override
    public void release() {
        try {
            accessFile.close();
            saveBreakPointFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
