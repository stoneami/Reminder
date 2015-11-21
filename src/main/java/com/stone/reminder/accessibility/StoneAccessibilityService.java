package com.stone.reminder.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by 80048914 on 2015/11/21.
 */
public class StoneAccessibilityService extends AccessibilityService {
    public static final String TAG = "JAccessibilityService";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.i(TAG, "onAccessibilityEvent(): " + event.getEventType());
    }

    @Override
    public void onInterrupt() {

    }
}
