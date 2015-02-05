package com.theandroidsuit.bytheway.sql.dao;

import android.content.Context;

import com.theandroidsuit.bytheway.sql.utils.CategoryDS;


/**
 * Created by Virginia Hernández on 21/01/15.
 */
public class CategoryDAO extends BTWDAO{

    CategoryDAO(Context ctx){
        super(new CategoryDS(ctx));
    }

}
