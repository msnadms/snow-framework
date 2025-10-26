package com.snow.test;

import com.snow.annotations.Component;
import com.snow.annotations.Controller;
import com.snow.annotations.Inject;
import com.snow.annotations.methods.Get;
import com.snow.annotations.methods.Post;
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

    @Get("{two}/")
    public String getUsers(@FromQuery String one, @FromRoute String two) {
        return one + " " + two;
    }

    @Post("settings/")
    public String submitSettings() {
        return "settings";
    }
}
