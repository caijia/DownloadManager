package com.caijia.download;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by cai.jia on 2018/6/26.
 */
public class OkHttpConnection implements Connection {

    private static final MediaType JSON_CONTENT_TYPE =
            MediaType.parse("application/json");

    private OkHttpClient okHttpClient;

    public OkHttpConnection(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient == null ? new OkHttpClient() : okHttpClient;
    }

    public OkHttpConnection() {
        this(null);
    }

    @Override
    public @Nullable
    FileResponse connect(FileRequest fileRequest) {
        HttpUrl httpUrl = HttpUrl.parse(fileRequest.getUrl());
        if (httpUrl == null) {
            return null;
        }

        //queryParams
        final HttpUrl.Builder httpUrlBuilder = httpUrl.newBuilder();
        Map<String, List<String>> queryParams = fileRequest.getQueryParams();
        setKeyValues(queryParams, new Callback() {
            @Override
            public void call(String key, String value) {
                httpUrlBuilder.addQueryParameter(key, value);
            }
        });
        httpUrl = httpUrlBuilder.build();

        //headers
        final Request.Builder headerBuilder = new Request.Builder().url(httpUrl);
        Map<String, List<String>> headers = fileRequest.getHeaders();
        setKeyValues(headers, new Callback() {
            @Override
            public void call(String key, String value) {
                headerBuilder.header(key, value);
            }
        });

        //body
        RequestBody requestBody;
        String bodyJsonString = fileRequest.getBodyJsonString();
        if (!TextUtils.isEmpty(bodyJsonString)) {
            requestBody = RequestBody.create(JSON_CONTENT_TYPE, bodyJsonString);

        } else {
            Map<String, List<String>> fieldParams = fileRequest.getFieldParams();
            requestBody = getFormBody(fieldParams);
        }

        String method = fileRequest.getMethod().name();
        if (!Utils.permitsRequestBody(method)) {
            headerBuilder.method(method, null);

        } else {
            headerBuilder.method(method, requestBody);
        }
        Request request = headerBuilder.build();

        try {
            Response response = okHttpClient.newCall(request).execute();
            String realDownloadUrl = response.request().url().toString();
            ResponseBody body = response.body();
            if (response.isSuccessful()) {
                FileResponse fileResponse = new FileResponse();
                fileResponse.setHeaders(response.headers().toMultimap());
                fileResponse.setRealDownloadUrl(realDownloadUrl);
                if (body != null) {
                    fileResponse.setByteStream(body.byteStream());
                }
                return fileResponse;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private RequestBody getFormBody(Map<String, List<String>> fieldParams) {
        FormBody.Builder builder = new FormBody.Builder();
        if (fieldParams != null && !fieldParams.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : fieldParams.entrySet()) {
                String key = entry.getKey();
                List<String> values = entry.getValue();
                if (values != null && !values.isEmpty()) {
                    for (String value : values) {
                        builder.add(key, value);
                    }
                }
            }
        }
        return builder.build();
    }

    private void setKeyValues(Map<String, List<String>> map, Callback callback) {
        if (map != null && !map.isEmpty()) {
            for (Map.Entry<String, List<String>> paramsMap : map.entrySet()) {
                String key = paramsMap.getKey();
                List<String> values = paramsMap.getValue();
                if (values != null && !values.isEmpty()) {
                    for (String value : values) {
                        callback.call(key, value);
                    }
                }
            }
        }
    }

    private interface Callback {
        void call(String key, String value);
    }
}
