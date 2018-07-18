package com.caijia.download;

public class DownloadState {

    public static final int IDLE = 0;

    public static final int START = 1;

    public static final int PREPARED = 2;

    public static final int DOWNLOADING = 3;

    public static final int PAUSING = 4;

    public static final int PAUSE = 5;

    public static final int COMPLETE = 6;

    public static String toName(int state) {
        String name = "";
        switch (state) {
            case IDLE:
                name = "idle";
                break;

            case PREPARED:
                name = "prepared";
                break;

            case DOWNLOADING:
                name = "downloading";
                break;

            case PAUSING:
                name = "pausing";
                break;

            case PAUSE:
                name = "pause";
                break;

            case COMPLETE:
                name = "complete";
                break;
        }
        return name;
    }
}
