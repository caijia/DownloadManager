package com.caijia.download;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by cai.jia on 2018/7/4.
 */
public class FileBreakPointManager implements BreakPointManager {

    private static final int OFFSET = 40;
    private File saveBreakPointFile;
    private RandomAccessFile writeFile;
    private ReentrantLock writeLock = new ReentrantLock();
    private ReentrantLock readLock = new ReentrantLock();

    @Override
    public void saveBreakPoint(int threadIndex, long downloadSize, String saveFilePath,
                               long currentPosition, long startPosition, long endPosition,
                               FileRequest fileRequest, int threadCount) {
        writeLock.lock();
        try {
            if (writeFile == null) {
                writeFile = new RandomAccessFile(saveBreakPointFile, "rw");
            }
            writeFile.seek(threadIndex * OFFSET);
            writeFile.write((currentPosition + downloadSize + "\r\n").getBytes());

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public long getBreakPoint(long startPosition, long endPosition, String saveFilePath,
                              int threadIndex, String fileName, long fileSize,
                              FileRequest fileRequest, int threadCount) {
        readLock.lock();
        RandomAccessFile accessFile = null;
        try {
            File parentFile = new File(saveFilePath).getParentFile();
            String md5String = Utils.fileRequestToMd5String(fileRequest, threadCount);
            saveBreakPointFile = new File(parentFile, md5String + ".txt");
            accessFile = new RandomAccessFile(saveBreakPointFile, "rw");
            accessFile.seek(threadIndex * OFFSET);
            String length = accessFile.readLine();
            if (!Utils.isEmpty(length) && length.matches("\\d+")) {
                return Long.parseLong(length);
            }
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (accessFile != null) {
                try {
                    accessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            readLock.unlock();
        }
        return startPosition;
    }

    @Override
    public void release() {
        try {
            if (writeFile != null) {
                writeFile.close();
                writeFile = null;
            }
            saveBreakPointFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
