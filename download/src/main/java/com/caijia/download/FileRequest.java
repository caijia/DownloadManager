package com.caijia.download;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by cai.jia on 2018/6/26.
 */
public class FileRequest {

    private String url;
    private String method;
    private Map<String, List<String>> headers;
    private Map<String, List<String>> queryParams;

    private FileRequest() {

    }

    FileRequest(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.headers = builder.headers;
        this.queryParams = builder.queryParams;
    }

    public static FileRequest toSampleRequest(String url) {
        return new Builder().method("GET").url(url).build();
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

    public Map<String, List<String>> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(Map<String, List<String>> queryParams) {
        this.queryParams = queryParams;
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    public static class Builder {
        private String url;
        private String method;
        private Map<String, List<String>> headers;
        private Map<String, List<String>> queryParams;

        public Builder() {
        }

        public Builder(FileRequest fileRequest) {
            this.url = fileRequest.url;
            this.method = fileRequest.method;
            this.headers = fileRequest.headers;
            this.queryParams = fileRequest.queryParams;
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
            if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
                return this;
            }
            setValue(key, value, headers);
            return this;
        }

        public Builder header(String key, List<String> values) {
            if (!TextUtils.isEmpty(key) && values != null && !values.isEmpty()) {
                for (String value : values) {
                    header(key, value);
                }
            }
            return this;
        }

        public Builder params(String key, String value) {
            if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
                return this;
            }
            setValue(key, value, queryParams);
            return this;
        }

        public Builder params(String key, List<String> values) {
            if (!TextUtils.isEmpty(key) && values != null && !values.isEmpty()) {
                for (String value : values) {
                    header(key, value);
                }
            }
            return this;
        }

        private void setValue(String key, String value, Map<String, List<String>> params) {
            if (params == null) {
                params = new HashMap<>();
            }

            List<String> values = params.get(key);
            if (values == null) {
                values = new LinkedList<>();
                params.put(key, values);
            }
            values.add(value);
        }

        public FileRequest build() {
            return new FileRequest(this);
        }
    }
}
