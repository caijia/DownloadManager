package com.caijia.download;

public interface DownloadState {

    int START = 0;

    int DOWNLOADING = 1;

    int PAUSING = 2;

    int PAUSE = 3;

    int COMPLETE = 4;

}
