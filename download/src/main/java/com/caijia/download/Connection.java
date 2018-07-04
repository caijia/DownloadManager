package com.caijia.download;

/**
 * Created by cai.jia on 2018/6/26.
 */
public interface Connection {

    FileResponse connect(FileRequest request) throws Exception;
}
