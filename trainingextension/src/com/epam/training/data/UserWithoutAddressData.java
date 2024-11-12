package com.epam.training.data;

public class UserWithoutAddressData {

    String uid;
    String name;

    public UserWithoutAddressData() {
    }

    public UserWithoutAddressData(String uid, String name) {
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
