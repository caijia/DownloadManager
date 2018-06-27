package com.caijia.download;

import java.util.List;
import java.util.Map;

/**
 * Created by cai.jia on 2018/6/27.
 */
public class HttpUtils {

    public static boolean requiresRequestBody(String method) {
        return method.equals("POST")
                || method.equals("PUT")
                || method.equals("PATCH")
                || method.equals("PROPPATCH")
                || method.equals("REPORT");
    }

    public static boolean permitsRequestBody(String method) {
        return !(method.equals("GET") || method.equals("HEAD"));
    }

    public static long contentLength(Map<String, List<String>> headers) {
        if (headers == null || headers.isEmpty()) {
            return -1;
        }

        String contentLengthValue = null;
        for (Map.Entry<String, List<String>> keyValues : headers.entrySet()) {
            if ("Content-Length".equalsIgnoreCase(keyValues.getKey())) {
                List<String> value = keyValues.getValue();
                if (value != null && !value.isEmpty()) {
                    contentLengthValue = value.get(0);
                    break;
                }
            }
        }

        if (contentLengthValue != null && contentLengthValue.length() > 0) {
            try {
                return Long.parseLong(contentLengthValue);
            } catch (Exception e) {
                return -1;
            }
        }
        return -1;
    }
}
