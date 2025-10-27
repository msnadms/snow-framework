package com.snow.test;

import com.snow.annotations.Component;
import com.snow.annotations.Inject;

@Component
public class TestObjFour {

    @Inject
    public TestObjFour() {
        System.out.println("TestObjFour instantiated");
    }
}
