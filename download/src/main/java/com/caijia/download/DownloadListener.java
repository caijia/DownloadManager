package com.caijia.download;

/**
 * Created by cai.jia on 2018/7/5.
 */
public interface DownloadListener {

    void onStart(CallbackInfo state);

    void onDownloading(CallbackInfo state);

    void onComplete(CallbackInfo state);

    void onPausing(CallbackInfo state);

    void onPause(CallbackInfo state);

}
