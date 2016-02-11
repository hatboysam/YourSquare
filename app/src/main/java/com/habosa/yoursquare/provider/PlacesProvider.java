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
        Log.d(TAG, "query:" + uri.toString());
        Cursor cursor = mDatabase.query(PlacesSQLHelper.TABLE, projection, selection, selectionArgs,
                null, null, sortOrder);

        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), CONTENT_URI);
        } else {
            Log.w(TAG, "query: null context, could not set up resolver.");
        }

        return cursor;
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
        notifyChange();

        return Uri.withAppendedPath(CONTENT_URI, String.valueOf(id));
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete:" + selection);
        notifyChange();

        return mDatabase.delete(PlacesSQLHelper.TABLE, selection, selectionArgs);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.d(TAG, "update:" + values);
        notifyChange();

        return mDatabase.update(PlacesSQLHelper.TABLE, values, selection, selectionArgs);
    }

    private void notifyChange() {
        if (getContext() == null) {
            Log.w(TAG, "notifyChange: null context");
            return;
        }

        getContext().getContentResolver().notifyChange(CONTENT_URI, null);
    }
}
