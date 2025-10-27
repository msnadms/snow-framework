package com.snow.test;

public class User {

    public int id;

    public String firstName;

    public String lastName;

    @Override
    public String toString() {
        return id + " " + firstName + " " + lastName;
    }
}
