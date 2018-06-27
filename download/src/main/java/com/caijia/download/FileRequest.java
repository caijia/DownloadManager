package com.caijia.download;

import java.util.List;
import java.util.Map;

/**
 * Created by cai.jia on 2018/6/26.
 */
public class FileRequest {

    private String url;
    private String method;
    private Map<String, List<String>> headers;
    private Map<String, List<String>> params;

    private FileRequest() {

    }

    FileRequest(Builder builder) {

    }

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public Map<String, List<String>> getParams() {
        return params;
    }

    public void setParams(Map<String, List<String>> params) {
        this.params = params;
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    public static class Builder {
        private String url;
        private String method;
        private Map<String, List<String>> headers;
        private Map<String, List<String>> params;

        public Builder(FileRequest fileRequest) {
            this.url = fileRequest.url;
            this.method = fileRequest.method;
            this.headers = fileRequest.headers;
            this.params = fileRequest.params;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder header(String key, String value) {
            return this;
        }

        public Builder header(String key, List<String> values) {
            return this;
        }

        public FileRequest build() {
            return new FileRequest(this);
        }
    }
}
