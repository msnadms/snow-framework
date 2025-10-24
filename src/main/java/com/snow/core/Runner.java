package com.snow.core;

import com.snow.di.ApplicationContext;
import com.snow.test.TestObjOne;

import java.io.IOException;

public class Runner {

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        //SnowServer server = SnowServer.create(8080, 5);
        //server.start();
        var context = ApplicationContext.get("com.snow");
        context.createComponent(TestObjOne.class);
    }
}
