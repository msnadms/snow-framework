package com.snow.util;

import java.util.*;
import java.util.regex.Pattern;

public class HttpUtil {

    private static final Pattern LEADING_SLASH = Pattern.compile("^/+");
    private static final Pattern TRAILING_SLASH = Pattern.compile("/+$");
    private static final Pattern MULTI_SLASH = Pattern.compile("/+");

    public static String getRoutingKey(String controllerRoute, String route) {
        return controllerRoute + "/" + route;
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

    public static Map<String, String> getRouteParams(String controllerRoute, List<String> routeParams) {
        String[] segments = HttpUtil.normalizeRoute(controllerRoute).split("/");
        Map<String, String> paramMap = new HashMap<>();
        int idx = 0;
        for (String segment : segments) {
            if (segment.startsWith("{") && segment.endsWith("}")) {
                paramMap.put(segment.substring(1, segment.length() - 1), routeParams.get(idx++));
            }
        }
        return paramMap;
    }

    public static String normalizeRoute(String route) {
            return MULTI_SLASH.matcher(
                    TRAILING_SLASH.matcher(
                            LEADING_SLASH.matcher(route)
                                    .replaceAll(""))
                            .replaceAll(""))
                    .replaceAll("/");
    }
}
