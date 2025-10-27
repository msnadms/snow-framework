package com.snow.test;

import com.snow.annotations.Component;
import com.snow.annotations.Inject;

@Component()
public class TestObjTwo {

    @Inject
    public TestObjTwo(TestObjThree three, TestObjFour four) {
        System.out.println("TestObjTwo instantiated");
    }
}
