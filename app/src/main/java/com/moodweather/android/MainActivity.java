package com.moodweather.android;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.moodweather.android.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BasicActivity {
    public LocationClient locationClient;
    private List<String> permissionList = new ArrayList<>();
    public static final String TAG = "MainActivity" ;
    private boolean isFirst ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        judgeFirst(isFirst);//判断是否为第一次运行
    }
    /**
     * 判断是否为第一次运行程序
     * @param isFirst
     */
    private void judgeFirst(boolean isFirst) {
        if (!isFirst){
            LogUtil.i(TAG,"第一次执行程序");
            initialization();//初始化
            askForPermission();//询问权限
            this.isFirst = true;
            LogUtil.i(TAG,"第一次执行完程序后，isFirst 的值是 " +isFirst);
        }else {
            LogUtil.i(TAG,"非第一次执行程序");
            initialization();//初始化
            askForPermission();//询问权限
        }
    }
    /**
     * 初始化
     */
    private void initialization() {
        locationClient = new LocationClient(getApplicationContext());
        locationClient.registerLocationListener(new MyLocationClickListener());
    }
    /**
     * 询问权限
     */
    private void askForPermission() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest
                .permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest
                .permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest
                .permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
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
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length > 0){
                    for (int result : grantResults){
                        if (result != PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(MainActivity.this ,
                                    "权限获取不完全无法使用本程序" , Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }else {
                    Toast.makeText(MainActivity.this ,
                            "未知错误" , Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    private class MyLocationClickListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            StringBuilder currentPosition = new StringBuilder();
            currentPosition.append("纬度： ").append(location.getLatitude()).append("\n");
            currentPosition.append("经度： ").append(location.getLongitude()).append("\n");
            currentPosition.append("定位方式： ");
            if (location.getLocType() == BDLocation.TypeGpsLocation){
                currentPosition.append("GPS");
            }else if (location.getLocType() == BDLocation.TypeNetWorkException){
                currentPosition.append("网络");
            }
            LogUtil.i("MainActivity" , "当前位置经纬度： "+currentPosition);
            LogUtil.i("MainActivity" , "location.getLocType() is "+location.getLocType());
            Toast.makeText(MainActivity.this , location.getLocType() ,Toast.LENGTH_SHORT).show();
        }
    }
}
