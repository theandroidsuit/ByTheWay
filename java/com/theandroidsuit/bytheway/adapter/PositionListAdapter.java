package com.theandroidsuit.bytheway.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.theandroidsuit.bytheway.R;
import com.theandroidsuit.bytheway.activity.DetailActivity;
import com.theandroidsuit.bytheway.geofence.GeofenceManager;
import com.theandroidsuit.bytheway.geofence.PositionManager;
import com.theandroidsuit.bytheway.sql.databaseTable.PositionEntity;
import com.theandroidsuit.bytheway.sql.utils.DBHelper;
import com.theandroidsuit.bytheway.util.BTWUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Virginia Hernández on 19/01/15.
 */
public class PositionListAdapter extends ArrayAdapter<PositionEntity> implements Switch.OnCheckedChangeListener{

    public final String TAG = this.getClass().getName();

    private int resource;
    private LayoutInflater inflater;
    private Context context;
    private DBHelper mDBHelper;
    private String startingFlow;


    public PositionListAdapter(Context ctx, int resourceId, List<PositionEntity> objs, DBHelper helper, String strFlow) {

        super(ctx, resourceId, objs);
        resource = resourceId;
        inflater = LayoutInflater.from(ctx);
        context = ctx;
        mDBHelper = helper;
        startingFlow = strFlow;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {

        /* create a new view of my layout and inflate it in the row */
        convertView = (RelativeLayout) inflater.inflate(resource, null);

        final PositionEntity position = getItem(pos);

        TextView txtTitle = (TextView) convertView.findViewById(R.id.positionListItemTitle);
        txtTitle.setText(position.getTitle());

        txtTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO: Go to detail
                Intent intent = new Intent(context, DetailActivity.class);

                intent.putExtra(PositionManager.ID_POSITION_KEY, position.getId());
                intent.putExtra(BTWUtils.STARTING_FLOW, startingFlow);

                context.startActivity(intent);
            }
        });


        Switch swActive = (Switch) convertView.findViewById(R.id.positionListSwitchActive);
        swActive.setOnCheckedChangeListener(this);
        swActive.setTag(position);


        if(PositionManager.STATUS_ACTIVATED.equals(position.getStatus()))
            swActive.setChecked(true);
        else
            swActive.setChecked(false);

        return convertView;
    }

    @Override
    public PositionEntity getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        PositionEntity pos = (PositionEntity) buttonView.getTag();
        GeofenceManager geoManager = new GeofenceManager(context);

        if(isChecked){
            pos.setStatus(PositionManager.STATUS_ACTIVATED);
            geoManager.requestOneGeofence(PositionManager.translateToGeofence(pos).toGeofence());
        }else{
            pos.setStatus(PositionManager.STATUS_DEACTIVATE);
            geoManager.removeOneGeofence(PositionManager.translateToGeofence(pos).toGeofence());
        }

        try {
            // Executing action against database
            Dao dao = mDBHelper.getPositionDao();
            dao.update(pos);
        }catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }

    }


}