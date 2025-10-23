package com.snow.http.models;

import com.snow.exceptions.BadRequestException;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpHeaders {

    private final Map<String, String> headers = new HashMap<String, String>();

    public HttpHeaders(byte[] rawHeaders) {
        String[] lines =
                new String(rawHeaders, StandardCharsets.US_ASCII)
                .split("\r\n");
        for (String line : lines) {
            String[] fields = line.split(": ");
            if (fields.length != 2) {
                throw new BadRequestException("Invalid header format: " + line);
            }
            headers.put(fields[0].trim(), fields[1].trim());
        }
    }

    public HttpHeaders() {}

    public String setHeader(String key, String value) {
        return headers.put(key, value);
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    @Override
    public String toString() {
        return headers.toString();
    }
}
