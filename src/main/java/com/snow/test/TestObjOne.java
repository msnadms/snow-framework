package com.snow.test;

import com.snow.annotations.Component;
import com.snow.annotations.Inject;
import com.snow.util.Lifetime;

@Component(Lifetime.SINGLETON)
public class TestObjOne {

    @Inject
    public TestObjOne(TestObjTwo two) {
        System.out.println("TestObjOne instantiated");
    }
}
