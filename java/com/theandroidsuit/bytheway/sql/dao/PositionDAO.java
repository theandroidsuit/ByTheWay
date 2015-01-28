package com.theandroidsuit.bytheway.sql.dao;

import android.content.Context;
import android.util.Log;

import com.theandroidsuit.bytheway.sql.bo.PositionEntity;
import com.theandroidsuit.bytheway.sql.utils.PositionDS;

import java.sql.SQLDataException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Virginia Hernández on 21/01/15.
 */
public class PositionDAO extends BTWDAO{

    PositionDAO(Context ctx) {
        super(ctx);
    }
}
