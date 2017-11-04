package com.moodweather.android.ndk;

/**
 * Created by THOMASLEWIS on 2017/11/1.
 */

public class NDK {
    // 1.加载实现了native函数的动态库，只需要写动态库的名字
    static {
        //System.loadLibrary(“MyJni”);加载库，需要注意的是加载的库名即编译生成的库名，去掉前缀lib和后缀so。
        System.loadLibrary("MyJni");
    }

    // 2.声明这是一个native函数，由本地代码实现
    public static native String getStringFromNative();//本地方法
    public static native String getString_From_c();
}
