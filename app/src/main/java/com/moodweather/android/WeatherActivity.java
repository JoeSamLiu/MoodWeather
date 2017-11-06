package com.moodweather.android;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
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

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.bumptech.glide.Glide;
import com.moodweather.android.gson.Forecast;
import com.moodweather.android.gson.Weather;
import com.moodweather.android.util.AssistUtil;
import com.moodweather.android.util.CacheUtil;
import com.moodweather.android.util.HttpUtil;
import com.moodweather.android.util.LogUtil;
import com.moodweather.android.util.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by THOMASLEWIS on 2017/10/1.
 */

public class WeatherActivity extends BasicActivity implements OnGetGeoCoderResultListener{
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
    private List<String> permissionList = new ArrayList<>();
    public LocationClient locationClient = null;
    private MyLocationClickListener myListener = new MyLocationClickListener();
    //BDAbstractLocationListener为7.2版本新增的Abstract类型的监听接口
    //原有BDLocationListener接口暂时同步保留。具体介绍请参考后文中的说明
    GeoCoder mSearch = null;//可以不需要地图模块，独立使用
    private double latitude;
    private double longitude;
    private TextView locationText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarVisibility();//设置状态栏属性
        setContentView(R.layout.activity_weather);
        initComponent();//初始化各控件
        JudgeIsFirst();//判断是否为第一次启动活动
        swipeUpdate();//下拉刷新
        showMenuFromSide();//弹出菜单
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
        locationText = (TextView)findViewById(R.id.location_data);//显示当前详细地址
        currentLocation();//当前定位信息
        getLocationInit();//初始化获取当前地址的方法
        LogUtil.i("WeatherActivity","各组件已经初始化！");
    }
    /**
     * 判断是否为第一次启动
     */
    private void JudgeIsFirst() {
        if (new AssistUtil().Judge()){
            DoDirectly();//直接请求服务器
        }else {
            getSystemCache();//获取缓存
        }
    }
    /**
     * 根据weatherId直接请求服务器获取数据
     */
    private void DoDirectly() {
        //无缓存时直接去服务器查询天气
        weatherId = getIntent().getStringExtra("weather_id");
        LogUtil.i("WeatherActivity","ChooseAreaFragment传回的的weatherId 是 "+weatherId);
        weatherLayout.setVisibility(View.INVISIBLE);
        requestWeather(weatherId);
        LogUtil.i("WeatherActivity","直接去服务器时的weatherId 是 "+weatherId+
                "\n赋值全局变量weatherId 的值为"+ this.weatherId);
        //无缓存直接去服务器加载
        loadBingPic();
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
     * 初始化获取地址方法
     */
    private void getLocationInit() {
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        locationClient = new LocationClient(getApplicationContext());
        //声明LocationClient类
        locationClient.registerLocationListener(myListener);
        //注册监听函数
        LocationClientOption option = new LocationClientOption();

        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，设置定位模式，默认高精度
        //LocationMode.High_Accuracy：高精度；
        //LocationMode. Battery_Saving：低功耗；
        //LocationMode. Device_Sensors：仅使用设备；

        option.setCoorType("bd09ll");
        //可选，设置返回经纬度坐标类型，默认gcj02
        //gcj02：国测局坐标；
        //bd09ll：百度经纬度坐标；
        //bd09：百度墨卡托坐标；
        //海外地区定位，无需设置坐标类型，统一返回wgs84类型坐标

        option.setScanSpan(1000);
        //可选，设置发起定位请求的间隔，int类型，单位ms
        //如果设置为0，则代表单次定位，即仅定位一次，默认为0
        //如果设置非0，需设置1000ms以上才有效

        option.setOpenGps(true);
        //可选，设置是否使用gps，默认false
        //使用高精度和仅用设备两种定位模式的，参数必须设置为true

        option.setLocationNotify(true);
        //可选，设置是否当GPS有效时按照1S/1次频率输出GPS结果，默认false

        option.setIgnoreKillProcess(false);
        //可选，定位SDK内部是一个service，并放到了独立进程。
        //设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)

        //option.setIgnoreCacheException(false);
        //可选，设置是否收集Crash信息，默认收集，即参数为false

        //option.setWifiValidTime(5*60*1000);
        //可选，7.2版本新增能力
        //如果设置了该接口，首次启动定位时，会先判断当前WiFi是否超出有效期，若超出有效期，会先重新扫描WiFi，然后定位

        option.setEnableSimulateGps(false);
        //可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false

        locationClient.setLocOption(option);
        //mLocationClient为第二步初始化过的LocationClient对象
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        //更多LocationClientOption的配置，请参照类参考中LocationClientOption类的详细说明

        // 初始化搜索模块，注册事件监听
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
        LogUtil.i(TAG,"后台获取详细地址的GetLocationActivity已经启动。"+locationText);
        LogUtil.i(TAG,"后台获取详细地址的GetLocationActivity已经执行完毕。"+locationText);
    }
    /**
     * 发起搜索
     */
    private void DoSearch() {
        LatLng ptCenter = new LatLng((Float.valueOf((float) latitude)), (Float.valueOf((float) longitude)));
        // 反Geo搜索
        mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                .location(ptCenter));
    }
    /**
     * 正向编码
     * @param geoCodeResult
     */
    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
        if (geoCodeResult == null || geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(WeatherActivity.this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
                    .show();
        }
        String strInfo = String.format("纬度：%f 经度：%f",
                geoCodeResult.getLocation().latitude, geoCodeResult.getLocation().longitude);
        Toast.makeText(WeatherActivity.this, strInfo, Toast.LENGTH_LONG).show();
    }
    /**
     * 逆向编码
     * @param reverseGeoCodeResult
     */
    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(WeatherActivity.this, "抱歉，未能找到结果", Toast.LENGTH_LONG)
                    .show();
        }
        LogUtil.d(TAG , "反编码的地址是： "+reverseGeoCodeResult.getAddress());
        locationText.setText(reverseGeoCodeResult.getAddress());//获得定位详细地址信息
    }
    /**
     * 询问权限
     */
    private void askForPermission() {
        if (ActivityCompat.checkSelfPermission(WeatherActivity.this, Manifest
                .permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(WeatherActivity.this, Manifest
                .permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ActivityCompat.checkSelfPermission(WeatherActivity.this, Manifest
                .permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(WeatherActivity.this, permissions, 1);
        } else {
            requestLocation();
        }
    }
    /**
     * 启动定位客户端
     */
    private void requestLocation() {
        locationClient.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationClient.stop();
    }

    /**
     * 获取定位经纬度
     */
    private class MyLocationClickListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location){
            //此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取经纬度相关（常用）的结果信息
            //更多结果信息获取说明，请参照类参考中BDLocation类中的说明

            latitude = location.getLatitude();    //获取纬度信息
            longitude = location.getLongitude();    //获取经度信息
            float radius = location.getRadius();    //获取定位精度，默认值为0.0f

            String coorType = location.getCoorType();
            //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准

            int errorCode = location.getLocType();
            //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
            LogUtil.i(TAG , "经纬度是： "+latitude+","+longitude+"\n"
                    +"定位精度是： "+radius
                    + "\n坐标类型是："+coorType
                    +"\n错误代码是： "+errorCode);
        }
    }
    /**
     * 判断缓存并作出处理
     */
    public void getSystemCache() {
        CacheUtil cacheUtil = new CacheUtil();
        String weatherString = cacheUtil.getCache(WeatherActivity.this,"weather");
        LogUtil.i("WeatherActivity","在WeatherActivity里读取缓存 weatherString 是 "+weatherString);
        final String weatherId;
        if (weatherString != null){
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
            this.weatherId = weatherId;
            LogUtil.i("WeatherActivity","读取了缓存时的weatherId 是 "+weatherId);
        }else {
            //无缓存时直接去服务器查询天气
            weatherId = getIntent().getStringExtra("weather_id");
            LogUtil.i("WeatherActivity","ChooseAreaFragment传回的的weatherId 是 "+weatherId);
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
            this.weatherId = weatherId;
            LogUtil.i("WeatherActivity","直接去服务器时的weatherId 是 "+weatherId+
            "\n赋值全局变量weatherId 的值为"+ this.weatherId);
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
     * 当前定位信息
     */
    private void currentLocation() {
        locationText.setOnClickListener(view -> {
            askForPermission();//询问权限
            DoSearch();//发起地址搜索
        });
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
