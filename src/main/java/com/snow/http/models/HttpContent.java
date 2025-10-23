package com.snow.http.models;

import java.nio.charset.StandardCharsets;

public class HttpContent {

    private final String content;

    public HttpContent(byte[] content) {
        this.content = new String(content, StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return content;
    }
}
