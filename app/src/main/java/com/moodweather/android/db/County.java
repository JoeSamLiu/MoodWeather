package com.moodweather.android.db;

import org.litepal.crud.DataSupport;

/**
 * Created by THOMASLEWIS on 2017/10/30.
 */

public class County extends DataSupport {
    private int id;//数据库id
    private String countyName;//县城名称
    private String weatherId;//县城所对应的天气id
    private int cityId;//县城所属的城市id

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}
