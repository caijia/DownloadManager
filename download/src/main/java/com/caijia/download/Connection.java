package com.caijia.download;

import android.support.annotation.Nullable;

/**
 * Created by cai.jia on 2018/6/26.
 */
public interface Connection {

    @Nullable FileResponse connect(FileRequest request);
}
