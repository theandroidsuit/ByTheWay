package com.theandroidsuit.bytheway.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.theandroidsuit.bytheway.R;
import com.theandroidsuit.bytheway.activity.DetailActivity;
import com.theandroidsuit.bytheway.geofence.GeofenceManager;
import com.theandroidsuit.bytheway.geofence.PositionManager;
import com.theandroidsuit.bytheway.sql.databaseTable.Position;
import com.theandroidsuit.bytheway.sql.utils.DBHelper;
import com.theandroidsuit.bytheway.util.BTWUtils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Virginia Hernández on 19/01/15.
 */
public class PositionByCategoryListAdapter extends BaseExpandableListAdapter implements Switch.OnCheckedChangeListener{

    public final String TAG = this.getClass().getName();
    public static final int POSITION_UPDATED_EXT = 4;


    //private int resource;
    private LayoutInflater inflater;
    private Context context;
    private DBHelper mDBHelper;
    private String startingFlow;

    private List<String> listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, List<Position>> listDataChild;


    public PositionByCategoryListAdapter(Context ctx, DBHelper helper, List<String> headers, HashMap<String, List<Position>> data, String strFlow) {

        inflater = LayoutInflater.from(ctx);
        context = ctx;
        mDBHelper = helper;
        startingFlow = strFlow;
        listDataHeader = headers;
        listDataChild = data;
    }


    public void updateAdapterData(List<String> newlistHeaders, HashMap<String, List<Position>> newListData) {
        listDataHeader.clear();
        listDataHeader.addAll(newlistHeaders);

        listDataChild.clear();
        listDataChild.putAll(newListData);

        this.notifyDataSetChanged();
    }


    @Override
    public int getGroupCount() {
        return listDataHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listDataHeader.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_list_category, null);
        }

        TextView lblListHeader = (TextView) convertView.findViewById(R.id.categoryListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        return convertView;

    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
                /* create a new view of my layout and inflate it in the row */
        convertView = (RelativeLayout) inflater.inflate(R.layout.item_list_position, null);

        final Position position = (Position) getChild(groupPosition, childPosition);

        TextView txtTitle = (TextView) convertView.findViewById(R.id.positionListItemTitle);
        txtTitle.setText(position.getTitle());

        txtTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO: Go to detail
                Intent intent = new Intent(context, DetailActivity.class);


                intent.putExtra(BTWUtils.STARTING_FLOW, startingFlow);
                intent.putExtra(PositionManager.ID_POSITION_KEY, position.getId());

                ((Activity)context).startActivityForResult(intent, PositionByCategoryListAdapter.POSITION_UPDATED_EXT);
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
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
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