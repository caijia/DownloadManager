package com.caijia.download;

/**
 * Created by cai.jia on 2018/7/5.
 */
public class CallbackInfo {

    private String downloadPath;

    private int state;

    private String savePath;

    private long fileSize;

    private long downloadSize;

    private long speed;

    private long startTime;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getDownloadSize() {
        return downloadSize;
    }

    public void setDownloadSize(long downloadSize) {
        this.downloadSize = downloadSize;
    }

    public long getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    @Override
    public String toString() {
        return "CallbackInfo{" +
                "downloadPath='" + downloadPath + '\'' +
                ", state=" + DownloadState.toName(state) +
                ", savePath='" + savePath + '\'' +
                ", fileSize=" + fileSize +
                ", downloadSize=" + downloadSize +
                ", speed=" + formatSpeed(speed) +
                '}';
    }

    public String formatSpeed(long speed) {
        if (speed < 1024) {
            return speed + "B/s";

        } else if (speed < 1024 * 1024) {
            return speed / 1024 + "KB/s";

        } else if (speed < 1024 * 1024 * 1024) {
            return speed / (1024 * 1024) + "M/s";
        }
        return "0B/s";
    }
}
