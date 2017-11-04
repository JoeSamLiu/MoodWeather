package com.moodweather.android.util;

import android.app.Activity;
import android.app.Service;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * 读取SharedPreference的文件
 */

public class CacheUtil {
    public String getCache(Activity activity , String fileName){
        LogUtil.i(activity.toString(),"活动调用了缓存！");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String weatherSting = sharedPreferences.getString(fileName, null);
        return weatherSting;
    }
    public String getCache(Service service, String fileName){
        LogUtil.i(service.toString(),"服务调用了缓存!");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(service);
        String weatherSting = sharedPreferences.getString(fileName, null);
        return weatherSting;
    }
}
