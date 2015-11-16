package com.stone.database;

/**
 * Created by stoneami on 2015/11/6.
 */
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBManager{
    private DatabaseHelper mHelper;

    private static DBManager mInstance = null;

    public static DBManager getInstance(Context context){
        if(mInstance == null){
            mInstance = new DBManager(context);
        }

        return mInstance;
    }

    private DBManager(Context context){
        mHelper = new DatabaseHelper(context);
    }

    public void insert(String pkg, String datetime){
        mHelper.insert(pkg,datetime);
    }

    public String getMostPopularApp(int dur){
        String pkg = null;

        SQLiteDatabase db = mHelper.getWritableDatabase();

        /*String[] columns = new String[]{"package", "count(package) as total"};
        String selection = "open_time between datetime(?,?) and datetime(?)";
        String[] selectionArgs =  new String[]{"'now'","'-" + dur + " hour'","'now'"};
        String groupBy = AppRecord.PACKAGE_NAME;
        String orderBy = "total desc";

        //query(String table, String[] columns, String selection,String[] selectionArgs, String groupBy, String having,
        //        String orderBy, String limit)
        Cursor cursor = db.query(AppRecord.TABLE_NAME, columns, selection, selectionArgs, groupBy, null, orderBy, null);*/
        String selection =
                "select package, count(package) as total from app_open_record " +
                "where open_time between datetime(?,?) and datetime(?) " +
                "group by package "+
                "order by total desc";
        String[] selectionArgs = new String[]{"now","-" + dur + " hour","now"};
        Cursor cursor = db.rawQuery(selection,selectionArgs);
        if(cursor != null && cursor.getCount() > 0){
            cursor.moveToFirst();
            pkg = cursor.getString(cursor.getColumnIndex(AppRecord.PACKAGE_NAME));
            cursor.close();
        }

        Cursor cur = db.rawQuery("select datetime(?)",new String[]{"'now'"});
        if(cur.getCount() > 0){
            Log.i("DBManager","now is " + cur.getString(0));
        }else{
            Log.i("DBManager","now is unknown ?");
        }

        db.close();

        Log.i("DBManager", "getMostPopularApp(): " + pkg);
        return pkg;
    }
    public void closeDB() {
        mHelper.close();
    }
}
