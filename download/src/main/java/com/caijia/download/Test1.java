package com.caijia.download;

/**
 * Created by cai.jia on 2018/7/6.
 */
public class Test1 {

    public static void main(String[] args) {
        FileRequest fileRequest = new FileRequest.Builder()
                .url("http://app.mi.com/download/532898")
                .build();

        FileDownloader fileDownloader = new FileDownloader.Builder()
                .threadCount(3)
                .saveFileDirPath("E://")
                .fileRequest(fileRequest)
                .build();

        fileDownloader.download(new DownloadListener() {
            @Override
            public void onStart(CallbackInfo state) {

            }

            @Override
            public void onDownloading(CallbackInfo state) {

            }

            @Override
            public void onComplete(CallbackInfo state) {

            }

            @Override
            public void onPausing(CallbackInfo state) {

            }

            @Override
            public void onPause(CallbackInfo state) {

            }
        });
    }
}
