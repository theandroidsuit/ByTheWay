package com.theandroidsuit.bytheway.sql.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.theandroidsuit.bytheway.sql.bo.BTWEntity;
import com.theandroidsuit.bytheway.sql.bo.CategoryEntity;

import java.util.ArrayList;

/**
 * Created by Virginia Hernández on 13/01/15.
 */
public class CategoryDS extends BTWDS{



    private String[] allColumns = {
            BTWSQLiteHelper.COLUMN_ID,
            BTWSQLiteHelper.COLUMN_TITLE,
            BTWSQLiteHelper.COLUMN_STATUS};



    /* Constructor */
    public CategoryDS(Context context) {
        super(context);
    }

    @Override
    public String getTableName(){
        return BTWSQLiteHelper.TABLE_CATEGORY;
    }

    @Override
    public String[] getAllColumns() {
        return allColumns;
    }


    /* Create */
    public BTWEntity createEntity(BTWEntity pos) {

        ContentValues values = getContentValues(pos);

        long insertId = database.insert(
                getTableName(),
                null,
                values);

        Cursor cursor = database.query(
                getTableName(),
                getAllColumns(),
                BTWSQLiteHelper.COLUMN_ID + " = " + insertId,
                null, null, null, null);

        cursor.moveToFirst();
        BTWEntity newEntity = cursorToEntity(cursor);
        cursor.close();

        return newEntity;
    }

    /* Getter One */
    public BTWEntity getEntityById(long idEntity) {
        Cursor cursor = database.query(
                getTableName(),
                getAllColumns(),
                BTWSQLiteHelper.COLUMN_ID + " = " + idEntity,
                null, null, null, null);

        cursor.moveToFirst();
        BTWEntity newEntity = cursorToEntity(cursor);
        cursor.close();

        return newEntity;
    }

    /* Getter ALL */
    public ArrayList<BTWEntity> getAllEntities() {
        ArrayList<BTWEntity> positions = new ArrayList<BTWEntity>();

        Cursor cursor = database.query(
                getTableName(),
                getAllColumns(),
                null, null, null, null, null);

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
    public void deleteEntity(BTWEntity entity) {

        long id = entity.getId();
        System.out.println("Position deleted with id: " + id);

        database.delete(
                getTableName(),
                BTWSQLiteHelper.COLUMN_ID + " = " + id, null);
    }

    /* Update */
    public BTWEntity updateEntity(BTWEntity entity) {

        ContentValues values = getContentValues(entity);

        String whereClause =  BTWSQLiteHelper.COLUMN_ID + " = ?";

        database.update(
                getTableName(),
                values,
                whereClause,
                new String[] {String.valueOf(entity.getId())});

        Cursor cursor = database.query(
                getTableName(),
                getAllColumns(),
                BTWSQLiteHelper.COLUMN_ID + " = " + entity.getId(),
                null, null, null, null);

        cursor.moveToFirst();
        BTWEntity updateEntity = cursorToEntity(cursor);
        cursor.close();

        return updateEntity;
    }


    /* Getter by status */
    public ArrayList<CategoryEntity> getAllCategoryByStatus(String status) {

        ArrayList<CategoryEntity> categories = new ArrayList<CategoryEntity>();

        String whereClause = BTWSQLiteHelper.COLUMN_STATUS + " = ? ";
        Cursor cursor = database.query(
                getTableName(),
                allColumns,
                whereClause,
                new String[] {status},
                null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            CategoryEntity category = (CategoryEntity) cursorToEntity(cursor);
            categories.add(category);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();

        return categories;
    }


    @Override
    public ContentValues getContentValues(BTWEntity entity) {

        CategoryEntity cat = (CategoryEntity) entity;

        ContentValues values = new ContentValues();
        values.put(BTWSQLiteHelper.COLUMN_TITLE, cat.getTitle());
        values.put(BTWSQLiteHelper.COLUMN_STATUS, cat.getStatus());
        return values;
    }


    public BTWEntity cursorToEntity(Cursor cursor) {

        if( cursor != null) {
            CategoryEntity pos = new CategoryEntity();

            pos.setId(cursor.getLong(0));
            pos.setTitle(cursor.getString(1));
            pos.setStatus(cursor.getString(2));

            return pos;
        }
        return null;
    }
}
