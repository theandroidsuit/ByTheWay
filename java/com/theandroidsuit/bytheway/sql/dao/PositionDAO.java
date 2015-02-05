package com.theandroidsuit.bytheway.sql.dao;

import android.content.Context;

import com.theandroidsuit.bytheway.sql.utils.PositionDS;


/**
 * Created by Virginia Hernández on 21/01/15.
 */
public class PositionDAO extends BTWDAO{

    PositionDAO(Context ctx){
        super(new PositionDS(ctx));
    }

}
