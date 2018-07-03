package com.caijia.download;

/**
 * Created by cai.jia on 2018/6/29.
 */
public class Test {

    public static void main(String[] args) {
        FileDownloader fileDownloader = new FileDownloader.Builder()
                .threadCount(3)
                .saveFileDirPath("E://")
                .build();
        FileRequest fileRequest = new FileRequest.Builder()
//                .url("http://localhost:8080/test.zip")
                .url("http://localhost:8080/dd.gif")
                .build();
        fileDownloader.download(fileRequest);
    }
}
