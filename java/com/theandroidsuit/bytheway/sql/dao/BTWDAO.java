package com.theandroidsuit.bytheway.sql.dao;

import android.content.Context;
import android.util.Log;

import com.theandroidsuit.bytheway.sql.bo.BTWEntity;
import com.theandroidsuit.bytheway.sql.utils.PositionDS;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Virginia Hernández on 21/01/15.
 *
 * Superclase para todos los DAO
 *
 */
public abstract class BTWDAO {


    public final String TAG = this.getClass().getName();

    private PositionDS ds;
    private Context ctx;

    BTWDAO(Context ctx){

        this.ctx = ctx;
        ds = new PositionDS(ctx);
    }

    public BTWEntity create(BTWEntity entity){

        BTWEntity ent = null;
        try{
            ds.open ();
            ent = ds.createPosition(entity);
            ds.close();
        }catch (SQLException e){
            Log.e(TAG, e.getMessage());
        }

        return ent;
    }

    public BTWEntity update(BTWEntity position){
        BTWEntity pos = null;
        try{
            ds.open ();
            pos = ds.updatePosition(position);
            ds.close();
        }catch (SQLException e){
            Log.e(TAG, e.getMessage());
        }

        return pos;
    }

    public void delete(BTWEntity position){
        try{
            ds.open ();
            ds.deletePosition(position);
            ds.close();
        }catch (SQLException e){
            Log.e(TAG, e.getMessage());
        }
    }

    public List<BTWEntity> getAllEntities(){

        List<BTWEntity> list = null;
        try{
            ds.open ();
            list = ds.getAllEntities();
            ds.close();
        }catch (SQLException e){
            Log.e(TAG, e.getMessage());
        }

        return list;
    }


    public BTWEntity getById(Long id){

        BTWEntity pos = null;
        try{
            ds.open ();
            pos = ds.getEntityById(id);
            ds.close();
        }catch (SQLException e){
            Log.e(TAG, e.getMessage());
        }

        return pos;
    }

}
