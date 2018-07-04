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
    private int writeFinishCount = 0;
    private File saveBreakPointFile;

    @Override
    public void saveBreakPoint(int threadIndex, long downloadSize, String saveFilePath,
                               long startPosition, long endPosition, FileRequest fileRequest,
                               int threadCount) {
        reentrantLock.lock();
        try {
            accessFile.seek(threadIndex * 40);
            accessFile.write((startPosition + downloadSize + "\n").getBytes());
            if (downloadSize == (endPosition - startPosition + 1)) {
                writeFinishCount++;
                if (writeFinishCount == threadCount) {
                    Utils.log("complete");
                    accessFile.close();
                    saveBreakPointFile.deleteOnExit();
                }
            }

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
        String md5String = Utils.fileRequestToMd5String(fileRequest,threadCount);
        saveBreakPointFile = new File(parentFile, md5String + ".temp");
        try {
            accessFile = new RandomAccessFile(saveBreakPointFile, "rw");
            accessFile.seek(threadIndex * 40);
            String length = accessFile.readLine();
            return Long.parseLong(length);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return startPosition;
    }
}
