package com.moodweather.android.util;

import android.util.Log;

/**
 * Created by THOMASLEWIS on 2017/10/31.
 */

public class LogUtil {
    public static final int VERBOSE = 0;//打印所有的日记
    public static final int DEBUG = 1;//打印所有的Debug（排除故障）日记
    public static final int INFO = 2;//打印所有的信息日记
    public static final int WARNING = 3;//打印所有的警告日记
    public static final int ERROR = 4;//打印所有的错误日记
    public static final int NOTHING = 5;//屏蔽所有的日记
    public static int level = VERBOSE;//设置打印等级
    /**
     * 打印所有的日记
     */
    public static void v(String tag , String msg){
        if (level <= VERBOSE){
            Log.d(tag,msg);
        }
    }
    /**
     * 打印所有的Debug（消除故障）日记
     */
    public static void d(String tag , String msg){
        if (level <= DEBUG){
            Log.d(tag,msg);
        }
    }
    /**
     * 打印所有的信息日记
     */
    public static void i(String tag , String msg){
        if (level <= INFO){
            Log.d(tag,msg);
        }
    }
    /**
     * 打印所有的警告日记
     */
    public static void w(String tag , String msg){
        if (level <= WARNING){
            Log.d(tag,msg);
        }
    }
    /**
     * 打印所有的错误日记
     */
    public static void e(String tag , String msg){
        if (level <= ERROR){
            Log.d(tag,msg);
        }
    }
    /**
     * 屏蔽所有的日记
     */
    public static void n(String tag , String msg){
        if (level <= NOTHING){
            Log.d(tag,msg);
        }
    }
}
