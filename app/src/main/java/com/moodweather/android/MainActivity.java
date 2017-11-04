package com.moodweather.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.moodweather.android.util.LogUtil;

public class MainActivity extends BasicActivity {
    public static final String TAG = "ManiActivity" ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //缓存数据的判断
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getString("weather",null) != null) {
            LogUtil.i(TAG,"主活动的Cache is "+sharedPreferences.getString("weather",null));
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
