package com.caijia.download;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by cai.jia on 2018/7/5.
 */
public class AndroidSchedule implements Schedule{

    private Handler handler;

    public AndroidSchedule() {
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void schedule(Runnable runnable) {
        handler.post(runnable);
    }
}
