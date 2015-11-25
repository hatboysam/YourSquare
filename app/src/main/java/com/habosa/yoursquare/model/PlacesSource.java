package com.habosa.yoursquare.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.habosa.yoursquare.sql.PlacesSQLHelper;

/**
 * Created by samstern on 11/24/15.
 */
public class PlacesSource {

    private SQLiteDatabase mDatabase;
    private PlacesSQLHelper mHelper;
    private Cursor mAllCursor;

    private static final String[] COLUMNS = new String[]{
            PlacesSQLHelper.COL_ID,
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
        if (mAllCursor != null) {
            mAllCursor.close();
        }

        mHelper.close();
    }

    public Place create(Place place) {
        // Insert
        ContentValues contentValues = new ContentValues();
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

        String query = String.format("%s = %s", PlacesSQLHelper.COL_ID, place.getId());
        int numRows = mDatabase.delete(PlacesSQLHelper.TABLE, query, null);
        return (numRows > 0);
    }

    public Cursor getAllCursor() {
        if (mAllCursor != null) {
            mAllCursor.close();
        }
        mAllCursor = mDatabase.query(PlacesSQLHelper.TABLE, COLUMNS, null,
                null, null, null, null);
        return mAllCursor;
    }

    public Place getBy(String column, Object value) {
        String query = String.format("%s = %s", column, value);
        Cursor cursor = mDatabase.query(PlacesSQLHelper.TABLE, COLUMNS, query,
                null, null, null, null);
        cursor.moveToFirst();
        Place place = fromCursor(cursor);
        cursor.close();
        return place;
    }

    public Place fromCursor(Cursor cursor) {
        int idInd = cursor.getColumnIndex(PlacesSQLHelper.COL_ID);
        int nameInd = cursor.getColumnIndex(PlacesSQLHelper.COL_NAME);
        int addressInd = cursor.getColumnIndex(PlacesSQLHelper.COL_ADDRESS);

        Place place = new Place();
        place.setId(cursor.getLong(idInd));
        place.setName(cursor.getString(nameInd));
        place.setAddress(cursor.getString(addressInd));

        return place;
    }

}
