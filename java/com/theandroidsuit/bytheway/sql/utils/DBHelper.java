package com.theandroidsuit.bytheway.sql.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.theandroidsuit.bytheway.geofence.PositionManager;
import com.theandroidsuit.bytheway.sql.databaseTable.Category;
import com.theandroidsuit.bytheway.sql.databaseTable.CategoryPosition;
import com.theandroidsuit.bytheway.sql.databaseTable.Position;

import java.sql.SQLException;

/**
 * Created by Virginia Hernández on 21/01/15.
 */
public class DBHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "btw_1.db";
    private static final int DATABASE_VERSION = 1;

    private Dao<Position, Integer> positionDao;
    private Dao<Category, Integer> categoryDao;
    private Dao<CategoryPosition, Integer> categoryPositionDao;


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Position.class);
            TableUtils.createTable(connectionSource, Category.class);
            TableUtils.createTable(connectionSource, CategoryPosition.class);


            // Create Default Category
            Category cat = new Category();
            cat.setId(Category.DEFAULT_CATEGORY_ID);
            cat.setTitle(PositionManager.CATEGORY_DEFAULT);
            cat.setStatus(PositionManager.STATUS_ACTIVATED);
            getCategoryDao().create(cat);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        onCreate(db, connectionSource);
    }

    public Dao<Position, Integer> getPositionDao() throws SQLException {
        if (positionDao == null) {
            positionDao = getDao(Position.class);
        }
        return positionDao;
    }


    public Dao<Category, Integer> getCategoryDao() throws SQLException {
        if (categoryDao == null) {
            categoryDao = getDao(Category.class);
        }
        return categoryDao;
    }

    public Dao<CategoryPosition, Integer> getCategoryPositionDao() throws SQLException {
        if (categoryPositionDao == null) {
            categoryPositionDao = getDao(CategoryPosition.class);
        }
        return categoryPositionDao;
    }


    @Override
    public void close() {
        super.close();
        positionDao = null;
        categoryDao = null;
        categoryPositionDao = null;
    }

}