package com.stone.reminder;

import android.content.Intent;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;

/**
 * Created by 80048914 on 2015/2/27.
 */
public class FloatViewRoot extends LinearLayout {
    private static final String TAG = "FloatViewRoot";
    private static final boolean DEBUG = true;

    private NotificationReceiver mService;

    private int mWidth = 1080;
    private int mHeight = 1080;
    private float mMinHeightRate = 0.3f;
    private float mMaxHeightRate = 0.7f;
    private int mMinHeight = 0;
    private int mMaxHeight = 1080;

    private void init(Context context){
        mDetector = new GestureDetector(context, mListener);
        mDetector.setOnDoubleTapListener(mDoubleTapListener);

        iniDisplayMetrics();
    }

    private void iniDisplayMetrics(){
        mWidth = getResources().getDisplayMetrics().widthPixels;
        mHeight = getResources().getDisplayMetrics().heightPixels;

        mMinHeight = (int)(mHeight * mMinHeightRate);
        mMaxHeight = (int)(mHeight * mMaxHeightRate);
    }

    public FloatViewRoot(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init(context);
    }

    public FloatViewRoot(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mService.handleTouchDownEvent(event);
                break;
            case MotionEvent.ACTION_MOVE:
                handleActionMove((int) event.getRawX(), (int) event.getRawY());
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mLongPress) {
                    mService.quitDrag();
                }

                handleActionUp((int) event.getRawX(), (int) event.getRawY());
                mService.handleTouchUpEvent(event);

                mLongPress = false;

                break;
            default:
                break;
        }

        return false;
    }

    private void handleActionMove(int x, int y) {
        move(x, y);
    }

    private void handleActionUp(int x, int y){
        iniDisplayMetrics();

        move(adjustXPosition(x, DisplayArea.UNCERTAIN), adjustYPosition(y));
    }

    private void move(int x, int y){
        if (DEBUG) Log.i(TAG,"handleActionMove(): (" + x + "," + y + ")");

        if (mLongPress) {
            mService.moveFloatView(x, y);
        }
    }

    private int adjustXPosition(int x, DisplayArea area){
        int ret = x;

        if(area == DisplayArea.LEFT){
            ret = 0;
        }else if(area == DisplayArea.RIGHT){
            ret = mWidth;
        }else{
            if(x - mWidth /2 < 0){//left
                ret = 0;
            }else{//right
                ret = mWidth;
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

    private void performHOME(){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        mService.startActivity(intent);
    }

    public void setService(NotificationReceiver service) {
        mService = service;
    }

    private GestureDetector mDetector;
    private GestureDetector.OnGestureListener mListener = new DefaultGestureListener();
    private OnDoubleTapListener mDoubleTapListener = new OnDoubleTapListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if(DEBUG){
                Log.i(TAG,"onSingleTapConfirmed()");
            }

            performHOME();
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if(DEBUG){
                Log.i(TAG,"onDoubleTap()");
            }

            mService.launchNotificationPkg();
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }
    };

    private boolean mLongPress = false;

    private class DefaultGestureListener implements GestureDetector.OnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }


        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            mLongPress = true;
            mService.enterDrag();
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(DEBUG) {
                Log.i(TAG, "onFling(): " + velocityX + " | " + velocityY);
            }

            int dY = Math.abs((int) (e2.getY() - e1.getY()));
            int dX = Math.abs((int) (e2.getX() - e1.getX()));

            if(dY - NotificationReceiver.sHeight * 2 / 3 >= 0){
                if(velocityY > 0){
                    if(DEBUG) {
                        Log.i(TAG, "onFling(): MSG_REQUEST_NEXT_NOTIFICATION");
                    }

                    mService.sendBroadcast(new Intent(NotificationListener.MSG_REQUEST_NEXT_NOTIFICATION));
                }else{
                    if(DEBUG) {
                        Log.i(TAG, "onFling(): MSG_REQUEST_PRE_NOTIFICATION");
                    }

                    mService.sendBroadcast(new Intent(NotificationListener.MSG_REQUEST_PRE_NOTIFICATION));
                }
            }else  if (dX - NotificationReceiver.sWidth / 2 >= 0) {
                if(velocityX > 0) {//left to right
                    //mService.hideFloatView();
                    //mService.sendBroadcast(new Intent(NotificationListener.MSG_REMOVE_ALL_NOTIFICATIONS));
                }else{//right to left
                    //mService.sendBroadcast(new Intent(NotificationListener.MSG_REMOVE_CURRENT_NOTIFICATIONS));
                }

            }

            return true;
        }
    }

    private static enum DisplayArea{
        LEFT,RIGHT,UNCERTAIN
    }
}
