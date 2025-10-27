package com.snow.http.models;

import java.io.InputStream;
import java.util.Map;

public interface HttpRequest {

    String method();
    String route();
    Map<String, String> headers();
    InputStream body();

}
