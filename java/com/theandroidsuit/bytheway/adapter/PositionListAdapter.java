package com.theandroidsuit.bytheway.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.theandroidsuit.bytheway.R;
import com.theandroidsuit.bytheway.activity.DetailActivity;
import com.theandroidsuit.bytheway.activity.ListPositionActivity;
import com.theandroidsuit.bytheway.geofence.GeofenceManager;
import com.theandroidsuit.bytheway.geofence.PositionManager;
import com.theandroidsuit.bytheway.sql.databaseTable.Position;
import com.theandroidsuit.bytheway.sql.utils.DBHelper;
import com.theandroidsuit.bytheway.util.BTWUtils;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Virginia Hernández on 19/01/15.
 */
public class PositionListAdapter extends BaseAdapter implements Switch.OnCheckedChangeListener{

    public final String TAG = this.getClass().getName();

    public static final int POSITION_UPDATED = 3;

    private int resource;
    private LayoutInflater inflater;
    private Context context;
    private DBHelper mDBHelper;
    private String startingFlow;
    private List<Position> data;
  //  private ListPositionActivity callback;

    public PositionListAdapter(Context ctx, int resourceId, List<Position> objs, DBHelper helper, String strFlow) {
        resource = resourceId;
        inflater = LayoutInflater.from(ctx);
        context = ctx;
    //    callback = (ListPositionActivity) ctx;
        data = objs;
        mDBHelper = helper;
        startingFlow = strFlow;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {

        /* create a new view of my layout and inflate it in the row */
        convertView = (RelativeLayout) inflater.inflate(resource, null);

        final Position position = getItem(pos);

        TextView txtTitle = (TextView) convertView.findViewById(R.id.positionListItemTitle);
        txtTitle.setText(position.getTitle());

        txtTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Go to detail
                Intent intent = new Intent(context, DetailActivity.class);

                intent.putExtra(PositionManager.ID_POSITION_KEY, position.getId());
                intent.putExtra(BTWUtils.STARTING_FLOW, startingFlow);

                ((Activity)context).startActivityForResult(intent, PositionListAdapter.POSITION_UPDATED);
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

    public void updateAdapterData(List<Position> newlist) {
        data.clear();
        data.addAll(newlist);
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (null == data)
            return 0;

        return data.size();
    }

    @Override
    public Position getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).getId();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Position pos = (Position) buttonView.getTag();
        GeofenceManager geoManager = new GeofenceManager(context);

        boolean hasChanged = hasChangeStatus(pos.getStatus(), isChecked);

        if(hasChanged && isChecked){
            pos.setStatus(PositionManager.STATUS_ACTIVATED);
            geoManager.requestOneGeofence(PositionManager.translateToGeofence(pos).toGeofence());
        }else if(hasChanged && !isChecked){
            pos.setStatus(PositionManager.STATUS_DEACTIVATE);
            geoManager.removeOneGeofence(PositionManager.translateToGeofence(pos).toGeofence());
        }

        if (hasChanged) {
            try {
                // Executing action against database
                Dao dao = mDBHelper.getPositionDao();
                dao.update(pos);
            } catch (SQLException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private boolean hasChangeStatus(String status, boolean isChecked) {
        boolean changed = false;

        if(PositionManager.STATUS_ACTIVATED.equals(status) && !isChecked){
            changed = true;
        }

        if(PositionManager.STATUS_DEACTIVATE.equals(status) && isChecked){
            changed = true;
        }

        return changed;
    }


}