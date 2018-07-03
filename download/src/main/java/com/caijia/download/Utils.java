package com.caijia.download;

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

    public static void log(String msg) {
        System.out.println(msg);
    }
}
