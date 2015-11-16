package com.stone.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.util.Log;

import com.stone.database.DBManager;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by stoneami on 2015/10/3.
 */
public class Util {
    public static final String TAG = "Util";
    public static final boolean DEBUG = true;

    private Context mContext;
    private static Util mInstance = null;

    private Util(Context context){
        mContext = context;
    }

    public static Util getInstance(Context context){
        if(mInstance == null){
            mInstance = new Util(context);
        }

        return mInstance;
    }

    public void performHOME(){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        mContext.startActivity(intent);
    }

    public boolean launchNotificationPkg(String pkg, PendingIntent pendingIntent){
        if (pendingIntent != null) {
            try {
                    Log.v("Util", "launchNotificationPkg(pendingIntent) -> launch PendingIntent: " + pendingIntent.getCreatorPackage());

                mContext.startIntentSender(pendingIntent.getIntentSender(), null,
                        Intent.FLAG_ACTIVITY_NEW_TASK,
                        Intent.FLAG_ACTIVITY_NEW_TASK, 0, null);
            } catch (IntentSender.SendIntentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }else {
            if(DEBUG) {
                Log.i(TAG, "launchNotificationPkg -> " + pkg);
            }

            PackageManager pm = mContext.getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(pkg);

            if (intent != null) {
                try {
                    mContext.startActivity(intent);
                } catch (android.content.ActivityNotFoundException e0) {
                    // TODO Auto-generated catch block
                    e0.printStackTrace();
                }

                return true;
            }
        }

        return false;
    }

    public String getCurrentDatetime(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(new Date(System.currentTimeMillis()));
    }
}
