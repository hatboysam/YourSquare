package com.habosa.yoursquare.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class PlacesSQLHelper extends SQLiteOpenHelper {

    public static final String TAG = "PlacesSQLHelper";

    public static final String DATABASE_NAME = "yoursquare.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE = "places";

    public static final String COL_ID = "_id";
    public static final String TYPE_ID = "integer primary key autoincrement";

    public static final String COL_GOOGLEPLACEID = "googleplaceid";
    public static final String TYPE_GOOGLEPLACEID = "text not null";

    public static final String COL_NAME = "name";
    public static final String TYPE_NAME = "text not null";

    public static final String COL_ADDRESS = "address";
    public static final String TYPE_ADDRESS = "text not null";

    public static final String COL_LAT = "lat";
    public static final String TYPE_LAT = "real not null";

    public static final String COL_LNG = "lng";
    public static final String TYPE_LNG = "real not null";

    public PlacesSQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate");
        String createStatement = "CREATE TABLE " +
                TABLE + "(" +
                COL_ID + " " + TYPE_ID + "," +
                COL_GOOGLEPLACEID + " " + TYPE_GOOGLEPLACEID + "," +
                COL_NAME + " " + TYPE_NAME + "," +
                COL_ADDRESS + " " + TYPE_ADDRESS + "," +
                COL_LAT + " " + TYPE_LAT + "," +
                COL_LNG + " " + TYPE_LNG +
                ");";
        db.execSQL(createStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade:" + oldVersion + ":" + newVersion);
        // TODO(samstern): upgrades
    }
}
