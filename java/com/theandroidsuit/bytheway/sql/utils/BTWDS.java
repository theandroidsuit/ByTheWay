package com.theandroidsuit.bytheway.sql.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.theandroidsuit.bytheway.sql.bo.BTWEntity;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Virginia Hernández on 21/01/15.
 */
public abstract class BTWDS {

    // Database fields
    protected SQLiteDatabase database;
    protected BTWSQLiteHelper dbHelper;


    BTWDS(Context context){
        dbHelper = new BTWSQLiteHelper(context);
    }

    /* Open */
    public void open() throws SQLException {
        if(null != dbHelper && !database.isOpen())
            database = dbHelper.getWritableDatabase();
    }

    /* Close */
    public void close() {
        if (null != dbHelper && database.isOpen()) {
            dbHelper.close();
            database.close();
        }
    }


    /* Create */
    public BTWEntity createPosition(BTWEntity pos) {

        ContentValues values = getContentValues(pos);

        long insertId = database.insert(BTWSQLiteHelper.TABLE_POSITION, null, values);

        Cursor cursor = database.query(
                BTWSQLiteHelper.TABLE_POSITION,
                getAllColumns(),
                BTWSQLiteHelper.COLUMN_ID + " = " + insertId,
                null, null, null, null);



        cursor.moveToFirst();
        BTWEntity newEntity = cursorToEntity(cursor);
        cursor.close();

        return newEntity;
    }

    /* Getter One */
    public BTWEntity getEntityById(long idPosition) {
        Cursor cursor = database.query(
                BTWSQLiteHelper.TABLE_POSITION,
                getAllColumns(),
                BTWSQLiteHelper.COLUMN_ID + " = " + idPosition,
                null, null, null, null);

        cursor.moveToFirst();
        BTWEntity newEntity = cursorToEntity(cursor);
        cursor.close();

        return newEntity;
    }

    /* Getter ALL */
    public ArrayList<BTWEntity> getAllEntities() {
        ArrayList<BTWEntity> positions = new ArrayList<BTWEntity>();

        Cursor cursor = database.query(BTWSQLiteHelper.TABLE_POSITION,
                getAllColumns(), null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            BTWEntity entity = cursorToEntity(cursor);
            positions.add(entity);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();

        return positions;
    }

    /* Delete */
    public void deletePosition(BTWEntity entity) {

        long id = entity.getId();
        System.out.println("Position deleted with id: " + id);

        database.delete(
                BTWSQLiteHelper.TABLE_POSITION,
                BTWSQLiteHelper.COLUMN_ID + " = " + id, null);
    }

    /* Update */
    public BTWEntity updatePosition(BTWEntity entity) {

        ContentValues values = getContentValues(entity);

        String whereClause =  BTWSQLiteHelper.COLUMN_ID + " = ?";

        database.update(BTWSQLiteHelper.TABLE_POSITION, values,
                whereClause, new String[] {String.valueOf(entity.getId())});

        Cursor cursor = database.query(
                BTWSQLiteHelper.TABLE_POSITION,
                getAllColumns(),
                BTWSQLiteHelper.COLUMN_ID + " = " + entity.getId(),
                null, null, null, null);

        cursor.moveToFirst();
        BTWEntity updateEntity = cursorToEntity(cursor);
        cursor.close();

        return updateEntity;
    }



    public abstract String[] getAllColumns();
    public abstract ContentValues getContentValues(BTWEntity entity);
    public abstract BTWEntity cursorToEntity(Cursor cursor);
}
