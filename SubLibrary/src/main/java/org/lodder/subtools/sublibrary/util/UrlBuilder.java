package org.lodder.subtools.sublibrary.util;

import java.util.ArrayList;
import java.util.List;

public class UrlBuilder {
    private final String baseUrl;
    private final List<String> params = new ArrayList<>();

    public UrlBuilder(String domain, String path){
        this.baseUrl = domain + path;
    }

    public UrlBuilder addParam(String param){
        params.add(param);
        return this;
    }

    public UrlBuilder addParam(String name, String value){
        return addParam(name + "=" + value);
    }

    public String build(){
        return baseUrl  + "?" + String.join("&", params);
    }
}
