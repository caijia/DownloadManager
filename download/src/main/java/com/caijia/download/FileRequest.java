package com.caijia.download;

import java.util.Map;

/**
 * Created by cai.jia on 2018/6/26.
 */
public class FileRequest {

    private String url;
    private String method;
    private Map<String,String> headers;
    private Map<String,Object> params;


    public static class Builder{
        private String url;
        private String method;
        private Map<String,String> headers;
        private Map<String,Object> params;



    }
}
