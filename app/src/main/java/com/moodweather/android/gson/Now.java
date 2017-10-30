package com.moodweather.android.gson;

/**
 * Created by THOMASLEWIS on 2017/10/27.
 */
/**
 * 假如now的具体内容如下
 */
/*
"now":{
  "tmp": "29",
    "cond":{
    "txt": "阵雨"
  }
}
 */

import com.google.gson.annotations.SerializedName;

/**
 * 那么对应新建类如下
 */
public class Now {
    @SerializedName("tmp")
    public String temperature;//当前温度

    @SerializedName("cond")
    public More more;

    public class More {
        @SerializedName("txt")
        public String info;//阴晴情况
    }
}
