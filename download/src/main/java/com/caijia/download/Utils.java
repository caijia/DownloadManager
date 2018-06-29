package com.caijia.download;

import android.text.TextUtils;

import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

/**
 * Created by cai.jia on 2018/6/27.
 */
public class Utils {

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

    public static String getHeader(String key,Map<String, List<String>> headers) {
        if (headers == null || headers.isEmpty() || TextUtils.isEmpty(key)) {
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
}
