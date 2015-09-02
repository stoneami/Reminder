/*-**********************************************************************
 **Copyright (C), 2008-2015, OPPO Mobile Comm Corp., Ltd VENDOR_EDIT
 * FileName : NotificationReceiver.java
 * Version Number : 1.0
 * Description : this class receive notification change and show (hide) float view
 * Author : LiuLei
 * Date : 2015-02-27
 * History : (ID, Date, Author, Description)
 **
 **********************************************************************/

package com.stone.reminder;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class NotificationReceiver extends Service {
    private static final String TAG = "NotificationReceiver";
    private static final boolean DEBUG = true;

    private FloatViewRoot mFloatLayout;
    private LinearLayout mContainer;
    private WindowManager.LayoutParams mWinParams;
    private WindowManager mWindowManager;

    public NotificationReceiver() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void init(){
        sWidth = getResources().getInteger(R.integer.float_window_width);
        sHeight = getResources().getInteger(R.integer.float_window_height);

        sPosX = getResources().getInteger(R.integer.float_window_pos_x);
        sPosY = getResources().getInteger(R.integer.float_window_pos_y);
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        init();

        if(DEBUG) {
            Log.i(TAG, "onCreate(): mShow=" + mShow);
        }

        IntentFilter myFilter = new IntentFilter(
                NotificationListener.MSG_NOTIFICATION_CHANGED);
        myFilter.addAction(NotificationListener.MSG_AUTO_OPEN_MSG);
        registerReceiver(mNotificationChangedReceiver, myFilter);

        mShow = false;
        createFloatView();

        showDefaultFloatView();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        if(DEBUG) {
            Log.i(TAG, "onDestroy(): mShow=" + mShow);
        }

        if(mFloatLayout != null && mShow) {
            mWindowManager.removeView(mFloatLayout);
        }

        unregisterReceiver(mNotificationChangedReceiver);

        sendBroadcast(new Intent(NotificationListener.MSG_REMOVE_ALL_NOTIFICATIONS));
    }

    //
    //Create, show, hide and move float view
    //
    public static int sWidth = 180;
    public static int sHeight = 180;
    private static int sPosX = 0;
    private static int sPosY = 825;

    private static boolean mShow = false;

    private void createFloatView() {
        mWindowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);

        mWinParams = new WindowManager.LayoutParams();
        mWinParams.type = LayoutParams.TYPE_PHONE;
        mWinParams.format = PixelFormat.RGBA_8888;
        mWinParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE;
        mWinParams.gravity = Gravity.LEFT | Gravity.TOP;
        mWinParams.x = sPosX;
        mWinParams.y = sPosY;
        mWinParams.width = sWidth;
        mWinParams.height = sHeight;

        mFloatLayout = (FloatViewRoot) LayoutInflater.from(getApplication()).inflate(R.layout.float_layout, null);
        mContainer = (LinearLayout)mFloatLayout.findViewById(R.id.container_layout);

        mFloatLayout.setService(this);
    }

    public void hideFloatView(){
        if(DEBUG) {
            Log.i(TAG, "hideFloatView(): mShow=" + mShow);
        }

        if(mShow) {
            mShow = false;
            if(mFloatLayout != null) {
                mWindowManager.removeView(mFloatLayout);
            }
        }
    }

    public void showDefaultFloatView(){
        if(DEBUG) {
            Log.i(TAG, "showDefaultFloatView(): mShow=" + mShow);
        }

        if(!mShow) {
            mShow = true;
            mWindowManager.addView(mFloatLayout, mWinParams);
        }

        setNormalBgOfFloatView();

        TextView textView = (TextView)mFloatLayout.findViewById(R.id.amount);
        ImageView imageView = (ImageView)mFloatLayout.findViewById(R.id.icon);

        imageView.setImageDrawable(getDrawable(R.drawable.upper_h));
        imageView.setVisibility(View.VISIBLE);
        textView.setVisibility(View.GONE);
    }

    public void showFloatView(){
        if(DEBUG) {
            Log.i(TAG, "showFloatView(): mShow=" + mShow);
        }

        if(!mShow) {
            mShow = true;
            mWindowManager.addView(mFloatLayout, mWinParams);
        }

        setNormalBgOfFloatView();

        Context context = null;
        try {
            context = createPackageContext(mNotificationPkg, Context.CONTEXT_IGNORE_SECURITY);
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }

        TextView textView = (TextView)mFloatLayout.findViewById(R.id.amount);
        ImageView imageView = (ImageView)mFloatLayout.findViewById(R.id.icon);

        Drawable drawable = null;
        if(context != null && mIcon != -1) {
            try {
                drawable = context.getResources().getDrawable(mIcon);
            }catch (IllegalArgumentException  e){
                e.printStackTrace();
            }
        }

        if(drawable != null) {
            imageView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
            imageView.setImageDrawable(drawable);
        }else{
            imageView.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
            textView.setText(String.valueOf(mMessageAmount));
        }
    }

    public void enterDrag(){
        if(DEBUG) {
            Log.i(TAG, "enterDrag() +++");
        }
    }

    public void quitDrag(){
        if(DEBUG) {
            Log.i(TAG, "quitDrag() ---");
        }
    }

    private void setNormalBgOfFloatView(){
        if(mMessageAmount > 3){
            mContainer.setBackgroundResource(R.drawable.red_background);

        }else{
            mContainer.setBackgroundResource(R.drawable.green_background);
        }
    }

    public void handleTouchDownEvent(MotionEvent event){
        setTouchDonwBgOfFloatView();
    }

    public void handleTouchUpEvent(MotionEvent event){
        setNormalBgOfFloatView();
    }

    private void setTouchDonwBgOfFloatView(){
        mContainer.setBackgroundResource(R.drawable.yellow_background);
    }

    public void moveFloatView(int x, int y){
        mWinParams.x = x - sWidth / 2;
        mWinParams.y = y - sHeight / 2 - 45;

        mWindowManager.updateViewLayout(mFloatLayout, mWinParams);
    }

    //
    // launch APP
    //
    public boolean hasNotification(){
        return mNotificationPkg != null && ! mNotificationPkg.isEmpty();
    }

    private boolean launchNotificationPkg(PendingIntent pendingIntent){
        if (pendingIntent != null) {
            try {
                if(DEBUG) {
                    Log.v(TAG, "launchNotificationPkg(pendingIntent) -> launch PendingIntent: " + pendingIntent.getCreatorPackage());
                }

                startIntentSender(pendingIntent.getIntentSender(), null,
                        Intent.FLAG_ACTIVITY_NEW_TASK,
                        Intent.FLAG_ACTIVITY_NEW_TASK, 0, null);
            } catch (IntentSender.SendIntentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

        return false;
    }

    public boolean launchNotificationPkg() {
        if (hasNotification()) {
            PendingIntent pendingIntent = mPendingIntent;
            if (pendingIntent != null) {
                try {
                    if(DEBUG) {
                        Log.i(TAG, "launchNotificationPkg -> launch PendingIntent: " + pendingIntent.getCreatorPackage());
                    }

                    startIntentSender(pendingIntent.getIntentSender(), null,
                            Intent.FLAG_ACTIVITY_NEW_TASK,
                            Intent.FLAG_ACTIVITY_NEW_TASK, 0, null);
                } catch (IntentSender.SendIntentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return true;
            } else {
                String pkg = mNotificationPkg;

                if(DEBUG) {
                    Log.i(TAG, "launchNotificationPkg -> " + pkg);
                }

                PackageManager pm = getPackageManager();
                Intent intent = pm.getLaunchIntentForPackage(pkg);

                if (intent != null) {
                    try {
                        startActivity(intent);
                    } catch (android.content.ActivityNotFoundException e0) {
                        // TODO Auto-generated catch block
                        e0.printStackTrace();
                    }

                    return true;
                }
            }
        }

        return false;
    }

    //
    // Receive the notification change event
    //
    private PendingIntent mPendingIntent;
    private String mNotificationPkg;
    private int mMessageAmount;
    private int mIcon;
    private BroadcastReceiver mNotificationChangedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
           if(NotificationListener.MSG_NOTIFICATION_CHANGED.equals(intent.getAction())) {
               mNotificationPkg = intent.getStringExtra(NotificationListener.PACKAGE);
               mPendingIntent = intent.getParcelableExtra(NotificationListener.PENDING_INTENT);
               mMessageAmount = intent.getIntExtra(NotificationListener.AMOUNT, 0);
               mIcon = intent.getIntExtra(NotificationListener.ICON, -1);

               if (mNotificationPkg.isEmpty()) {
                   SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(NotificationReceiver.this);
                   if(sp.getBoolean(MainPreferencFragment.KEY_ALWAYS_SHOW_FLOAT_VIEW, false)) {
                       showDefaultFloatView();
                   }else{
                       hideFloatView();
                   }
               } else {
                   showFloatView();
               }
           }else if(NotificationListener.MSG_AUTO_OPEN_MSG.equals(intent.getAction())){
               launchNotificationPkg((PendingIntent)intent.getParcelableExtra(NotificationListener.PENDING_INTENT));
           }
        }
    };
}
