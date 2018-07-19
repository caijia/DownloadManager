package com.caijia.download;

/**
 * Created by cai.jia on 2018/7/5.
 */
public class CallbackInfo {

    /**
     * 文件下载路径,跟用户传过来的下载路径可能不一样,也许是重定向后的下载路径
     */
    private String downloadPath;

    /**
     * 下载状态{@link DownloadState}
     */
    private int state;

    /**
     * 保存的文件路径,只有当文件下载完后才有值,{@link #state}等于{@link DownloadState#COMPLETE}
     */
    private String savePath;

    /**
     * 下载文件的总大小
     */
    private long fileSize;

    /**
     * 当前下载的总大小
     */
    private long downloadSize;

    /**
     * 下载的速度,单位是B/s，可以转换格式{@link Utils#formatSpeed(long)}
     */
    private long speed;

    /**
     * 预计下载完成剩余时间
     */
    private long leftTime;

    public void setSpeed(long speed) {
        this.speed = speed;
    }

    public long getLeftTime() {
        return leftTime;
    }

    public void setLeftTime(long leftTime) {
        this.leftTime = leftTime;
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

    public String getFormatSpeed() {
        return Utils.formatSpeed(speed);
    }

    public String getFormatLeftTime() {
        return Utils.formatTime(leftTime);
    }

    @Override
    public String toString() {
        return "CallbackInfo{" +
                "downloadPath='" + downloadPath + '\'' +
                ", state=" + DownloadState.toName(state) +
                ", savePath='" + savePath + '\'' +
                ", fileSize=" + fileSize +
                ", downloadSize=" + downloadSize +
                ", speed=" + Utils.formatSpeed(speed) +
                '}';
    }
}
