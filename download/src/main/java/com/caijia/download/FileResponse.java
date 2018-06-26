package com.caijia.download;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by cai.jia on 2018/6/26.
 */
public class FileResponse {

    private InputStream byteStream;
    private Map<String,List<String>> headers;

    public InputStream getByteStream() {
        return byteStream;
    }

    public void setByteStream(InputStream byteStream) {
        this.byteStream = byteStream;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }
}
