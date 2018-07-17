package com.caijia.download;

/**
 * Created by cai.jia on 2018/7/4.
 */
public interface BreakPointManager {

    /**
     * 保存线程下载的长度
     *
     * @param threadIndex  线程索引
     * @param threadCount  线程个数
     * @param downloadSize 线程下载的总长度
     * @param saveFileDir  下载保存目录
     * @param fileRequest  下载请求链接
     */
    void saveDownloadLength(int threadIndex, int threadCount, long downloadSize,
                            String saveFileDir, FileRequest fileRequest);

    /**
     * 获取线程下载的长度
     *
     * @param threadIndex 线程索引
     * @param threadCount 线程个数
     * @param saveFileDir 下载保存目录
     * @param fileRequest 下载请求链接
     * @return 线程下载的总长度
     */
    long getDownloadLength(int threadIndex, int threadCount,
                           String saveFileDir, FileRequest fileRequest);


    /**
     * 资源释放
     */
    void release();
}
