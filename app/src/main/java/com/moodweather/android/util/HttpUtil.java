package com.moodweather.android.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by THOMASLEWIS on 2017/10/30.
 */

public class HttpUtil {
    /**
     *OkHttp请求
     * @param address  请求地址
     * @param callback  回调响应
     */
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request
                .Builder()
                .url(address)
                .build();
        client.newCall(request).enqueue(callback);
    }
}
