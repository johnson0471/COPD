package com.example.copd;

import java.util.Date;
import java.util.Locale;

public class Item implements java.io.Serializable {



    private long id;
    private String name;
    private String dateTime;
    private String stageTime;
    private String heartRate;
    private String stage;

    public Item(){
        name = "NULL";
    }

    public Item(long id, String name, String dateTime, String stageTime,
                String heartRate, String stage){
        this.id = id;
        this.name = name;
        this.dateTime = dateTime;
        this.stageTime = stageTime;
        this.heartRate = heartRate;
        this.stage = stage;
    }

    public long getId(){
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){ this.name = name; }

    public String getDatetime() {
        return dateTime;
    }

    // 裝置區域的日期時間
    public String getLocaleDatetime() {
        return String.format(Locale.getDefault(), "%tF  %<tR", new Date(dateTime));
    }

    // 裝置區域的日期
    public String getLocaleDate() {
        return String.format(Locale.getDefault(), "%tF", new Date(dateTime));
    }

    // 裝置區域的時間
    public String getLocaleTime() {
        return String.format(Locale.getDefault(), "%tR", new Date(dateTime));
    }

    public void setDatetime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getStageTime(){
        return stageTime;
    }

    public void setStageTime(String stageTime){ this.stageTime = stageTime; }

    public String getHeartRate(){
        return heartRate;
    }

    public void setHeartRate(String heartRate){ this.heartRate = heartRate; }

    public String getStage(){
        return stage;
    }

    public void setStage(String stage){ this.stage = stage; }
}
