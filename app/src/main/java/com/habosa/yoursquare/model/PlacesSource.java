package com.habosa.yoursquare.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.habosa.yoursquare.sql.PlacesSQLHelper;
import com.habosa.yoursquare.sql.QueryBuilder;

/**
 * Created by samstern on 11/24/15.
 */
public class PlacesSource {

    private static final String TAG = "PlacesSource";

    private SQLiteDatabase mDatabase;
    private PlacesSQLHelper mHelper;

    private static final String[] COLUMNS = new String[]{
            PlacesSQLHelper.COL_ID,
            PlacesSQLHelper.COL_GOOGLEPLACEID,
            PlacesSQLHelper.COL_NAME,
            PlacesSQLHelper.COL_ADDRESS
    };

    public PlacesSource(Context context) {
        mHelper = new PlacesSQLHelper(context);
    }

    public void open() {
        mDatabase = mHelper.getWritableDatabase();
    }

    public void close() {
        mHelper.close();
    }

    public Place create(Place place) {
        // Insert
        ContentValues contentValues = new ContentValues();
        contentValues.put(PlacesSQLHelper.COL_GOOGLEPLACEID, place.getGooglePlaceId());
        contentValues.put(PlacesSQLHelper.COL_NAME, place.getName());
        contentValues.put(PlacesSQLHelper.COL_ADDRESS, place.getAddress());
        long id = mDatabase.insert(PlacesSQLHelper.TABLE, null, contentValues);

        // Return inserted place
        return getBy(PlacesSQLHelper.COL_ID, id);
    }

    public boolean delete(Place place) {
        if (place.getId() == 0L) {
            throw new IllegalArgumentException("Can't delete place without ID!");
        }

        String query = new QueryBuilder().equals(PlacesSQLHelper.COL_ID, place.getId()).build();
        int numRows = mDatabase.delete(PlacesSQLHelper.TABLE, query, null);
        return (numRows > 0);
    }

    public Cursor getAll() {
        return query(null);
    }

    public Cursor fuzzySearch(String term) {
        // TODO(samstern): When is this cursor closed?
        String query = new QueryBuilder()
                .contains(PlacesSQLHelper.COL_NAME, term)
                .or()
                .contains(PlacesSQLHelper.COL_ADDRESS, term)
                .build();

        return query(query);
    }

    public Cursor query(String query) {
        Log.d(TAG, "query:" + query);
        return mDatabase.query(PlacesSQLHelper.TABLE, COLUMNS, query,
                null, null, null, null);
    }


    public Place getBy(String column, Object value) {
        String query = new QueryBuilder().equals(column, value).build();
        Cursor cursor = mDatabase.query(PlacesSQLHelper.TABLE, COLUMNS, query,
                null, null, null, null);
        cursor.moveToFirst();
        Place place = fromCursor(cursor);
        cursor.close();
        return place;
    }

    public Place fromCursor(Cursor cursor) {
        int idInd = cursor.getColumnIndex(PlacesSQLHelper.COL_ID);
        int googlePlaceIdInd = cursor.getColumnIndex(PlacesSQLHelper.COL_GOOGLEPLACEID);
        int nameInd = cursor.getColumnIndex(PlacesSQLHelper.COL_NAME);
        int addressInd = cursor.getColumnIndex(PlacesSQLHelper.COL_ADDRESS);

        Place place = new Place();
        place.setId(cursor.getLong(idInd));
        place.setGooglePlaceId(cursor.getString(googlePlaceIdInd));
        place.setName(cursor.getString(nameInd));
        place.setAddress(cursor.getString(addressInd));

        return place;
    }

}
