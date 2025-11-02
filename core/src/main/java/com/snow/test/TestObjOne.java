package com.snow.test;

import com.snow.annotations.*;
import com.snow.annotations.methods.Get;
import com.snow.annotations.methods.Post;
import com.snow.annotations.params.FromBody;
import com.snow.annotations.params.FromQuery;
import com.snow.annotations.params.FromRoute;
import com.snow.util.Lifetime;

import java.util.concurrent.CompletableFuture;

@Component(Lifetime.SCOPED)
@Controller("users")
public class TestObjOne {

    @Inject
    public TestObjOne(TestObjTwo two) {
        System.out.println("TestObjOne instantiated");
    }

    @Get("{two}/")
    @RequiredRoles({"admin=true", "sub=1234567890"})
    public CompletableFuture<String> getUsers(@FromQuery String one, @FromRoute String two) throws InterruptedException {
        Thread.sleep(1000);
        return CompletableFuture.supplyAsync(() -> one + " " + two);
    }

    @Post
    public User setUser(@FromBody User user) {
        return user;
    }

    @Post("settings/")
    public String submitSettings() {
        return "settings";
    }
}
