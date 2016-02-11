package com.habosa.yoursquare.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.util.Log;

import com.habosa.yoursquare.provider.PlacesProvider;
import com.habosa.yoursquare.sql.PlacesSQLHelper;
import com.habosa.yoursquare.sql.QueryBuilder;

/**
 * Created by samstern on 11/24/15.
 */
public class PlacesSource {

    private static final String TAG = "PlacesSource";

    private static final String ORDER_BY_ID_DESC = String.format("%s DESC", PlacesSQLHelper.COL_ID);

    private Context mContext;

    private static final String[] COLUMNS = new String[]{
            PlacesSQLHelper.COL_ID,
            PlacesSQLHelper.COL_GOOGLEPLACEID,
            PlacesSQLHelper.COL_NAME,
            PlacesSQLHelper.COL_ADDRESS
    };

    public PlacesSource(Context context) {
        mContext = context;
    }

    public void create(Place place) {
        // Create ContentValues
        ContentValues contentValues = new ContentValues();
        contentValues.put(PlacesSQLHelper.COL_GOOGLEPLACEID, place.getGooglePlaceId());
        contentValues.put(PlacesSQLHelper.COL_NAME, place.getName());
        contentValues.put(PlacesSQLHelper.COL_ADDRESS, place.getAddress());

        // Insert
        mContext.getContentResolver().insert(PlacesProvider.CONTENT_URI, contentValues);
    }

    public boolean delete(Place place) {
        if (place.getId() == 0L) {
            throw new IllegalArgumentException("Can't delete place without ID!");
        }

        String query = new QueryBuilder().equals(PlacesSQLHelper.COL_ID, place.getId()).build();
        int numRows = mContext.getContentResolver().delete(PlacesProvider.CONTENT_URI, query, null);

        return (numRows > 0);
    }

    public Cursor getAll() {
        return query(null);
    }

    public CursorLoader getLoader(Context context, String query) {
        Uri uri = PlacesProvider.CONTENT_URI;
        return new CursorLoader(context, uri, COLUMNS, query, null, ORDER_BY_ID_DESC);
    }

    public String getFuzzySearchQuery(String term) {
        return new QueryBuilder()
                .contains(PlacesSQLHelper.COL_NAME, term)
                .or()
                .contains(PlacesSQLHelper.COL_ADDRESS, term)
                .build();
    }

    public Cursor query(String query) {
        Log.d(TAG, "query:" + query);
        return mContext.getContentResolver().query(PlacesProvider.CONTENT_URI, COLUMNS, query,
                null, ORDER_BY_ID_DESC);
    }
}
