package com.moodweather.android.util;

import static android.content.ContentValues.TAG;

/**
 * Created by THOMASLEWIS on 2017/11/5.
 */

public class AssistUtil {
    private boolean b = true;
    public boolean Judge(){
        if (b){
            LogUtil.i(TAG,"Judge()返回真");
            this.b = false;
            return true;
        }else {
            LogUtil.i(TAG,"Judge()返回假");
            return false;
        }
    }
}
