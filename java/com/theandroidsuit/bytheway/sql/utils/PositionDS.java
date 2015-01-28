package com.theandroidsuit.bytheway.sql.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.theandroidsuit.bytheway.sql.bo.BTWEntity;
import com.theandroidsuit.bytheway.sql.bo.PositionEntity;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Virginia Hernández on 13/01/15.
 */
public class PositionDS extends BTWDS{



    private String[] allColumns = {
            BTWSQLiteHelper.COLUMN_ID,
            BTWSQLiteHelper.COLUMN_LATITUDE,
            BTWSQLiteHelper.COLUMN_LONGITUDE,
            BTWSQLiteHelper.COLUMN_SENSITIVE,
            BTWSQLiteHelper.COLUMN_TITLE,
            BTWSQLiteHelper.COLUMN_DESCRIPTION,
            BTWSQLiteHelper.COLUMN_STATUS};



    /* Constructor */
    public PositionDS(Context context) {
        super(context);
    }


    /* Getter by status */
    public ArrayList<PositionEntity> getAllPositionsByStatus(String status) {

        ArrayList<PositionEntity> positions = new ArrayList<PositionEntity>();

        String whereClause = BTWSQLiteHelper.COLUMN_STATUS + " = ? ";
        Cursor cursor = database.query(BTWSQLiteHelper.TABLE_POSITION, allColumns,
                whereClause, new String[] {status}, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            PositionEntity position = (PositionEntity) cursorToEntity(cursor);
            positions.add(position);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();

        return positions;
    }


    /* OVERRIDE METHODS */

    @Override
    public ContentValues getContentValues(BTWEntity entity) {

        PositionEntity pos = (PositionEntity) entity;

        ContentValues values = new ContentValues();
        values.put(BTWSQLiteHelper.COLUMN_LATITUDE, pos.getLatitude());
        values.put(BTWSQLiteHelper.COLUMN_LONGITUDE, pos.getLongitude());
        values.put(BTWSQLiteHelper.COLUMN_SENSITIVE, pos.getSensitive());
        values.put(BTWSQLiteHelper.COLUMN_TITLE, pos.getTitle());
        values.put(BTWSQLiteHelper.COLUMN_DESCRIPTION, pos.getDescription());
        values.put(BTWSQLiteHelper.COLUMN_STATUS, pos.getStatus());
        return values;
    }

    @Override
    public String[] getAllColumns() {
        return allColumns;
    }

    public BTWEntity cursorToEntity(Cursor cursor) {

        if( cursor != null) {
            PositionEntity pos = new PositionEntity();

            pos.setId(cursor.getLong(0));
            pos.setLatitude(cursor.getDouble(1));
            pos.setLongitude(cursor.getDouble(2));
            pos.setSensitive(cursor.getLong(3));
            pos.setTitle(cursor.getString(4));
            pos.setDescription(cursor.getString(5));
            pos.setStatus(cursor.getString(6));

            return pos;
        }
        return null;
    }
}
