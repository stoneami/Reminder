package com.stone.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by stoneami on 2015/11/6.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "reminder.db";

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "DatabaseHelper onCreate");

        StringBuffer sBuffer = new StringBuffer();

        sBuffer.append("CREATE TABLE [" + AppRecord.TABLE_NAME + "] (");
        sBuffer.append("[_id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ");
        sBuffer.append("[package] VARCHAR,");
        sBuffer.append("[open_time] DATETIME)");

        db.execSQL(sBuffer.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insert(String pkg, String datetime){
        SQLiteDatabase db = getWritableDatabase();

        if(db == null){
            Log.w(TAG,"can not open the database !");
            return;
        }

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(AppRecord.PACKAGE_NAME, pkg);
            values.put(AppRecord.OPEN_TIME, datetime);
            long row = db.insert(AppRecord.TABLE_NAME, null, values);
            Log.i(TAG, "insert(); row=" + row);

            db.setTransactionSuccessful();
        }finally {
            db.endTransaction();
        }

        db.close();
    }
}
