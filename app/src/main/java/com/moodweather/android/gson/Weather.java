package com.moodweather.android.gson;

import com.google.gson.annotations.SerializedName;
import com.moodweather.android.gson.AQI;
import com.moodweather.android.gson.Basic;
import com.moodweather.android.gson.Now;
import com.moodweather.android.gson.Suggestion;

import java.util.List;

/**
 * Created by THOMASLEWIS on 2017/10/27.
 */

public class Weather {
    @SerializedName("status")
    public String status;//接口状态码
    @SerializedName("aqi")
    public AQI aqi ;//空气质量接口数据
    @SerializedName("basic")
    public Basic basic;//基础信息
    @SerializedName("now")
    public Now now;//当前天气数据
    @SerializedName("suggestion")
    public Suggestion suggestion;//建议
    /**
     * daily_forecast包含的是一个数组，使用List集合引用Forecast类。
     */
    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
