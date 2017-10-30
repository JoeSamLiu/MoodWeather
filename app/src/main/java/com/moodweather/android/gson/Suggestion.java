package com.moodweather.android.gson;

/**
 * Created by THOMASLEWIS on 2017/10/27.
 */
/**
 * 假如suggestion的具体内容如下
 */
/*
"suggestion":{
  "comf":{
    "txt": "白天天气较热，虽然有雨，但仍然无法削弱较高气温给人带来的暑意，这种天气会让您感到不舒适。"
  },
    "cw":{
    "txt": "不宜洗车，未来24小时有雨，如果在此期间洗车，雨水和路上的泥水会弄脏您的爱车。"
  },
  "sport":{
    "txt": "有降水，风力较强，建议您在室内进行低强度运动；若坚持户外运动，建议您选择避雨防风的地点。"
  }
}
 */

import com.google.gson.annotations.SerializedName;

/**
 * 那么对应新建类如下
 */

public class Suggestion {
    @SerializedName("air")
    public Air air;//空气质量等级

    @SerializedName("comf")
    public Comfort comfort;//舒适度

    @SerializedName("cw")
    public CarWash carWash;//洗车指数

    @SerializedName("sport")
    public Sport sport;//运动指数

    public class Comfort {
        @SerializedName("brf")
        public String brf;//体感度

        @SerializedName("txt")
        public String info;//建议
    }

    public class CarWash {
        @SerializedName("brf")
        public String brf;//体感度

        @SerializedName("txt")
        public String info;//建议
    }

    public class Sport {
        @SerializedName("brf")
        public String brf;//体感度

        @SerializedName("txt")
        public String info;//建议
    }

    private class Air {
        @SerializedName("brf")
        public String brf;//体感度

        @SerializedName("txt")
        public String info;//建议
    }
}
