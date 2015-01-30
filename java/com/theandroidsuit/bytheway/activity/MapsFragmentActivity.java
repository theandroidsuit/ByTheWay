package com.theandroidsuit.bytheway.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.theandroidsuit.bytheway.R;
import com.theandroidsuit.bytheway.fragment.GoogleMapFragment;
import com.theandroidsuit.bytheway.geofence.GeofenceManager;
import com.theandroidsuit.bytheway.geofence.PositionManager;
import com.theandroidsuit.bytheway.sql.databaseTable.PositionEntity;
import com.theandroidsuit.bytheway.sql.utils.DBHelper;
import com.theandroidsuit.bytheway.util.BTWUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Virginia Hernández on 14/01/15.
 */

public class MapsFragmentActivity extends ActionBarActivity {

    private static final String INITIALIZE_ACTIVITY_GEOFENCE_KEY = "initialize_activity_geofence_key";
    public final String TAG = this.getClass().getName();
    private GoogleMapFragment mTaskFragment;

    private static final String TAG_MAP_FRAGMENT = "map_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_maps);

        FragmentManager fm = getFragmentManager();
        mTaskFragment = (GoogleMapFragment) fm.findFragmentByTag(TAG_MAP_FRAGMENT);



        if (mTaskFragment == null) {
            mTaskFragment = new GoogleMapFragment();
            fm.beginTransaction().add(R.id.mapFragment, mTaskFragment, TAG_MAP_FRAGMENT).commit();
        }


        // Setting action to button ADD POSITION (+)
        ImageView addPosition = (ImageView) findViewById(R.id.addPosition);
        addPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddCurrentPositionActivity.class);
                startActivity(intent);
            }
        });

    }

/*    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        initialize = savedInstanceState.getBoolean(INITIALIZE_ACTIVITY_GEOFENCE_KEY);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(INITIALIZE_ACTIVITY_GEOFENCE_KEY, initialize);
    }*/

    /******************** ACTIVITY LIVE CICLE **********************/
    @Override
    protected void onResume() {
        Log.d(TAG,"onResume");

        super.onResume();

        String or = getIntent().getStringExtra(BTWUtils.OPERATION_RESULT);
        if(null != or && !or.isEmpty()){
            Toast.makeText(this, or, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in And	Intent intent;
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_all_positions:
                intent = new Intent(MapsFragmentActivity.this, ListPositionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            case R.id.action_add_position_by_form:
                intent = new Intent(MapsFragmentActivity.this, AddPositionByFormActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
           /* case R.id.action_add_position_by_map:
                intent = new Intent(MapsActivity.this, ChoosePositionByMapActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;*/
            default:
                break;
        }

        return true;

    }







}
