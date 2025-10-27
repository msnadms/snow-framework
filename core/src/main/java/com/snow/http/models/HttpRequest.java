package com.snow.http.models;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface HttpRequest {

    String method();
    String route();
    Map<String, List<String>> headers();
    InputStream body() throws IOException;

}
