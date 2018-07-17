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
    public void saveDownloadLength(int threadIndex, int threadCount, long downloadSize,
                                   String saveFileDir, FileRequest fileRequest) {
        writeLock.lock();
        try {
            if (writeFile == null) {
                writeFile = new RandomAccessFile(saveBreakPointFile, "rw");
            }
            writeFile.seek(threadIndex * OFFSET);
            writeFile.write((downloadSize + "\r\n").getBytes());

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public long getDownloadLength(int threadIndex, int threadCount, String saveFileDir,
                                  FileRequest fileRequest) {
        readLock.lock();
        RandomAccessFile accessFile = null;
        try {
            String md5String = Utils.fileRequestToMd5String(fileRequest, threadCount);
            saveBreakPointFile = new File(saveFileDir, md5String + ".txt");
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
        return 0;
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
