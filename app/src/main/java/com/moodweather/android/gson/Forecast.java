package com.moodweather.android.gson;

/**
 * Created by THOMASLEWIS on 2017/10/27.
 */
/**
 *假如now的具体内容如下
 */
/*
"daily_forecast":[
{
  "date": "2017-10-27",
    "cond":{
    "txt_d": "阵雨"
  }
    "tmp":{
    "max": "26",
    "min": "15"
  }
}，
{
  "date": "2017-10-28",
    "cond":{
    "txt_d": "晴"
  }
    "tmp":{
    "max": "26",
    "min": "15"
  }
}，
  …………
]
 */

import com.google.gson.annotations.SerializedName;

/**
 * 该类有点特殊，采用集合类型声明，新建类如下
 */

public class Forecast {
    //    @SerializedName("date")
    public String date;//预报日期
    @SerializedName("astro")
    public Astro astro;//日出日落，月出月落

    public class Astro {
        @SerializedName("mr")
        public String moonRise;//月升
        @SerializedName("ms")
        public String moonSet;//月落
        @SerializedName("sr")
        public String sunRise;//日出
        @SerializedName("ss")
        public String sunSet;//日落

    }
    @SerializedName("tmp")
    public Temperature temperature;//温度

    public class Temperature {
        @SerializedName("max")
        public String max;//最高温度
        @SerializedName("min")
        public String min;//最低温度
    }
    @SerializedName("cond")
    public More more;//阴晴情况

    public class More {
        @SerializedName("txt_d")
        public String info;//阴晴情况
    }
    @SerializedName("uv")
    public String ultraviolet;//紫外线
    @SerializedName("vis")
    public String visibleSpectrometry;//可见光
    @SerializedName("wind")
    public Wind wind;//风

    public class Wind {
        //        @SerializedName("deg")
        public Wind windDeg;//风?
        //        @SerializedName("dir")
        public Wind windDir;//风向
        //        @SerializedName("sc")
        public Wind windStr;//风强
    }
}
