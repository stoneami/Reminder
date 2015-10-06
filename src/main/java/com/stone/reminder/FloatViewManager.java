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
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.IBinder;

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

import com.stone.utils.PreferenceUtil;

public class FloatViewManager extends Service {
    private static final String TAG = "FloatViewManager";
    private static final boolean DEBUG = true;

    private FloatViewRoot mFloatLayout;
    private LinearLayout mContainer;
    private WindowManager.LayoutParams mWinParams;
    private WindowManager mWindowManager;

    public FloatViewManager() {
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

        iniDisplayMetrics();
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

        if(PreferenceUtil.getInstance(FloatViewManager.this).permitFloatView()) {
            showDefaultFloatView();
        }
    }

    private void reCalculatePos() {
        int oldHeight = mHeight;
        int oldWidth = mWidth;
        int oldX = mWinParams.x;
        int oldY = mWinParams.y;
        int oldWinWidth = mWinParams.width;

        iniDisplayMetrics();
        int x = 0;
        if(oldX == 0){
            x = 0;
        }else if(oldX == oldWidth-oldWinWidth){
            x = mWidth - mWinParams.width;
        }else{
            x = (int)((float)mWidth*oldX/oldWidth);
        }

        int y = adjustYPosition((int)((float)mHeight*oldY/oldHeight));

        moveFloatView(x,y);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.i(TAG, "onConfigurationChanged()");

        reCalculatePos();

        super.onConfigurationChanged(newConfig);
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

        mFloatLayout.setFloatViewManager(this);
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

        imageView.setImageDrawable(getDrawable(R.drawable.home_icon));
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

    private void setNormalBgOfFloatView(){
        if(mMessageAmount > 3){
            mContainer.setBackgroundResource(R.drawable.red_background);
        }else{
            mContainer.setBackgroundResource(R.drawable.green_background);
        }
    }

    //
    // Handle touch event
    //
    private boolean mLongPress = false;
    public void handleLongPressEvent(MotionEvent event){
        mLongPress = true;
        mContainer.setBackgroundResource(R.drawable.red_background);
    }

    private int mLastX = 0;
    private int mLastY = 0;

    public void handleTouchDownEvent(MotionEvent event){
        setTouchDonwBgOfFloatView();

        mLastX = (int)event.getRawX();
        mLastY = (int)event.getRawY();
    }

    public void handleTouchUpEvent(MotionEvent event){
        //iniDisplayMetrics();
        setNormalBgOfFloatView();

        if(mLongPress) {
            int dx = (int) event.getRawX() - mLastX;
            int dy = (int) event.getRawY() - mLastY;

            moveFloatView(adjustXPosition(mWinParams.x + dx, DisplayArea.UNCERTAIN), adjustYPosition(mWinParams.y + dy));

            mLastX = 0;
            mLastY = 0;

            mLongPress = false;
        }
    }

    public void handleTouchMoveEvent(MotionEvent event){
        if(!mLongPress) return;

        int dx = (int)event.getRawX() - mLastX;
        int dy = (int)event.getRawY() - mLastY;

        int x = mWinParams.x + dx;
        int y = mWinParams.y + dy;
        if(x < 0){
            x=0;
        }else if(x > mWidth){
            x=mWidth-mWinParams.width;
        }

        moveFloatView(x, y);

        Log.i(TAG, "handleTouchMoveEvent(): mWinParams.x=" + mWinParams.x + " mWinParams.y=" + mWinParams.y + " dx=" + dx + " dy=" + dy);

        mLastX = (int)event.getRawX();
        mLastY = (int)event.getRawY();
    }

    private int mWidth = 1080;
    private int mHeight = 1080;
    private float mMinHeightRate = 0.2f;
    private float mMaxHeightRate = 0.7f;
    private int mMinHeight = 0;
    private int mMaxHeight = 1080;

    private float mLeftAreaRate = 0.25f;
    private float mRightAreaRate = 0.6f;
    private int mLeftAreaX = (int)(mWidth * mLeftAreaRate);
    private int mRightAreaX = (int)(mWidth * mRightAreaRate);

    private void iniDisplayMetrics(){
        mWidth = getResources().getDisplayMetrics().widthPixels;
        mHeight = getResources().getDisplayMetrics().heightPixels;

        mMinHeight = (int)(mHeight * mMinHeightRate);
        mMaxHeight = (int)(mHeight * mMaxHeightRate);

        mLeftAreaX = (int)(mWidth * mLeftAreaRate);
        mRightAreaX = (int)(mWidth * mRightAreaRate);

        Log.i(TAG, "iniDisplayMetrics(): mWidth=" + mWidth + " mHeight=" + mHeight);
    }

    private int adjustXPosition(int x, DisplayArea area){
        int ret = x;

        if(area == DisplayArea.LEFT){
            ret = 0;
        }else if(area == DisplayArea.RIGHT){
            ret = mWidth - mWinParams.width;
        }else{
            if(x - mLeftAreaX <= 0){//left
                ret = 0;
            }else if(x - mRightAreaX >= 0){//right
                ret = mWidth - mWinParams.width;
            }
        }

        return ret;
    }

    private int adjustYPosition(int y){
        int ret = y;

        if(y < mMinHeight){//up
            ret = mMinHeight;
        }else if(y > mMaxHeight){//down
            ret = mMaxHeight;
        }

        return ret;
    }

    private void setTouchDonwBgOfFloatView(){
        mContainer.setBackgroundResource(R.drawable.blue_background);
    }

    public void moveFloatView(int x, int y){
        mWinParams.x = x;
        mWinParams.y = y;

        mWindowManager.updateViewLayout(mFloatLayout, mWinParams);
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
                   if(PreferenceUtil.getInstance(FloatViewManager.this).showDefaultFloatView()) {
                       if(DEBUG) {
                           Log.i(TAG, "mNotificationChangedReceiver.onReceive(): showDefaultFloatView()");
                       }
                       showDefaultFloatView();
                   }else{
                       if(DEBUG) {
                           Log.i(TAG, "mNotificationChangedReceiver.onReceive(): hideFloatView()");
                       }
                       hideFloatView();
                   }
               } else {
                   if(DEBUG) {
                       Log.i(TAG, "mNotificationChangedReceiver.onReceive(): showFloatView()");
                   }
                   showFloatView();
               }
           }
        }
    };

    private enum DisplayArea{
        LEFT,RIGHT,UNCERTAIN
    }
}
