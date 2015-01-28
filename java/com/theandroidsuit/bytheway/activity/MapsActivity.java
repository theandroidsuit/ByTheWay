package com.theandroidsuit.bytheway.activity;

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

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import com.theandroidsuit.bytheway.R;
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

public class MapsActivity extends ActionBarActivity implements LocationListener {

    private static final String INITIALIZE_ACTIVITY_GEOFENCE_KEY = "initialize_activity_geofence_key";
    public final String TAG = this.getClass().getName();

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Map<String, Long> markerMap = null; // Map to store relation between marker and positionId

    private boolean firstTimePosition = true;
    private static Location currentLocation;

    private DBHelper mDBHelper;

    private ArrayList<PositionEntity> valuesActive = null;

    private Boolean changeGeofences = true;
    private Boolean initialize = false;

    private GeofenceManager geoManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Initialize map for relationship between: PositionEntry <--> Marker
        markerMap = new HashMap<>();

        // Setup Map
        setUpMapIfNeeded();

        // Setup Geofence System
        geoManager = new GeofenceManager(this);
        geoManager.setupGeofenceSystem();

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

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        initialize = savedInstanceState.getBoolean(INITIALIZE_ACTIVITY_GEOFENCE_KEY);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(INITIALIZE_ACTIVITY_GEOFENCE_KEY, initialize);
    }

    /******************** ACTIVITY LIVE CICLE **********************/
    @Override
    protected void onResume() {
        Log.d(TAG,"onResume");

        super.onResume();


        // Why here? Because, I need to update data after possible updatings
        changeGeofences = getIntent().getBooleanExtra(GeofenceManager.INIT_GEOFENCE_KEY, false);

        try {
            // Retrieve from database
            Dao dao = getHelper().getPositionDao();

            valuesActive = (ArrayList<PositionEntity>) dao.queryForEq(PositionEntity.COLUMN_STATUS, PositionManager.STATUS_ACTIVATED);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        setUpMapIfNeeded();

        putPositionsAtMap();
        setInfoWindowListeners();

        if (!initialize && !changeGeofences ) {

            PositionManager.setGeofencesToActivate(valuesActive);

            // Register active geofences
            geoManager.requestGeofences();

            initialize = true;
        }

        String or = getIntent().getStringExtra(BTWUtils.OPERATION_RESULT);
        if(null != or && !or.isEmpty()){
            Toast.makeText(this, or, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        // Unregister the broadcast receiver
        geoManager.releaseReceiver();

        if (mDBHelper != null) {
            OpenHelperManager.releaseHelper();
            mDBHelper = null;
        }

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
                intent = new Intent(MapsActivity.this, ListPositionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            case R.id.action_add_position_by_form:
                intent = new Intent(MapsActivity.this, AddPositionByFormActivity.class);
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


    /******************** SUPPORT METHODS **********************/
    private void putPositionsAtMap() {
        // Set Positions at Map
        mMap.clear();
        if (null != valuesActive && 0 < valuesActive.size()){

            // Add positions to map
            setAllMarkerAtMap(valuesActive);
        }
    }

    private void setAllMarkerAtMap(List<PositionEntity> values) {
        for(PositionEntity item: values){
            addMarkerOnMap(item);
        }
    }

    private DBHelper getHelper() {
        if (mDBHelper == null) {
            mDBHelper = OpenHelperManager.getHelper(this, DBHelper.class);
        }
        return mDBHelper;
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setMyLocationOnMap()}  once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        Log.d(TAG, "setUpMapIfNeeded");

        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

            CameraUpdate zoom = CameraUpdateFactory.zoomTo(14);
            mMap.animateCamera(zoom);

            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setMyLocationOnMap();
            }
        }
    }


    private void setMyLocationOnMap() {
        // Getting LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Getting the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        // Getting Current Location from GPS
        currentLocation = locationManager.getLastKnownLocation(provider);

        if(currentLocation != null){
            onLocationChanged(currentLocation);
        }

        locationManager.requestLocationUpdates(provider, 5000, 0, this);


        // Enabling MyLocation Layer of Google Map
        mMap.setMyLocationEnabled(true);

    }

    private void addMarkerOnMap(PositionEntity pos) {
        // Creating a marker
        MarkerOptions markerOptions = new MarkerOptions();

        // Setting the position for the marker
        LatLng latLng = new LatLng(pos.getLatitude(),pos.getLongitude());

        markerOptions.position(latLng);

        // Setting the title for the marker.
        // This will be displayed on taping the marker
        markerOptions.title(pos.getTitle());

        // Setting the marker Icon
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_blue));

        // Placing a marker on the touched position
        Marker marker = mMap.addMarker(markerOptions);

        markerMap.put(marker.getId(), pos.getId());
    }

    public static Location getCurrentLocation (){
        return currentLocation;
    }

    private void setInfoWindowListeners() {
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                Long idPosition = markerMap.get(marker.getId());
                Intent intent = new Intent(getApplicationContext(), DetailActivity.class);

                // Passing data as a parecelable object to FallaInfoActivity
                intent.putExtra(PositionManager.ID_POSITION_KEY, idPosition);

                startActivity(intent);
            }
        });
    }

    /* Interface Methods */

    @Override
    public void onLocationChanged(Location location) {

        // Getting latitude of the current location
        Double currentPositionLat = Double.valueOf(location.getLatitude());

        // Getting longitude of the current location
        Double currentPositionLong = Double.valueOf(location.getLongitude());

        // Creating a LatLng object for the current location
        LatLng latLng = new LatLng(currentPositionLat, currentPositionLong);

        // Showing the current location in Google Map
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        if (firstTimePosition){
            CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(14.0f).build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            mMap.moveCamera(cameraUpdate);

            firstTimePosition = false;
        }
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

}
