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


    public abstract BTWEntity createEntity(BTWEntity pos);
    public abstract BTWEntity getEntityById(long idPosition);
    public abstract ArrayList<BTWEntity> getAllEntities();
    public abstract void deleteEntity(BTWEntity entity);
    public abstract BTWEntity updateEntity(BTWEntity entity);
    public abstract String[] getAllColumns();
    public abstract ContentValues getContentValues(BTWEntity entity);
    public abstract BTWEntity cursorToEntity(Cursor cursor);

    public abstract String getTableName();
}
