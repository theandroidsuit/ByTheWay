package com.theandroidsuit.bytheway.sql.dao;

import android.content.Context;

import com.theandroidsuit.bytheway.sql.utils.CategoryPositionDS;


/**
 * Created by Virginia Hernández on 21/01/15.
 */
public class CategoryPositionDAO extends BTWDAO{

    CategoryPositionDAO(Context ctx){
        super(new CategoryPositionDS(ctx));
    }
}
