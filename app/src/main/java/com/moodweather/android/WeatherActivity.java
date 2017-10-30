package com.moodweather.android;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.moodweather.android.gson.Forecast;
import com.moodweather.android.gson.Weather;
import com.moodweather.android.util.HttpUtil;
import com.moodweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by THOMASLEWIS on 2017/10/1.
 */

public class WeatherActivity extends AppCompatActivity {
    private ScrollView weatherLayout;
    /**
     * 用于显示城市名
     */
    private TextView titleCity;
    /**
     * 用于显示发布时间
     */
    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;
    /**
     * 用于显示空气质量
     */
    private TextView aqiText;
    /**
     * 用于显示空气酸碱性
     */
    private TextView pm25Text;
    /**
     * 用于显示舒适度
     */
    private TextView comfortText;
    /**
     * 用于显示是否适宜洗车
     */
    private TextView carWashText;
    /**
     * 用于显示是否合适运动
     */
    private TextView sportText;
    /**
     * 用于显示动态背景
     */
    private ImageView bingPicImg;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21 ){
            View decorView = getWindow().getDecorView();
            //活动的布局显示在状态栏上
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    |View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            //将状态栏设置成透明的
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        //初始化各控件
        bingPicImg = (ImageView)findViewById(R.id.bing_pic_img);
        weatherLayout = (ScrollView)findViewById(R.id.weather_layout);
        titleCity = (TextView)findViewById(R.id.title_city);
        titleUpdateTime = (TextView)findViewById(R.id.title_update_time);
        degreeText = (TextView)findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout)findViewById(R.id.forecast_layout);
        aqiText = (TextView)findViewById(R.id.aqi_text);
        pm25Text = (TextView)findViewById(R.id.pm25_text);
        comfortText = (TextView)findViewById(R.id.comfort_text);
        carWashText = (TextView)findViewById(R.id.car_wash_text);
        sportText = (TextView)findViewById(R.id.sport_text);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherSting = sharedPreferences.getString("weather", null);
        Log.d("WeatherActivity","weatherString is "+weatherSting);
        if (weatherSting != null){
            //有缓存时直接解析天气数据
            Log.d("WeatherActivity","使用了缓存");
            SharedPreferences userSettings = getSharedPreferences("setting",0);
            SharedPreferences.Editor editor = userSettings.edit();
            editor.clear();
            editor.commit();
            Weather weather = Utility.handleWeatherResponse(weatherSting);
            Log.d("WeatherActivity","A01weather.aqi is "+weather.aqi);
            showWeatherInfo(weather);
        }else {
            //无缓存时直接去服务器查询天气
            Log.d("WeatherActivity","直接访问服务器");
            String weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

        String bingPic = sharedPreferences.getString("bing_pic",null);
        if (bingPic != null){
            //有缓存直接读取背景
            Glide.with(this).load(bingPic).into(bingPicImg);
            Log.d("WeatherActivity","缓存取背景"+bingPicImg.toString());
        }else {
            //无缓存直接去服务器加载
            loadBingPic();
            Log.d("WeatherActivity","服务器取背景"+bingPicImg.toString());
        }
    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        Log.d("WeatherActivity","执行了loadBingPic()");
        String requestBingPic = "http://guolin.tech/api/bing_pic";//必应每日一图接口地址
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d("WeatherActivity","执行了onFailure()");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                Log.d("WeatherActivity","执行了onResponse()");
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic" , bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                        Log.d("WeatherActivity","执行了onResponse().run");
                    }
                });
            }
        });
    }

    /**
     * 根据城市id请求城市天气信息
     * @param weatherId
     */
    private void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid="+weatherId
                +"&key=9bff2107c1bd4be8b5ba7224ea82d82f";
        Log.d("WeatherActivity","weatherUrl is "+weatherUrl+"\nweatherId is "+weatherId);
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this ,
                                "获取天气信息失败" , Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather" , responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(WeatherActivity.this ,
                                    "获取天气信息失败" , Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        loadBingPic();
    }
    /**
     * 处理并展示weather实体类中的数据
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {
        Log.d("WeatherActivity","01weather.aqi is "+weather.aqi);
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature +"°C";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for (Forecast forecast :weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,
                    forecastLayout,false);
            TextView dateText = (TextView)view.findViewById(R.id.date_text);
            TextView infoText = (TextView)view.findViewById(R.id.info_text);
            TextView maxText = (TextView)view.findViewById(R.id.max_text);
            TextView minText = (TextView)view.findViewById(R.id.min_text);
            Log.d("WeatherActivity","date is "+forecast.date+"\ninfo is "+
                    forecast.more.info+"\nmax is "+forecast.temperature.max+
                    "\nmin is "+forecast.temperature.min);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            Log.d("WeatherActivity","dateText is "+dateText+"\ninfoText is "+
                    infoText+"\nmaxText is "+maxText+"\nminText is "+minText);
            forecastLayout.addView(view);
        }
        Log.d("WeatherActivity","02weather.aqi is "+weather.aqi);
        if (weather.aqi != null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
            Log.d("WeatherActivity","qai is "+weather.aqi.city.aqi+
                    "pm25 is "+weather.aqi.city.pm25);
        }
        String comfort = "舒适度: "+weather.suggestion.comfort.info;
        String carWash = "洗车指数: "+weather.suggestion.carWash.info;
        String sport = "运动建议: "+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);//请求数据时应该隐藏ScrollView，不然空数据界面不好看。
    }
}
