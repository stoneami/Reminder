package com.stone.reminder;

import android.content.Intent;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;


import com.stone.reminder.utils.Util;

/**
 * Created by 80048914 on 2015/2/27.
 */
public class FloatViewRoot extends LinearLayout {
    private static final String TAG = "FloatViewRoot";
    private static final boolean DEBUG = true;
    private Context mContext;

    private FloatViewManager mFloatViewManager;

    private void init(Context context){
        mContext = context;
        mDetector = new GestureDetector(context, mListener);
        mDetector.setOnDoubleTapListener(mDoubleTapListener);
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
                mFloatViewManager.handleTouchDownEvent(event);
                break;
            case MotionEvent.ACTION_MOVE:
                mFloatViewManager.handleTouchMoveEvent(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                mFloatViewManager.handleTouchUpEvent(event);
                break;
            default:
                break;
        }
        return false;
    }

    public void setFloatViewManager(FloatViewManager manager) {
        mFloatViewManager = manager;
    }

    private GestureDetector mDetector;
    private GestureDetector.OnGestureListener mListener = new DefaultGestureListener();
    private OnDoubleTapListener mDoubleTapListener = new OnDoubleTapListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if(DEBUG){
                Log.i(TAG,"onSingleTapConfirmed()");
            }

            Util.getInstance(mContext).performHOME();
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if(DEBUG){
                Log.i(TAG,"onDoubleTap()");
            }

            mContext.sendBroadcast(new Intent(NotificationListener.MSG_OPEN_CURRENT_NOTIFICATION));
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }
    };

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
            mFloatViewManager.handleLongPressEvent(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(DEBUG) {
                Log.i(TAG, "onFling(): " + velocityX + " | " + velocityY);
            }

            int dY = Math.abs((int) (e2.getY() - e1.getY()));
            int dX = Math.abs((int) (e2.getX() - e1.getX()));

            if(dY - FloatViewManager.sHeight * 2 / 3 >= 0){
                if(velocityY > 0){
                    if(DEBUG) {
                        Log.i(TAG, "onFling(): MSG_REQUEST_NEXT_NOTIFICATION");
                    }

                    mContext.sendBroadcast(new Intent(NotificationListener.MSG_REQUEST_NEXT_NOTIFICATION));
                }else{
                    if(DEBUG) {
                        Log.i(TAG, "onFling(): MSG_REQUEST_PRE_NOTIFICATION");
                    }

                    mContext.sendBroadcast(new Intent(NotificationListener.MSG_REQUEST_PRE_NOTIFICATION));
                }
            }else  if (dX - FloatViewManager.sWidth / 2 >= 0) {
                //Log.i(TAG, " iniRecentTask() " );
                //iniRecentTask();
                if(velocityX > 0) {//left to right
                    //mFloatViewManager.hideFloatView();
                    //mFloatViewManager.sendBroadcast(new Intent(NotificationListener.MSG_REMOVE_ALL_NOTIFICATIONS));
                }else{//right to left
                    //mFloatViewManager.sendBroadcast(new Intent(NotificationListener.MSG_REMOVE_CURRENT_NOTIFICATIONS));
                }

            }

            return true;
        }
    }
}
