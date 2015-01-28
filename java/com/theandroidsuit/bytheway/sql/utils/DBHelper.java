package com.theandroidsuit.bytheway.sql.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.theandroidsuit.bytheway.sql.databaseTable.PositionEntity;

import java.sql.SQLException;

/**
 * Created by Virginia Hernández on 21/01/15.
 */
public class DBHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "btw_psitions.db";
    private static final int DATABASE_VERSION = 1;

    private Dao<PositionEntity, Integer> usuarioDao;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, PositionEntity.class);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        onCreate(db, connectionSource);
    }

    public Dao<PositionEntity, Integer> getPositionDao() throws SQLException {
        if (usuarioDao == null) {
            usuarioDao = getDao(PositionEntity.class);
        }
        return usuarioDao;
    }


    @Override
    public void close() {
        super.close();
        usuarioDao = null;
    }

}