package com.theandroidsuit.bytheway.sql.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.theandroidsuit.bytheway.sql.bo.BTWEntity;
import com.theandroidsuit.bytheway.sql.bo.CategoryPositionEntity;

import java.util.ArrayList;

/**
 * Created by Virginia Hernández on 13/01/15.
 */
public class CategoryPositionDS extends BTWDS{



    private String[] allColumns = {
            BTWSQLiteHelper.COLUMN_ID,
            BTWSQLiteHelper.COLUMN_ID_CATEGORY,
            BTWSQLiteHelper.COLUMN_ID_POSITION};



    /* Constructor */
    public CategoryPositionDS(Context context) {
        super(context);
    }

    @Override
    public String getTableName(){
        return BTWSQLiteHelper.TABLE_CATEGORY_POSITION;
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
    public ArrayList<CategoryPositionEntity> getAllCategoryByStatus(String status) {

        ArrayList<CategoryPositionEntity> categories = new ArrayList<CategoryPositionEntity>();

        String whereClause = BTWSQLiteHelper.COLUMN_STATUS + " = ? ";
        Cursor cursor = database.query(
                getTableName(),
                allColumns,
                whereClause,
                new String[] {status},
                null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            CategoryPositionEntity category = (CategoryPositionEntity) cursorToEntity(cursor);
            categories.add(category);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();

        return categories;
    }


    @Override
    public ContentValues getContentValues(BTWEntity entity) {

        CategoryPositionEntity cat = (CategoryPositionEntity) entity;

        ContentValues values = new ContentValues();
        values.put(BTWSQLiteHelper.COLUMN_ID_CATEGORY, cat.getCategoryId());
        values.put(BTWSQLiteHelper.COLUMN_ID_POSITION, cat.getPositionId());
        return values;
    }


    public BTWEntity cursorToEntity(Cursor cursor) {

        if( cursor != null) {
            CategoryPositionEntity pos = new CategoryPositionEntity();

            pos.setId(cursor.getLong(0));
            pos.setCategoryId(cursor.getLong(1));
            pos.setPositionId(cursor.getLong(2));

            return pos;
        }
        return null;
    }
}
