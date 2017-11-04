package com.moodweather.android;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.moodweather.android.gson.Forecast;
import com.moodweather.android.gson.Weather;
import com.moodweather.android.util.CacheUtil;
import com.moodweather.android.util.HttpUtil;
import com.moodweather.android.util.LogUtil;
import com.moodweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by THOMASLEWIS on 2017/10/1.
 */

public class WeatherActivity extends BasicActivity{
    private ScrollView weatherLayout;
    private TextView titleCity;//用于显示城市名
    private TextView titleUpdateTime;//用于显示发布时间
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;//用于显示空气质量
    private TextView pm25Text;//用于显示空气酸碱性
    private TextView comfortText;//用于显示舒适度
    private TextView carWashText;//用于显示是否适宜洗车
    private TextView sportText;//用于显示是否合适运动
    private ImageView bingPicImg;//用于显示动态背景图片
    public SwipeRefreshLayout swipeRefreshLayout;//用于下拉更新
    public DrawerLayout drawerLayout;//用于滑出菜单的布局
    private Button navButton;//用于弹出菜单的按钮
    String weatherId;
    private LinearLayout weatherTouch;
    public static final String TAG = "WeatherActivity" ;
    private boolean isFirst;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarVisibility();//设置状态栏属性
        setContentView(R.layout.activity_weather);
        initComponent();//初始化各控件
        getSystemCache();//获取缓存
        swipeUpdate();//下拉刷新
        showMenuFromSide();//弹出菜单
    }

    /**
     * 滑动弹出菜单列表
     */
    private void showMenuFromSide() {
        //屏幕触屏滑动监听
        weatherTouch.setOnTouchListener(new View.OnTouchListener() {

            public float mCurPosY;
            public float mCurPosX;
            public float mPosX;
            public float mPosY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                switch (event.getAction()) {
                    //单点触摸按下动作
                    case MotionEvent.ACTION_DOWN:
                        mPosX = event.getX();
                        mPosY = event.getY();
                        break;
                    //单点你触摸移动动作
                    case MotionEvent.ACTION_MOVE:
                        mCurPosX = event.getX();
                        mCurPosY = event.getY();
                        break;
                    //单点触摸离开动作
                    case MotionEvent.ACTION_UP:
                        if (mCurPosX - mPosX < 0
                                && (Math.abs(mCurPosX - mPosX) > 50)) {
                            //向左滑动

                        } else if (mCurPosX - mPosX > 0
                                && (Math.abs(mCurPosX - mPosX) > 100)) {
                            //向右滑动
                            drawerLayout.openDrawer(GravityCompat.START);//滑出菜单
                        }
                        //                        else if (mCurPosY - mPosY < 0
                        //                                && (Math.abs(mCurPosY - mPosY)) > 250){
                        //                            //向上滑动
                        //                        }else if (mCurPosY - mPosY > 0
                        //                                && (Math.abs(mCurPosY - mPosY)) > 250){
                        //                            //向下滑动
                        //                        }
                        break;
                }
                return true;
            }
        });
        LogUtil.i(TAG,"右滑滑出菜单已经启动！");
    }

    /**
     * 下拉更新天气和背景
     */
    private void swipeUpdate() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            LogUtil.i(TAG,"刷新时的weatherId 是 "+weatherId);
            requestWeather(weatherId);
        });
        LogUtil.i(TAG,"已（下拉）更新完毕！");
    }

    /**
     * 判断缓存并作出处理
     */
    public void getSystemCache() {
        CacheUtil cacheUtil = new CacheUtil();
        String weatherSting = cacheUtil.getCache(this,"weather");
        final String weatherId;
        if (weatherSting != null){
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherSting);
            weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
            this.weatherId = weatherId;
            LogUtil.i("WeatherActivity","读取了缓存时的weatherId 是 "+weatherId);
        }else {
            //无缓存时直接去服务器查询天气
            weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
            this.weatherId = weatherId;
            LogUtil.i("WeatherActivity","直接去服务器时的weatherId 是 "+this.weatherId);
        }
        String bingPic = cacheUtil.getCache(this , "bing_pic");
        if (bingPic != null){
            //有缓存直接读取背景
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            //无缓存直接去服务器加载
            loadBingPic();
        }
    }

    /**
     * 设置状态栏
     */
    private void setStatusBarVisibility() {
        if (Build.VERSION.SDK_INT >= 21 ){
            View decorView = getWindow().getDecorView();
            //活动的布局显示在状态栏上
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            |View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            //将状态栏设置成透明的
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        LogUtil.i("WeatherActivity","天气状态栏已设置为透明!");
    }

    /**
     * 初始化各控件
     */
    private void initComponent() {
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
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);//设置下拉进度条的颜色
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        weatherTouch = (LinearLayout)findViewById(R.id.weather_touch);
        navButton = (Button)findViewById(R.id.nav_button);//导航按钮，弹出侧面菜单
        navButton.setOnClickListener(view -> drawerLayout.openDrawer(GravityCompat.START));
        LogUtil.i("WeatherActivity","各组件已经初始化！");
    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";//必应每日一图接口地址
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic" , bingPic);
                editor.apply();
                runOnUiThread(() -> Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg));
            }
        });
    }

    /**
     * 根据城市id请求城市天气信息
     * @param weatherId
     */
    public void requestWeather(String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid="+weatherId
                +"&key=9bff2107c1bd4be8b5ba7224ea82d82f";
        Log.d("WeatherActivity","天气活动的weatherUrl是 "+weatherUrl+"\n天气活动的weatherId是 "+weatherId);
        this.weatherId = weatherId;
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(WeatherActivity.this ,
                            "获取天气信息失败" , Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(() -> {
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
                    swipeRefreshLayout.setRefreshing(false);
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
        if (weather != null && "ok".equals(weather.status)) {
            String cityName = weather.basic.cityName;
            String updateTime = weather.basic.update.updateTime.split(" ")[1];
            String degree = weather.now.temperature + "°C";
            String weatherInfo = weather.now.more.info;
            titleCity.setText(cityName);
            titleUpdateTime.setText(updateTime);
            degreeText.setText(degree);
            weatherInfoText.setText(weatherInfo);
            forecastLayout.removeAllViews();
            for (Forecast forecast : weather.forecastList) {
                View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,
                        forecastLayout, false);
                TextView dateText = view.findViewById(R.id.date_text);
                TextView infoText = view.findViewById(R.id.info_text);
                TextView maxText = view.findViewById(R.id.max_text);
                TextView minText = view.findViewById(R.id.min_text);
                dateText.setText(forecast.date);
                infoText.setText(forecast.more.info);
                maxText.setText(forecast.temperature.max+"°C");
                minText.setText(forecast.temperature.min+"°C");
                forecastLayout.addView(view);
            }
            if (weather.aqi != null) {
                aqiText.setText(weather.aqi.city.aqi);
                pm25Text.setText(weather.aqi.city.pm25);
            }
            String comfort = "舒适度: " + weather.suggestion.comfort.info;
            String carWash = "洗车指数: " + weather.suggestion.carWash.info;
            String sport = "运动建议: " + weather.suggestion.sport.info;
            comfortText.setText(comfort);
            carWashText.setText(carWash);
            sportText.setText(sport);
            weatherLayout.setVisibility(View.VISIBLE);//请求数据时应该隐藏ScrollView，不然空数据界面不好看。
            //启动后台更新服务
            //            Intent intent = new Intent(this , AutoUpdateService.class);
            //            startService(intent);
            //            LogUtil.i("WeatherActivity","开启后台更新服务!");
        }else {
            Toast.makeText(WeatherActivity.this , "获取天气信息失败！" , Toast.LENGTH_SHORT).show();
        }
    }
}
