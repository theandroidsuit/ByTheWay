package com.theandroidsuit.bytheway.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.theandroidsuit.bytheway.R;
import com.theandroidsuit.bytheway.activity.DetailActivity;
import com.theandroidsuit.bytheway.geofence.GeofenceManager;
import com.theandroidsuit.bytheway.geofence.PositionManager;
import com.theandroidsuit.bytheway.sql.databaseTable.PositionEntity;
import com.theandroidsuit.bytheway.sql.utils.DBHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Virginia Hernández on 29/01/15.
 */
public class GoogleMapFragment extends MapFragment implements LocationListener{


    private static View view;
    /**
     * Note that this may be null if the Google Play services APK is not
     * available.
     */

    private static GoogleMap mMap;
    private Map<String, Long> markerMap = null; // Map to store relation between marker and positionId

    private boolean firstTimePosition = true;
    private static Location currentLocation;

    private DBHelper mDBHelper;

    private ArrayList<PositionEntity> valuesActive = null;

    private Boolean changeGeofences = true;
    private Boolean initialize = false;


    private GeofenceManager geoManager;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        markerMap = new HashMap<>();

        // Setup Geofence System
        geoManager = new GeofenceManager(getActivity());
        geoManager.setupGeofenceSystem();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setRetainInstance(true);
        //view = super.onCreateView(inflater, container, savedInstanceState);
        view = (RelativeLayout) inflater.inflate(R.layout.activity_maps, container, false);
        // Initialize map for relationship between: PositionEntry <--> Marker

        return view;

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void initializeMap(){
        setUpMapIfNeeded(); // For setting up the MapFragment

        // Why here? Because, I need to update data after possible updatings
        changeGeofences = getActivity().getIntent().getBooleanExtra(GeofenceManager.INIT_GEOFENCE_KEY, false);

        try {
            // Retrieve from database
            Dao dao = getHelper().getPositionDao();

            valuesActive = (ArrayList<PositionEntity>) dao.queryForEq(PositionEntity.COLUMN_STATUS, PositionManager.STATUS_ACTIVATED);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        putPositionsAtMap();
        setInfoWindowListeners();


        if (!initialize && !changeGeofences ) {

            PositionManager.setGeofencesToActivate(valuesActive);

            // Register active geofences
            geoManager.requestGeofences();

            initialize = true;
        }


    }

    private void setUpMapIfNeeded() {

        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = getMap(); ///((GoogleMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap();

            CameraUpdate zoom = CameraUpdateFactory.zoomTo(14);
            mMap.animateCamera(zoom);

            // Check if we were successful in obtaining the map.
        }

        if (mMap != null) {
            setMyLocationOnMap();
        }
    }



    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        initializeMap();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDetach() {

        // Unregister the broadcast receiver
        geoManager.releaseReceiver();

        if (mDBHelper != null) {
            OpenHelperManager.releaseHelper();
            mDBHelper = null;
        }

        super.onDetach();

    }

    /******************** SUPPORT METHODS **********************/

    private DBHelper getHelper() {
        if (mDBHelper == null) {
            mDBHelper = OpenHelperManager.getHelper(getActivity(), DBHelper.class);
        }
        return mDBHelper;
    }

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


    private void setMyLocationOnMap() {
        // Getting LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);

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
                Intent intent = new Intent(getActivity(), DetailActivity.class);

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
