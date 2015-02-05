package com.theandroidsuit.bytheway.activity;

import android.app.Activity;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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

public class ChoosePositionByMapActivity extends FragmentActivity implements LocationListener, GoogleMap.OnMarkerDragListener {

    public final String TAG = this.getClass().getName();

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private boolean firstTimePosition = true;
    private Location location = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG,"onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Setting action to button ADD POSITION (+)
        ImageView addPosition = (ImageView) findViewById(R.id.addPosition);
        addPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (location != null) {
                    Intent intent = new Intent(getApplicationContext(), AddPositionByMapActivity.class);
                    intent.putExtra(PositionManager.LOCATION_SELECTED_KEY, location);
                    startActivity(intent);
                }else{
                    // Toast for invalid data
                    Toast.makeText(getApplicationContext(), R.string.invalid_location, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /******************** ACTIVITY LIVE CICLE **********************/
    @Override
    protected void onResume() {
        Log.d(TAG,"onResume");

        super.onResume();

        setUpMapIfNeeded();

        if (null != location) {
            putCurrentPositionAtMap();
        }

   }


    /******************** SUPPORT METHODS **********************/
    private void putCurrentPositionAtMap() {
        // Set Positions at Map
        mMap.clear();
        if (null != location){

            // Add positions to map
            PositionEntity position = createPositionEntity(location);
            setMarkerAtMap(position);
        }
    }

    private PositionEntity createPositionEntity(Location currentLocation) {
        PositionEntity positionEntity = new PositionEntity();

        positionEntity.setLatitude(currentLocation.getLatitude());
        positionEntity.setLongitude(currentLocation.getLongitude());

        return positionEntity;
    }

    private void setMarkerAtMap(PositionEntity currentLocation) {
            addMarkerOnMap(currentLocation);
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setMyLocationOnMap()}  once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link com.google.android.gms.maps.SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(android.os.Bundle)} may not be called again so we should call this
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
                mMap.setOnMarkerDragListener(this);
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
        location = locationManager.getLastKnownLocation(provider);

        if(location != null){
            onLocationChanged(location);
        }
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
        markerOptions.draggable(true);

        // Placing a marker on the touched position
        mMap.addMarker(markerOptions);


    }


    @Override
    public void onLocationChanged(Location location) {

        // Creating a LatLng object for the current location
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        // Showing the current location in Google Map
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        if (firstTimePosition){
            CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(14.0f).build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            mMap.moveCamera(cameraUpdate);

            firstTimePosition = false;
        }
    }

    /* Interface Methods */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
    }

    @Override
    public void onMarkerDrag(Marker marker) {
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng latLng = marker.getPosition();

        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
    }
}
