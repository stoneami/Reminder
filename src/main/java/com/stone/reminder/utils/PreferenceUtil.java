package com.stone.reminder.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.stone.reminder.MainPreferencFragment;

/**
 * Created by 80048914 on 2015/9/26.
 */
public class PreferenceUtil {
    private static PreferenceUtil mInstance;
    private Context mContext;
    private PreferenceUtil(Context context) {
        mContext = context;
    }

    public static PreferenceUtil getInstance(Context context){
        if(mInstance == null) {
            mInstance = new PreferenceUtil(context);
        }
        return mInstance;
    }

    public boolean permitFloatView(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean ret = sp.getBoolean(MainPreferencFragment.KEY_FLOAT_VIEW, true);
        android.util.Log.i("PreferenceUtil","permitFloatView(): " + ret);
        return ret;
    }

    public boolean showDefaultFloatView(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean ret = sp.getBoolean(MainPreferencFragment.KEY_SHOW_DEFAULT_FLOAT_VIEW, true);
        android.util.Log.i("PreferenceUtil","showDefaultFloatView(): " + ret);
        return ret;
    }

    public boolean openMsgOnlyHOME(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sp.getBoolean(MainPreferencFragment.KEY_AUTO_OPEN_MSG_ONLY_AT_HOME, false);
    }

    public boolean openMsgEverywhere(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sp.getBoolean(MainPreferencFragment.KEY_AUTO_OPEN_EVERYWHERE, false);
    }

    public boolean recordOngoingMsg(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sp.getBoolean(MainPreferencFragment.KEY_RECORD_ONGOING_MSG, true);
    }

    public boolean smartOpenApp(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sp.getBoolean(MainPreferencFragment.KEY_SMART_OPEN_APP, true);
    }

    public boolean displayOftenOpenIcon(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sp.getBoolean(MainPreferencFragment.KEY_DISPLAY_OFTEN_OPEN_ICON, true);
    }
}
