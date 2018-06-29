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
    private HttpMethod method;
    private Map<String, List<String>> headers;
    private Map<String, List<String>> queryParams;
    private Map<String, List<String>> fieldParams;
    private String bodyJsonString;

    private FileRequest() {

    }

    FileRequest(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.headers = builder.headers;
        this.queryParams = builder.queryParams;
        this.fieldParams = builder.fieldParams;
        this.bodyJsonString = builder.bodyJsonString;

        if (TextUtils.isEmpty(url)) {
            throw new RuntimeException("url is null");
        }

        if (this.method == null) {
            this.method = HttpMethod.GET;
        }
    }

    public static FileRequest toSampleRequest(String url) {
        return new Builder().method(HttpMethod.GET).url(url).build();
    }

    public String getUrl() {
        return url;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public Map<String, List<String>> getQueryParams() {
        return queryParams;
    }

    public Map<String, List<String>> getFieldParams() {
        return fieldParams;
    }

    public String getBodyJsonString() {
        return bodyJsonString;
    }

    public Builder newBuilder() {
        return new Builder(this);
    }

    public static class Builder {
        private String url;
        private HttpMethod method;
        private Map<String, List<String>> headers;
        private Map<String, List<String>> queryParams;
        private Map<String, List<String>> fieldParams;
        private String bodyJsonString;

        public Builder() {
        }

        public Builder(FileRequest fileRequest) {
            this.url = fileRequest.url;
            this.method = fileRequest.method;
            this.headers = fileRequest.headers;
            this.queryParams = fileRequest.queryParams;
            this.fieldParams = fileRequest.fieldParams;
            this.bodyJsonString = fileRequest.bodyJsonString;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder method(HttpMethod method) {
            this.method = method;
            return this;
        }

        public Builder bodyJsonString(String bodyString) {
            this.bodyJsonString = bodyString;
            return this;
        }

        public Builder header(String key, String value) {
            if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
                return this;
            }
            headers = setValue(key, value, headers);
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

        public Builder queryParams(String key, String value) {
            if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
                return this;
            }
            queryParams = setValue(key, value, queryParams);
            return this;
        }

        public Builder queryParams(String key, List<String> values) {
            if (!TextUtils.isEmpty(key) && values != null && !values.isEmpty()) {
                for (String value : values) {
                    queryParams(key, value);
                }
            }
            return this;
        }

        public Builder fieldParams(String key, String value) {
            if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
                return this;
            }
            fieldParams = setValue(key, value, fieldParams);
            return this;
        }

        public Builder fieldParams(String key, List<String> values) {
            if (!TextUtils.isEmpty(key) && values != null && !values.isEmpty()) {
                for (String value : values) {
                    fieldParams(key, value);
                }
            }
            return this;
        }

        private Map<String, List<String>> setValue(String key, String value,
                                                   Map<String, List<String>> params) {
            if (params == null) {
                params = new HashMap<>();
            }

            List<String> values = params.get(key);
            if (values == null) {
                values = new LinkedList<>();
                params.put(key, values);
            }
            values.add(value);
            return params;
        }

        public FileRequest build() {
            return new FileRequest(this);
        }
    }
}
