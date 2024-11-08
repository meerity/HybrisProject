package com.epam.training.model;

public class UserWithoutAddressModel {

    String uid;
    String name;

    public UserWithoutAddressModel() {
    }

    public UserWithoutAddressModel(String uid, String name) {
        this.uid = uid;
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
