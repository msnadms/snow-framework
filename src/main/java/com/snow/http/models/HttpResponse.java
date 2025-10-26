package com.snow.http.models;

import java.io.OutputStream;

public interface HttpResponse {

    void status(int code);
    void header(String name, String value);
    OutputStream body();

}
