package com.theandroidsuit.bytheway.sql.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Virginia Hernández on 13/01/15.
 */
public class BTWSQLiteHelper extends SQLiteOpenHelper{


    // TODO: Make a diferent SQLiteOpenHelper class for each table

    public static final String TABLE_POSITION = "position";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_SENSITIVE = "sensitive";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_STATUS = "status";


    private static final String DATABASE_NAME = "position.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String POSITION_TABLE_CREATE = "create table "
            + TABLE_POSITION + "("
            + COLUMN_ID         + " integer primary key autoincrement, "
            + COLUMN_LATITUDE   + " real not null, "
            + COLUMN_LONGITUDE  + " real not null, "
            + COLUMN_SENSITIVE  + " integer default 10 not null, "
            + COLUMN_TITLE      + " text not null, "
            + COLUMN_DESCRIPTION + " text, "
            + COLUMN_STATUS     + " text default 'on' not null "
            + ");";


    public BTWSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(POSITION_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(BTWSQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POSITION);
        onCreate(db);
    }
}
