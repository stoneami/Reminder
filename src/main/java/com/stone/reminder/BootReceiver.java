package com.stone.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by LiuLei on 2015/3/7.
 */
public class BootReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(Intent.ACTION_BOOT_COMPLETED.equals(action)){
            Intent i = new Intent();
            i.setClass(context, FloatViewManager.class);
            context.startService(i);
            Log.i("Reminder", "start service after boot");
        }
    }
}
