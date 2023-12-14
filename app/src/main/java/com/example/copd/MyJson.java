package com.example.copd;

import com.google.gson.annotations.SerializedName;

public class MyJson {

    public String getMyMsg() {
        return myMsg;
    }

    public void setMyMsg(String myMsg) {
        this.myMsg = myMsg;
    }

    @SerializedName("msg")
    private String myMsg;

    public String getMyAge() {
        return myAge;
    }

    public void setMyAge(String myAge) {
        this.myAge = myAge;
    }

    @SerializedName("age")
    private String myAge;

    public String getMyHeight() {
        return myHeight;
    }

    public void setMyHeight(String myHeight) {
        this.myHeight = myHeight;
    }

    @SerializedName("height")
    private String myHeight;

    public String getMyGender() {
        return myGender;
    }

    public void setMyGender(String myGender) {
        this.myGender = myGender;
    }

    @SerializedName("gender")
    private String myGender;

    public String getUserName(){ return userName;}

    @SerializedName("name")
    private String userName;


}

class classURL{
    static String ApplicationURL="http://140.120.183.226/xampp/copd/";

}

class userInfo{
    static String userName;
    static int userHeight;
    static String Account;
    static int userAge;
    static String userGender;
}