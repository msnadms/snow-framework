package com.snow.http;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpUtil {

    public static String getRoutingKey(String method, String route) {
        return getRoutingKey(method, "", route);
    }

    public static String getRoutingKey(String method, String controllerRoute, String route) {
        return method + " " +  controllerRoute + "/" + route;
    }

    public static String getSimpleRoute(String route) {
        int idx = route.indexOf('?');
        return idx == -1 ? route : route.substring(0, idx);
    }

    public static Map<String, String> getQueryParams(String route) {
        int idx = route.indexOf('?');
        if (idx == -1) {
            return Collections.emptyMap();
        }
        String[] params = route.substring(idx + 1).split("&");
        Map<String, String> paramMap = new HashMap<>();
        for (String kvs : params) {
            String[] kv = kvs.split("=");
            paramMap.put(kv[0], kv[1]);
        }
        return paramMap;
    }
}
