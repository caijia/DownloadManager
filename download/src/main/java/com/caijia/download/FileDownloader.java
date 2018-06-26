package com.caijia.download;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by cai.jia on 2018/6/26.
 */
public class FileDownloader {

    public static void main(String[] args) {
        new FileDownloader().download(null);
    }


    public void download(FileRequest fileRequest) {
        OkHttpClient client = new OkHttpClient();
        HttpUrl httpUrl = HttpUrl.parse("http://202.96.98.106:8089/imageserver1/app/dyhzz.apk");
        if (httpUrl != null) {
            httpUrl = httpUrl.newBuilder()
                    .addQueryParameter("", "")
                    .build();
            Request request = new Request.Builder()
                    .url(httpUrl)
                    .get()
                    .build();

            try {
                Response response = client.newCall(request).execute();
                ResponseBody body = response.body();
                if (response.isSuccessful() && body != null) {
                    InputStream inputStream = body.byteStream();
                    RandomAccessFile accessFile = new RandomAccessFile("E://dyhzz1.apk","rws");
                    accessFile.setLength(body.contentLength());
                    accessFile.seek(0);
                    byte[] buffer = new byte[1024 * 8];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        accessFile.write(buffer, 0, len);
                    }
                    accessFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



    }
}
