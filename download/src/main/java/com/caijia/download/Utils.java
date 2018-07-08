package com.caijia.download;

import android.os.Build;
import android.util.Log;

import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

/**
 * Created by cai.jia on 2018/6/27.
 */
class Utils {

    public static boolean permitsRequestBody(String method) {
        return !(method.equals("GET") || method.equals("HEAD"));
    }

    public static String getHeader(String key, Map<String, List<String>> headers) {
        if (headers == null || headers.isEmpty() || Utils.isEmpty(key)) {
            return "";
        }

        String contentLengthValue = null;
        for (Map.Entry<String, List<String>> keyValues : headers.entrySet()) {
            if (key.equalsIgnoreCase(keyValues.getKey())) {
                List<String> value = keyValues.getValue();
                if (value != null && !value.isEmpty()) {
                    contentLengthValue = value.get(0);
                    break;
                }
            }
        }
        return contentLengthValue;
    }

    public static String getMD5(String info) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(info.getBytes("UTF-8"));
            byte[] encryption = md5.digest();

            StringBuilder sb = new StringBuilder();
            for (byte anEncryption : encryption) {
                if (Integer.toHexString(0xff & anEncryption).length() == 1) {
                    sb.append("0").append(Integer.toHexString(0xff & anEncryption));
                } else {
                    sb.append(Integer.toHexString(0xff & anEncryption));
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static void log(boolean debug,String msg) {
        if (!debug) {
            return;
        }
        if (isAndroidPlatform()) {
            Log.d("fileDownloader", msg);

        }else{
            System.out.println(msg);
        }
    }

    public static void log(boolean debug,int threadIndex, String msg) {
        log(debug,"thread " + threadIndex + " -> " + msg);
    }

    public static String mapToString(Map<String, List<String>> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            if (values != null && !values.isEmpty()) {
                for (String value : values) {
                    sb.append(key).append(":").append(value);
                }
            }
        }
        return sb.toString();
    }

    public static String fileRequestToMd5String(FileRequest request, int threadCount) {
        String sb = request.getUrl() +
                request.getMethod() +
                request.getBodyJsonString() +
                mapToString(request.getQueryParams()) +
                mapToString(request.getFieldParams()) +
                mapToString(request.getHeaders()) +
                threadCount;
        return getMD5(sb);
    }

    public static boolean isAndroidPlatform() {
        try {
            Class.forName("android.os.Build");
            if (Build.VERSION.SDK_INT != 0) {
                return true;
            }
        } catch (ClassNotFoundException ignored) {
        }
        return false;
    }
}
