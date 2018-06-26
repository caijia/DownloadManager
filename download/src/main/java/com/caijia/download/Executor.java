package com.caijia.download;

import java.io.InputStream;

/**
 * Created by cai.jia on 2018/6/26.
 */
public interface Executor {

    InputStream execute(FileRequest request);
}
