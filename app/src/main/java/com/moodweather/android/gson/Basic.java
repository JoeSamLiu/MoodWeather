package com.moodweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by THOMASLEWIS on 2017/10/27.
 * JSON中一些字段不适合直接用Java字段来命名，这里
 * 使用@SerializedName注解的方式来让JSON和Java之间建立映射关系。
 */

public class Basic {
    /**
     * 城市名称对应Json
     */
    @SerializedName("city")
    public String cityName;
    /**
     * 国家名称对应Json
     */
    @SerializedName("cnty")
    public String countryName;
    /**
     * 纬度对应Json
     */
    @SerializedName("lat")
    public String latitude;
    /**
     * 国家名称对应Json
     */
    @SerializedName("lon")
    public String longitude;

    @SerializedName("id")
    public String weatherId;

    @SerializedName("update")
    public Update update;

    public class Update {
        /**
         * 当地时间（北京东八区时间）+0800(领先UTC 8个小时)
         */
        @SerializedName("loc")
        public String updateTime;
        /**
         * 通用协调时间（Universal Time Coordinated）
         */
        @SerializedName("utc")
        public String utcTime;
    }
}
