/*-**********************************************************************
 **Copyright (C), 2008-2015, OPPO Mobile Comm Corp., Ltd VENDOR_EDIT
 * FileName : NotificationListener.java
 * Version Number : 1.0
 * Description : listen the notification changes
 * Author : LiuLei
 * Date : 2015-02-27
 * History : (ID, Date, Author, Description)
 **
 **********************************************************************/

package com.stone.reminder;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import android.content.pm.ResolveInfo;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;

import com.stone.database.DBManager;
import com.stone.utils.PreferenceUtil;
import com.stone.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class NotificationListener extends NotificationListenerService {
    private static final String TAG = "NotificationListener";

    private static final boolean DEBUG = true;

    private ActivityManager mActivityManager;
    private PackageManager mPackageManager;

    public static final String MSG_NOTIFICATION_CHANGED = "msg.j.notification.changed";
    public static final String MSG_REMOVE_ALL_NOTIFICATIONS = "msg.j.remove.all.notifications";
    public static final String MSG_REQUEST_NEXT_NOTIFICATION = "msg.j.request.next.notification";
    public static final String MSG_REQUEST_PRE_NOTIFICATION = "msg.j.request.pre.notification";
    public static final String MSG_REMOVE_CURRENT_NOTIFICATIONS = "msg.j.remove.current.notification";
    public static final String MSG_LOAD_NOTIFICATIONS = "msg.j.load.notifications";
    public static final String MSG_RELOAD_NOTIFICATIONS = "msg.j.reload.notifications";
    public static final String MSG_ALWAYS_SHOW_FLOAT_VIEW = "msg.j.always.show.float.view";

    public static final String MSG_OPEN_CURRENT_NOTIFICATION = "msg.j.open.current.notification";

    public static final String PACKAGE = "pkg";
    public static final String PENDING_INTENT = "pending-intent";
    public static final String AMOUNT = "message-amount";
    public static final String ICON = "message-icon";

    private static final int ASYNC_DELAY = 600;//ms

    private Handler mHandler = new Handler();

    private ArrayList<NotificationListenerItem> mPkgList = new ArrayList<NotificationListenerItem>(
            5);
    private ArrayList<String> mAutoOpenPackage = new ArrayList<String>();

    private final ArrayList<String> mIgnoredPackage = new ArrayList<String>();

    private void init() {
        String[] list = getResources().getStringArray(R.array.ignored_packages);
        for (int i = 0; i < list.length; i++) {
            mIgnoredPackage.add(list[i]);
        }

        list = getResources().getStringArray(R.array.auto_open_packages);
        for (int i = 0; i < list.length; i++) {
            mAutoOpenPackage.add(list[i]);
        }

        mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        mPackageManager = getPackageManager();
    }

    private void asyncLoadActiveNotification() {
        if (DEBUG) Log.i(TAG, "asyncLoadActiveNotification()");

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadActiveNotifications();
            }
        }, ASYNC_DELAY);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction();
            if (MSG_REMOVE_ALL_NOTIFICATIONS.equals(action)) {

                if (DEBUG) Log.i(TAG, "onReceive(): clear all notifications !");

                mPkgList.clear();

                notifyNotificationChanged(null);
            } else if (MSG_REQUEST_NEXT_NOTIFICATION.equals(action)) {
                int index = findNextHighPriorityItem(false);

                if (DEBUG) Log.i(TAG, "MSG_REQUEST_NEXT_NOTIFICATION: index=" + index);

                if (-1 != index) {
                    NotificationListenerItem item = mPkgList.get(index);
                    if (!mCurPkg.equals(item.mPkg)) {
                        notifyNotificationChanged(item);
                    }
                } else {
                    notifyNotificationChanged(null);
                }
            } else if (MSG_REQUEST_PRE_NOTIFICATION.equals(action)) {
                int index = findPreHighPriorityItem(false);

                if (DEBUG) Log.i(TAG, "MSG_REQUEST_PRE_NOTIFICATION: index=" + index);

                if (-1 != index) {
                    NotificationListenerItem item = mPkgList.get(index);
                    if (!mCurPkg.equals(item.mPkg)) {
                        notifyNotificationChanged(mPkgList.get(index));
                    }
                } else {
                    notifyNotificationChanged(null);
                }
            } else if (MSG_REMOVE_CURRENT_NOTIFICATIONS.equals(action)) {
                if (DEBUG) Log.i(TAG, "MSG_REMOVE_CURRENT_NOTIFICATIONS");
                if (mPkgList.size() > 1) {
                    String prePkg = mCurPkg;
                    mCurPkg = mPkgList.get(findNextHighPriorityItem(true)).mPkg;
                    mPkgList.remove(getItemIndex(prePkg));

                    NotificationListenerItem item = mPkgList.get(getItemIndex(mCurPkg));
                    notifyNotificationChanged(item);
                }  else {
                    mPkgList.clear();
                    notifyNotificationChanged(null);
                }
            } else if (MSG_LOAD_NOTIFICATIONS.equals(action)) {
                asyncLoadActiveNotification();
            } else if (MSG_ALWAYS_SHOW_FLOAT_VIEW.equals(action)) {
                if (DEBUG) Log.i(TAG, "MSG_ALWAYS_SHOW_FLOAT_VIEW");
                if (mPkgList.isEmpty()) {
                    if (DEBUG) Log.i(TAG, "notifyNotificationChanged(null)");
                    notifyNotificationChanged(null);
                }
            }else if(MSG_RELOAD_NOTIFICATIONS.equals(action)){
                notifyNotificationChanged(null);
                reloadActiveNotifications();
            }else if(MSG_OPEN_CURRENT_NOTIFICATION.equals(action)){
                // TODO: 2015/10/4
                if(!mCurPkg.isEmpty()){
                    int idx = getItemIndex(mCurPkg);
                    if(idx != -1){
                        Util.getInstance(NotificationListener.this).launchNotificationPkg(mCurPkg, mPkgList.get(idx).mPendingIntent);

                        String datetime = Util.getInstance(NotificationListener.this).getCurrentDatetime();
                        Log.i(TAG,"package: " + mCurPkg + " datetime: " + datetime);
                        DBManager.getInstance(NotificationListener.this).insert(mCurPkg, datetime);
                    }
                }else{
                    if(PreferenceUtil.getInstance(NotificationListener.this).smartOpenApp()) {
                        String pkg = DBManager.getInstance(NotificationListener.this).getMostPopularApp(6);
                        if (pkg != null) {
                            Util.getInstance(NotificationListener.this).launchNotificationPkg(pkg, null);
                        }
                    }
                }
            }
        }
    };

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        if (DEBUG) Log.i(TAG, "onCreate()");

        init();

        IntentFilter filter = new IntentFilter(MSG_REMOVE_ALL_NOTIFICATIONS);
        filter.addAction(MSG_REQUEST_NEXT_NOTIFICATION);
        filter.addAction(MSG_REQUEST_PRE_NOTIFICATION);
        filter.addAction(MSG_REMOVE_CURRENT_NOTIFICATIONS);
        filter.addAction(MSG_LOAD_NOTIFICATIONS);
        filter.addAction(MSG_ALWAYS_SHOW_FLOAT_VIEW);
        filter.addAction(MSG_RELOAD_NOTIFICATIONS);
        filter.addAction(MSG_OPEN_CURRENT_NOTIFICATION);
        registerReceiver(mReceiver, filter);
    }

    private boolean mFirstTimeAfterBind = false;

    @Override
    public IBinder onBind(Intent intent) {
        if (DEBUG) Log.i(TAG, "onBind()");

        if (mPkgList.isEmpty()) {
            if (DEBUG) Log.i(TAG, "onBind()");
            asyncLoadActiveNotification();
        }

        mFirstTimeAfterBind = true;
        return super.onBind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mFirstTimeAfterBind = false;
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        if (DEBUG) Log.i(TAG, "onDestroy()");

        notifyNotificationChanged(null);

        unregisterReceiver(mReceiver);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        // TODO Auto-generated method stub
        if (DEBUG) Log.i(TAG, "+++ onNotificationPosted(): " + sbn.getPackageName());

        if (mFirstTimeAfterBind && mPkgList.size() == 0) {
            loadActiveNotifications();
            mFirstTimeAfterBind = false;
        } else {
            if (shouldAutoOpenMsg(sbn) && shouldAddPackage(sbn)) {
                if (DEBUG) Log.i(TAG, "auto open message: " + sbn.getPackageName());
                Util.getInstance(this).launchNotificationPkg(sbn.getPackageName(), sbn.getNotification().contentIntent);
            } else {
                checkIfNeedAddNotification(sbn);
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // TODO Auto-generated method stub
        if (DEBUG) Log.i(TAG, "--- onNotificationRemoved(): " + sbn.getPackageName());

        if (mPkgList.size() == 0) {
            loadActiveNotifications();
        }

        checkIfNeedRemoveNotification(sbn);
    }

    private void reloadActiveNotifications() {
        mPkgList.clear();
        loadActiveNotifications();
    }

    private void loadActiveNotifications() {
        Log.i(TAG, "begin to load active notifications");

        StatusBarNotification[] sbns = null;

        try {
            sbns = getActiveNotifications();
        } catch (Exception e) {
            Log.w(TAG, "loadActiveNotifications(): something wrong with getActiveNotifications() !");
            e.printStackTrace();
        }

        if (sbns == null) return;

        int len = sbns.length;

        if (len > 0) {
            ArrayList<NotificationListenerItem> canClearList = new ArrayList<NotificationListenerItem>();
            ArrayList<NotificationListenerItem> noClearList = new ArrayList<NotificationListenerItem>();

            for (int i = 0; i < len; i++) {
                StatusBarNotification sbn = sbns[i];
                if (shouldAddPackage(sbn)) {
                    String pkg = sbn.getPackageName();
                    Notification notice = sbn.getNotification();
                    PendingIntent pendingIntent = notice.contentIntent;

                    boolean flagNoClear = (notice.flags & Notification.FLAG_NO_CLEAR) != 0;
                    boolean flagOnGoing = (notice.flags & Notification.FLAG_ONGOING_EVENT) != 0;
                    boolean canCancel = !flagNoClear && !flagOnGoing;

                    if (mPkgList.size() > 0) {
                        if (canCancel) {
                            mPkgList.add(new NotificationListenerItem(pkg, notice.flags, pendingIntent, notice.icon));
                        } else {
                            ArrayList<NotificationListenerItem> temp = mPkgList;
                            mPkgList = new ArrayList<>(temp.size() + 1);
                            mPkgList.add(new NotificationListenerItem(pkg, notice.flags, pendingIntent, notice.icon));
                            for (NotificationListenerItem t : temp) {
                                mPkgList.add(t);
                            }
                        }
                    } else {
                        mPkgList.add(new NotificationListenerItem(pkg, notice.flags, pendingIntent, notice.icon));
                    }

                    if (DEBUG)
                        Log.i(TAG, "loadActiveNotifications(): found active notification -> " + pkg);
                }
            }

            if (mPkgList.size() > 0) {
                int index = findHighPriorityItem();
                notifyNotificationChanged(mPkgList.get(index));
            } else {
                if (DEBUG)
                    Log.i(TAG, "loadActiveNotifications(): no active notification, show default float view!");
                //show the default float view
                notifyNotificationChanged(null);
            }
        }
    }

    private void checkIfNeedAddNotification(StatusBarNotification sbn) {
        String pkg = sbn.getPackageName();
        Notification notice = sbn.getNotification();
        PendingIntent pendingIntent = notice.contentIntent;
        int icon = notice.icon;

        int flags = notice.flags;
        if (shouldAddPackage(sbn)) {
            mPkgList.add(new NotificationListenerItem(pkg, flags, pendingIntent, icon));

            int index = findHighPriorityItem();
            notifyNotificationChanged(mPkgList.get(index));

            if (DEBUG) Log.v(TAG, "new notification is posted by " + pkg + " |flags="
                    + flags + "|curPkg: " + mPkgList.get(index).mPkg);
        } else {
            if (DEBUG) Log.v(TAG, "ignore " + pkg);
        }
    }

    private boolean shouldAddPackage(StatusBarNotification sbn) {
        String pkg = sbn.getPackageName();
        Notification notice = sbn.getNotification();
        PendingIntent pendingIntent = notice.contentIntent;

        boolean flagNoClear = (notice.flags & Notification.FLAG_NO_CLEAR) != 0;
        boolean flagOnGoing = (notice.flags & Notification.FLAG_ONGOING_EVENT) != 0;
        boolean canCancel = !flagNoClear && !flagOnGoing;
        boolean recOngoingMsg = PreferenceUtil.getInstance(this).recordOngoingMsg();

        boolean newPkg = (getItemIndex(pkg) == -1);
        boolean permittedPkg = !mIgnoredPackage.contains(pkg);
        boolean hasLaunchIntent = hasLaunchIntentForPackage(pkg);

        if (DEBUG) {
            boolean allowed = (canCancel || recOngoingMsg) && newPkg && permittedPkg && (pendingIntent != null || hasLaunchIntent);
            Log.i(TAG, "shouldAddPackage(): " + allowed + " pkg=" + pkg + " recOngoingMsg=" + recOngoingMsg + " flags=" + notice.flags + " newPkg=" + newPkg + " permittedPkg=" + permittedPkg + " hasLaunchIntent=" + hasLaunchIntent + " pendingIntent=" + pendingIntent);
        }

        /**
         * Conditions:
         * 1.Notification can be canceled
         * 2.Notification has not been added yet
         * 3.Notification is permitted APP
         * 4.Notification has Pending-Intent or launch-Intent
         */
        return (canCancel || recOngoingMsg) && newPkg && permittedPkg && (pendingIntent != null || hasLaunchIntent);
    }

    private boolean hasLaunchIntentForPackage(String pkg) {
        PackageManager pm = getPackageManager();
        return pm.getLaunchIntentForPackage(pkg) != null;
    }

    private void checkIfNeedRemoveNotification(StatusBarNotification sbn) {
        String pkg = sbn.getPackageName();

        int index = getItemIndex(sbn.getPackageName());
        if (index != -1) {
            mPkgList.remove(index);

            NotificationListenerItem item = null;

            if (!mPkgList.isEmpty()) {
                int idx = findHighPriorityItem();

                item = mPkgList.get(idx);
                if (DEBUG)
                    Log.v(TAG, "remove notification(" + pkg
                            + "), curPkg: " + item.mPkg);
            } else {
                if (DEBUG)
                    Log.v(TAG, "remove notification(" + pkg
                            + "), no notification left!");
            }
            notifyNotificationChanged(item);
        }
    }

    private String mCurPkg = "";

    private void notifyNotificationChanged(NotificationListenerItem item) {
        Intent intent = new Intent(MSG_NOTIFICATION_CHANGED);
        String pkg = "";
        if (item != null) {
            pkg = item.mPkg;
            PendingIntent pendingIntent = item.mPendingIntent;
            int icon = item.mIcon;
            intent.putExtra(PACKAGE, pkg);
            intent.putExtra(PENDING_INTENT, pendingIntent);
            intent.putExtra(AMOUNT, mPkgList.size());
            intent.putExtra(ICON, icon);

            mCurPkg = pkg;
        } else {
            intent.putExtra(PACKAGE, "");

            mCurPkg = "";
        }

        sendBroadcast(intent);

        if (DEBUG) {
            if (!pkg.isEmpty()) Log.i(TAG, "notifyNotificationChanged(): " + pkg);
        }
    }

    private int getItemIndex(String pkg) {
        int i = 0;
        for (; i < mPkgList.size(); i++) {
            NotificationListenerItem item = mPkgList.get(i);

            if (item.mPkg.equals(pkg)) {
                break;
            }
        }

        if (mPkgList.size() == i) {
            return -1;
        } else {
            return i;
        }
    }

    private int findHighPriorityItem() {
        int index = -1;

        int size = mPkgList.size();
        if (size > 0) {
            int i = size - 1;
            for (; i >= 0; i--) {
                NotificationListenerItem item = mPkgList.get(i);

                // find a Notification with FLAG_AUTO_CANCEL from rear to head
                if ((item.mFlags & Notification.FLAG_AUTO_CANCEL) != 0)
                    break;
            }

            if (i < 0) {
                // there is no Notification with FLAG_AUTO_CANCEL
                // the last one is the highest priority
                index = size - 1;
            } else {
                index = i;
            }
        }

        return index;
    }

    public int findNextHighPriorityItem(boolean priority) {
        int index = -1;

        if (mPkgList.size() >= 1) {
            int size = mPkgList.size();

            //find current item
            int cur = size - 1;
            for (; cur >= 0; cur--) {
                if (mPkgList.get(cur).mPkg.equals(mCurPkg))
                    break;
            }

            //find second priority item
            int i = (cur - 1 + size) % size;
            int target = i;

            if (priority) {
                i = (i - 1 + size) % size;
                for (; i != cur; i = (i - 1 + size) % size) {
                    if ((mPkgList.get(i).mFlags & Notification.FLAG_AUTO_CANCEL) != 0 &&
                            (mPkgList.get(target).mFlags & Notification.FLAG_AUTO_CANCEL) == 0) {
                        target = i;
                    }
                }
            }

            index = target;
        }
        return index;
    }

    public int findPreHighPriorityItem(boolean priority) {
        int index = -1;

        if (mPkgList.size() >= 1) {
            int size = mPkgList.size();

            //find current item
            int cur = size - 1;
            for (; cur >= 0; cur--) {
                if (mPkgList.get(cur).mPkg.equals(mCurPkg))
                    break;
            }

            //find second priority item
            int i = (cur + 1) % size;
            int target = i;

            if (priority) {
                i = (i + 1) % size;
                for (; i != cur; i = (i + 1) % size) {
                    if ((mPkgList.get(i).mFlags & Notification.FLAG_AUTO_CANCEL) != 0 &&
                            (mPkgList.get(target).mFlags & Notification.FLAG_AUTO_CANCEL) == 0) {
                        target = i;
                    }
                }
            }

            index = target;
        }
        return index;
    }

    private boolean shouldAutoOpenMsg(StatusBarNotification sbn) {
        boolean openEverywhere = PreferenceUtil.getInstance(this).openMsgEverywhere();
        boolean openAtHOME = PreferenceUtil.getInstance(this).openMsgOnlyHOME();
        boolean permit = PreferenceUtil.getInstance(this).permitFloatView();

        boolean hasPendingIntent = (sbn.getNotification().contentIntent != null);

        return permit && hasPendingIntent && ((openEverywhere && !myselfOnTop()) || (openAtHOME && atLauncher()));
    }

    private List<String> getLaunchers() {
        List<String> launchers = new ArrayList<String>();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = mPackageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolveInfo) {
            launchers.add(ri.activityInfo.packageName);
        }

        return launchers;
    }

    private boolean myselfOnTop() {
        long start = System.currentTimeMillis();
        boolean ret = false;

        List<RunningTaskInfo> top = mActivityManager.getRunningTasks(1);
        if (top != null && top.size() > 0) {
            if (getApplicationInfo().packageName.equals(top.get(0).topActivity.getPackageName())) {
                ret = true;
            }
        }

        return ret;
    }

    private boolean atLauncher() {
        long start = System.currentTimeMillis();
        boolean ret = false;
        List<String> launchers = getLaunchers();
        List<RunningTaskInfo> top = mActivityManager.getRunningTasks(1);
        if (top != null && top.size() > 0 && launchers != null && launchers.size() > 0) {
            if (launchers.contains(top.get(0).topActivity.getPackageName())) {
                ret = true;
            }
        }

        Log.i(TAG, "atLauncher() cost: " + (System.currentTimeMillis() - start) + "ms");
        return ret;
    }

    private class NotificationListenerItem {
        public String mPkg;
        public int mFlags;
        public PendingIntent mPendingIntent;
        public int mIcon;

        public NotificationListenerItem(String pkg, int flags,
                                        PendingIntent pendingIntent, int icon) {
            mPkg = pkg;
            mFlags = flags;
            mPendingIntent = pendingIntent;
            mIcon = icon;
        }
    }
    /////////
}