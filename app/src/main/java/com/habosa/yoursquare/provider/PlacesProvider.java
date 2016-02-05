package com.habosa.yoursquare.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.habosa.yoursquare.sql.PlacesSQLHelper;

/**
 * ContentProvider for Places
 */
public class PlacesProvider extends ContentProvider {

    public static final Uri CONTENT_URI = Uri.parse("content://com.habosa.yoursquare/places");

    private static final String TAG = "PlacesProvder";

    private PlacesSQLHelper mHelper;
    private SQLiteDatabase mDatabase;


    @Override
    public boolean onCreate() {
        mHelper = new PlacesSQLHelper(getContext());
        mDatabase = mHelper.getWritableDatabase();
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO(samstern): What to do with the URI? I think I should be using it to get the table name.
        Log.d(TAG, "query:" + uri.toString());
        return mDatabase.query(PlacesSQLHelper.TABLE, projection, selection, selectionArgs,
                null, null, sortOrder);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        Log.d(TAG, "insert:" + values);
        long id = mDatabase.insert(PlacesSQLHelper.TABLE, null, values);
        return Uri.withAppendedPath(CONTENT_URI, String.valueOf(id));
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete:" + selection);
        return mDatabase.delete(PlacesSQLHelper.TABLE, selection, selectionArgs);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.d(TAG, "update:" + values);
        return mDatabase.update(PlacesSQLHelper.TABLE, values, selection, selectionArgs);
    }
}
