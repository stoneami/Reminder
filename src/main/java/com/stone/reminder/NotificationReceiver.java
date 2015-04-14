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
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.os.Vibrator;
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

//    private Animation mSlideLeftHideAnimation;
//    private Animation mSlideRightShowAnimation;

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

        sDefaultVibrateTime = getResources().getInteger(R.integer.default_vibrate_time);
        mNeedVibrator = getResources().getBoolean(R.bool.need_vibrator);

//        mSlideLeftHideAnimation = AnimationUtils.loadAnimation(this, R.anim.out_to_left);
//        mSlideRightShowAnimation = AnimationUtils.loadAnimation(this, R.anim.in_from_left);
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
        registerReceiver(mNotificationChangedReceiver, myFilter);

        mShow = false;
        createFloatView();
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
        mWinParams = new WindowManager.LayoutParams();

        //获取的是WindowManagerImpl.CompatModeWrapper
        mWindowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);

        mWinParams.type = LayoutParams.TYPE_PHONE;

        //设置图片格式，效果为背景透明
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

    public void showFloatView(){
        if(DEBUG) {
            Log.i(TAG, "showFloatView(): mShow=" + mShow);
        }

        if(!mShow) {
            mShow = true;
            mWindowManager.addView(mFloatLayout, mWinParams);

//            mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
//                    View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
//                    .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
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
            ((ImageView)mFloatLayout.findViewById(R.id.icon)).setImageDrawable(drawable);
        }else{
            imageView.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
            ((TextView)mFloatLayout.findViewById(R.id.amount)).setText(String.valueOf(mMessageAmount));
        }
    }

    public void enterDrag(){
        if(DEBUG) {
            Log.i(TAG, "enterDrag() +++");
        }

        //setDragBgOfFloatView();
    }

    public void quitDrag(){
        if(DEBUG) {
            Log.i(TAG, "quitDrag() ---");
        }

        //setNormalBgOfFloatView();
    }

    private void setNormalBgOfFloatView(){
        if(mMessageAmount > 3){
//            mFloatLayout.setBackgroundResource(R.drawable.red_background);
            mContainer.setBackgroundResource(R.drawable.red_background);

        }else{
//            mFloatLayout.setBackgroundResource(R.drawable.green_background);
            mContainer.setBackgroundResource(R.drawable.green_background);
        }
    }

    private void setDragBgOfFloatView(){
//        mFloatLayout.setBackgroundResource(R.drawable.yellow_background);
        mContainer.setBackgroundResource(R.drawable.yellow_background);

    }

    public void handleTouchDownEvent(MotionEvent event){
        setTouchDonwBgOfFloatView();
    }

    public void handleTouchUpEvent(MotionEvent event){
        setNormalBgOfFloatView();
    }

    private void setTouchDonwBgOfFloatView(){
//        mFloatLayout.setBackgroundResource(R.drawable.yellow_background);
        mContainer.setBackgroundResource(R.drawable.yellow_background);
    }

    public void moveFloatView(int x, int y){
        mWinParams.x = x - sWidth / 2;
        mWinParams.y = y - sHeight / 2;

        mWindowManager.updateViewLayout(mFloatLayout, mWinParams);
    }

    public void slideToLeftHide(){
        if(mShow){
            if(DEBUG) Log.i(TAG,"slideToLeftHide()");

//            mContainer.startAnimation(mSlideLeftHideAnimation);
        }
    }

    public void slideToRightShow(){
        if(mShow){
            if(DEBUG) Log.i(TAG,"slideToRightShow()");
//            mContainer.startAnimation(mSlideRightShowAnimation);
        }
    }

    //
    // launch APP
    //
    public boolean hasNotification(){
        return mNotificationPkg != null && ! mNotificationPkg.isEmpty();
    }

    private Vibrator mVibrator;
    private long sDefaultVibrateTime = 25;
    private boolean mNeedVibrator;
    public void doVibrate(){
        if(! mNeedVibrator) return;

        if(mVibrator == null) {
            mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }

        mVibrator.vibrate(sDefaultVibrateTime);
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
            mNotificationPkg = intent.getStringExtra(NotificationListener.PACKAGE);
            mPendingIntent = intent.getParcelableExtra(NotificationListener.PENDING_INTENT);
            mMessageAmount = intent.getIntExtra(NotificationListener.AMOUNT, 0);
            mIcon = intent.getIntExtra(NotificationListener.ICON, -1);

            if(mNotificationPkg.isEmpty()){
                hideFloatView();
            }else{
                showFloatView();
            }
        }
    };
}
