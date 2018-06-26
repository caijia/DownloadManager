package com.caijia.download;

import android.support.annotation.Nullable;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by cai.jia on 2018/6/26.
 */
public class OkHttpConnection implements Connection {

    private OkHttpClient okHttpClient;

    public OkHttpConnection() {
        okHttpClient = new OkHttpClient();
    }

    @Override
    public @Nullable FileResponse connect(FileRequest fileRequest) {
        HttpUrl httpUrl = HttpUrl.parse("");
        if (httpUrl != null) {
            httpUrl = httpUrl.newBuilder()
                    .addQueryParameter("", "")
                    .build();
            Request request = new Request.Builder()
                    .url(httpUrl)
                    .method("", null)
                    .build();
            try {
                Response response = okHttpClient.newCall(request).execute();
                ResponseBody body = response.body();
                if (response.isSuccessful()) {
                    FileResponse fileResponse = new FileResponse();
                    fileResponse.setHeaders(response.headers().toMultimap());
                    if (body != null) {
                        fileResponse.setByteStream(body.byteStream());
                    }
                    return fileResponse;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
