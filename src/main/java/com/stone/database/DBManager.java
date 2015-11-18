package com.stone.database;

/**
 * Created by stoneami on 2015/11/6.
 */

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBManager {
    private static String TAG = "DBManager";
    private static boolean DEBUG = true;

    private DatabaseHelper mHelper;

    private static DBManager mInstance = null;

    private static Context mContext;

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

    public void insert(String pkg, String datetime) {
        mHelper.insert(pkg, datetime);
    }

    public String getMostPopularApp(int dur) {
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

        Log.i(TAG, "getMostPopularApp(): " + pkg);
        return pkg;
    }

    public void closeDB() {
        mHelper.close();
    }
}
