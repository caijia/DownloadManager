package com.caijia.download;

/**
 * Created by cai.jia on 2018/7/4.
 */
public interface BreakPointManager {

    void saveBreakPoint(int threadIndex, long downloadSize, String saveFilePath,
                        long startPosition, long endPosition, FileRequest fileRequest,
                        int threadCount);

    long getBreakPoint(long startPosition, long endPosition, String saveFilePath,
                       int threadIndex, String fileName, long fileSize, FileRequest fileRequest,
                       int threadCount);

    void release();
}
