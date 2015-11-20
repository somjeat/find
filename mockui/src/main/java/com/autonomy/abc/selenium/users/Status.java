package com.autonomy.abc.selenium.users;

public enum Status {
    PENDING, CONFIRMED;

    public static Status fromString(String status){
        if(status.toLowerCase().equals("confirmed")){
            return CONFIRMED;
        } else {
            return PENDING;
        }
    }
}
