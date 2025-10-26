package com.snow.test;

import com.snow.annotations.Component;
import com.snow.annotations.Controller;
import com.snow.annotations.Inject;
import com.snow.annotations.Route;
import com.snow.annotations.params.FromQuery;
import com.snow.annotations.params.FromRoute;
import com.snow.util.Lifetime;

@Component(Lifetime.SCOPED)
@Controller("users")
public class TestObjOne {

    @Inject
    public TestObjOne(TestObjTwo two) {
        System.out.println("TestObjOne instantiated");
    }

    @Route(path = "{two}", method = "GET")
    public String getUsers(@FromQuery("one") String one, @FromRoute("two") String two) {
        return one + " " + two;
    }

    @Route(path = "settings", method = "POST")
    public String submitSettings() {
        return "settings";
    }
}
