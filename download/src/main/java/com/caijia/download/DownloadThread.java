package com.caijia.download;

import java.util.concurrent.Callable;

/**
 * Created by cai.jia on 2018/6/26.
 */
public class DownloadThread implements Callable<Boolean> {

    private FileRequest fileRequest;
    private long startPosition;
    private long endPosition;

    public DownloadThread(FileRequest fileRequest, long startPosition, long endPosition) {
        this.fileRequest = fileRequest;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    @Override
    public Boolean call() throws Exception {


        return null;
    }
}
