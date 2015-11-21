package com.stone.reminder.database;

/**
 * Created by stoneami on 2015/11/6.
 */

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.stone.reminder.NotificationListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DBManager {
    private static String TAG = "DBManager";
    private static boolean DEBUG = true;

    private DatabaseHelper mHelper;

    private static DBManager mInstance = null;

    private static Context mContext;

    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    public static DBManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DBManager(context);
            mContext = context;
        }

        return mInstance;
    }

    private DBManager(Context context) {
        mHelper = new DatabaseHelper(context);
    }

    private ArrayList<StatisticsItem> mStatisticsList = new ArrayList<>(10);

    private Runnable mLoadingRunnable = new Runnable() {
        @Override
        public void run() {
            SQLiteDatabase db = mHelper.getWritableDatabase();
            /**
             * select datetime('now') is 8 hour earlier than Beijing Locale, so we increase 8 hours manually
             */
            String selection = "select package, count(package) as total from app_open_record where open_time between datetime(?,?,?) and datetime(?,?) group by package order by total desc";
            String[] selectionArgs = new String[]{"now", "+8 hour", "-24 hour", "now", "+8 hour"};
            Cursor cursor = db.rawQuery(selection, selectionArgs);
            while (cursor.moveToNext()) {
                String pkg = cursor.getString(cursor.getColumnIndex(AppRecord.PACKAGE_NAME));
                int count = cursor.getInt(cursor.getColumnIndex("total"));
                Log.i(TAG, "asyncLoadData(): pkg=" + pkg + ", count=" + count);
                mStatisticsList.add(new StatisticsItem(pkg, count));
            }

            //ready to show often-open app
            if(cursor.getCount() > 0){
                mContext.sendBroadcast(new Intent(NotificationListener.MSG_DISPLAY_OFTEN_OPEN_ICON));
            }

            cursor.close();

            mReady = true;

            Log.i(TAG,"DBManager initialize completely !");
        }
    };

    private boolean mReady = false;
    public void asyncLoadData(){
        if(mReady) {
            Log.i(TAG,"DBManager has been initialized, no need to do again !");
            return;
        }

        mExecutor.execute(mLoadingRunnable);
    }

    private static class StatisticsItem implements Comparable<StatisticsItem>{
        private String pkg;
        private int count;

        public StatisticsItem(String pkg,int count){
            this.pkg = pkg;
            this.count = count;
        }

        @Override
        public int compareTo(StatisticsItem another) {
            if(this.count > another.count){
                return -1;
            }else{
                return 1;
            }
        }
    }

    private InsertRunnable mInsertRunnable = new InsertRunnable();
    private class InsertRunnable implements Runnable {
        private String pkg;
        private String datetime;

        public InsertRunnable updateData(String pkg, String datetime){
            this.pkg = pkg;
            this.datetime = datetime;

            return this;
        }

        @Override
        public void run() {
            int n = mStatisticsList.size();
            int i = 0;
            for(;i<n;i++){
                if(mStatisticsList.get(i).pkg.equals(pkg)){
                    ++ mStatisticsList.get(i).count;
                    break;
                }
            }

            if(n == 0 || i == n){
                mStatisticsList.add(new StatisticsItem(pkg,1));
            }

            Collections.sort(mStatisticsList);

            if(DEBUG){
                int MAX = mStatisticsList.size();
                Log.i(TAG,"******after sort()******");
                for(int t=0;t<MAX;t++){
                    Log.i(TAG,"pkg=" + mStatisticsList.get(t).pkg + " count=" + mStatisticsList.get(t).count);
                }
                Log.i(TAG,"************************");
            }

            insertDB(pkg, datetime);
        }
    }

    public void asyncInsert(final String pkg, final String datetime){
        mExecutor.execute(mInsertRunnable.updateData(pkg, datetime));
    }

    public String getMostPopularApp(){
        String pkg = "";

        if(mReady && mStatisticsList.size() > 0){
            pkg = mStatisticsList.get(0).pkg;
        }

        Log.i(TAG, "getMostPopularApp(): " + pkg);
        return pkg;
    }

    private void insertDB(String pkg, String datetime) {
        mHelper.insert(pkg, datetime);
    }

    private String getMostPopularAppFromDB(int dur) {
        String pkg = "";

        SQLiteDatabase db = mHelper.getWritableDatabase();

        /**
         * select datetime('now') is 8 hour earlier than Beijing Locale, so we increase 8 hours manually
         */
        String selection = "select package, count(package) as total from app_open_record where open_time between datetime(?,?,?) and datetime(?,?) group by package order by total desc";
        String[] selectionArgs = new String[]{"now", "+8 hour", "-" + dur + " hour", "now", "+8 hour"};
        Cursor cursor = db.rawQuery(selection, selectionArgs);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            pkg = cursor.getString(cursor.getColumnIndex(AppRecord.PACKAGE_NAME));
            cursor.close();
        }

        if (DEBUG) {
            Cursor cur = db.rawQuery("select datetime('now','+8 hour')", null);
            if (cur.getCount() > 0) {
                cur.moveToFirst();
                Log.i(TAG, "now is " + cur.getString(0));
            }
            cur.close();
        }

        db.close();

        Log.i(TAG, "getMostPopularAppFromDB(): " + pkg);
        return pkg;
    }
}