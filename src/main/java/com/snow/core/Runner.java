package com.snow.core;

import com.snow.di.ComponentScanner;
import com.snow.http.SnowServer;
import java.io.IOException;

public class Runner {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        //SnowServer server = SnowServer.create(8080, 5);
        //server.start();
        System.out.println(ComponentScanner.scan("com.snow"));
    }
}
