package com.caijia.download;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by cai.jia on 2018/7/4.
 */
public class FileBreakPointManager implements BreakPointManager {

    private RandomAccessFile accessFile;
    private ReentrantLock reentrantLock = new ReentrantLock();

    @Override
    public void saveBreakPoint(int threadIndex, long downloadSize, String saveFilePath,
                               long startPosition, long endPosition, FileRequest fileRequest) {
        reentrantLock.lock();
        try {
            accessFile.seek(threadIndex * 40);
            accessFile.write((startPosition + downloadSize + "\n").getBytes());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            reentrantLock.unlock();
        }
    }

    @Override
    public long getBreakPoint(long startPosition, long endPosition, String saveFilePath,
                              int threadIndex, String fileName, long fileSize,
                              FileRequest fileRequest) {
        File file = new File("E://temp.text");
        try {
            accessFile = new RandomAccessFile(file, "rws");
            accessFile.seek(threadIndex * 40);
            String length = accessFile.readLine();
            return Long.parseLong(length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return startPosition;
    }
}
