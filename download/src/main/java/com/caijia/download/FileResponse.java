package com.caijia.download;

import android.text.TextUtils;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by cai.jia on 2018/6/26.
 */
public class FileResponse {

    private InputStream byteStream;
    private Map<String, List<String>> headers;
    private String realDownloadUrl;

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

    public String getRealDownloadUrl() {
        return realDownloadUrl;
    }

    public void setRealDownloadUrl(String realDownloadUrl) {
        this.realDownloadUrl = realDownloadUrl;
    }

    public long getContentLength() {
        String contentLength = Utils.getHeader("Content-Length", headers);
        long contentLengthLong = -1;
        try {
            contentLengthLong = Long.parseLong(contentLength);
        } catch (Exception e) {
        }
        return contentLengthLong;
    }

    public String getFileName() {
        String disposition = Utils.getHeader("Content-Disposition", headers);
        if (!TextUtils.isEmpty(disposition)) {
            Matcher m = Pattern.compile(".*filename=(.*)").matcher(disposition.toLowerCase());
            if (m.find()) return m.group(1);
        } else {
            return realDownloadUrl.substring(realDownloadUrl.lastIndexOf(File.separator) + 1);
        }
        return Utils.getMD5(realDownloadUrl) + ".temp";//默认取一个文件名;
    }
}
