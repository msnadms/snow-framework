package com.snow.test;

import com.snow.annotations.Component;
import com.snow.annotations.Inject;
import com.snow.util.Lifetime;

@Component(Lifetime.SINGLETON)
public class TestObjTwo {

    @Inject
    public TestObjTwo(TestObjThree three, TestObjFour four) {
        System.out.println("TestObjTwo instantiated");
    }
}
