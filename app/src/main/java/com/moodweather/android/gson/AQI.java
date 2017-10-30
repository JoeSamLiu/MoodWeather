package com.moodweather.android.gson;

/**
 * Created by THOMASLEWIS on 2017/10/27.
 */

/**
 * 假如api的具体内容如下
 */
/*
"aqi":{
  "city":{
    "aqi": "44",
    "pm25": "13"
  }
}
 */

import com.google.gson.annotations.SerializedName;

/**
 * 那么对应新建类如下
 */

public class AQI {
    @SerializedName("city")
    public AQICity city;

    public class AQICity {
        @SerializedName("aqi")
        public String aqi;//空气质量
        @SerializedName("pm25")
        public String pm25;//空气PH值
        @SerializedName("qlty")
        public String quality;//空气质量等级
    }
}
