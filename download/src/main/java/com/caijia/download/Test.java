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
                .url("http://localhost:8080/timg.gif")
//                .url("http://localhost:8080/app-release.apk")
                .url("http://app.mi.com/download/532898")
                .build();
        fileDownloader.download(fileRequest,new DownloadListener() {
            @Override
            public void onStart(CallbackInfo state) {
                System.out.println(state.toString());
            }

            @Override
            public void onDownloading(CallbackInfo state) {
                System.out.println(state.toString());
            }

            @Override
            public void onComplete(CallbackInfo state) {
                System.out.println(state.toString());
            }

            @Override
            public void onPausing(CallbackInfo state) {
                System.out.println(state.toString());
            }

            @Override
            public void onPause(CallbackInfo state) {
                System.out.println(state.toString());
            }
        });
    }
}
