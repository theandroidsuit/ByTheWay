package com.theandroidsuit.bytheway.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.theandroidsuit.bytheway.R;
import com.theandroidsuit.bytheway.error.BTWOperationError;
import com.theandroidsuit.bytheway.geofence.GeofenceManager;
import com.theandroidsuit.bytheway.geofence.PositionManager;
import com.theandroidsuit.bytheway.sql.databaseTable.PositionEntity;
import com.theandroidsuit.bytheway.sql.utils.DBHelper;
import com.theandroidsuit.bytheway.util.BTWUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;


public class AddPositionByMapActivity extends ActionBarActivity implements SeekBar.OnSeekBarChangeListener{

    public final String TAG = this.getClass().getName();
    private Location location = null;

    private GoogleMap mMap;
    private DBHelper mDBHelper;
    private Circle circleSensitivity;
    private SeekBar sensitivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_position);

        location = getIntent().getExtras().getParcelable(PositionManager.LOCATION_SELECTED_KEY);

        final String startingFlow = getIntent().getStringExtra(BTWUtils.STARTING_FLOW) == null ?
                BTWUtils.ADD_POSITION_ACTIVITY : getIntent().getStringExtra(BTWUtils.STARTING_FLOW);

        setUpMapIfNeeded();

        // Setting the listener
        sensitivity = (SeekBar) findViewById(R.id.itemEditSensitive);
        sensitivity.setOnSeekBarChangeListener(this);
        sensitivity.setProgress(GeofenceManager.DEFAULT_RADIUS);


        ImageView edit = (ImageView) findViewById(R.id.imageEdit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra(GeofenceManager.INIT_GEOFENCE_KEY, true);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                try {
                    addPosition();
                }catch (BTWOperationError oe){
                    intent.putExtra(BTWUtils.OPERATION_RESULT, oe.getDescription());
                }

                intent.putExtra(BTWUtils.STARTING_FLOW, startingFlow);

                startActivity(intent);
            }
        });

        final ImageView delete = (ImageView) findViewById(R.id.imageDelete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDelete();
            }
        });

    }

    private void confirmDelete(){

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Delete this position?");

        alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do your work here

                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                dialog.dismiss();

                startActivity(intent);

            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        alert.create().show();

    }


    private void addPosition()throws BTWOperationError{
        Dao dao;
        try {
            dao = getHelper().getPositionDao();
            PositionEntity position  = getPositionValuesFromView();
            dao.create(position);

            addNewGeofence(position);
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void addNewGeofence(PositionEntity position) {
        GeofenceManager geoManager = new GeofenceManager(getApplicationContext());
        geoManager.requestOneGeofence(PositionManager.translateToGeofence(position).toGeofence());
        PositionManager.addActiveGeofence(position);
    }

    private PositionEntity getPositionValuesFromView() throws BTWOperationError {
        PositionEntity pos = new PositionEntity();

        if (null != location) {
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();

            pos.setLatitude(latitude);
            pos.setLongitude(longitude);
        }else{
            BTWOperationError error = new BTWOperationError();
            error.setName("LocationError");
            error.setDescription(getString(R.string.no_location_error));
            throw error;
        }

        EditText title = (EditText) findViewById(R.id.itemEditTitle);
        EditText description = (EditText) findViewById(R.id.itemEditDescription);
        sensitivity = (SeekBar) findViewById(R.id.itemEditSensitive);
        Switch status = (Switch) findViewById(R.id.itemEditStatus);

        pos.setTitle(title.getText().toString());
        pos.setDescription(description.getText().toString());

        if(status.isChecked())
            pos.setStatus(PositionManager.STATUS_ACTIVATED);
        else
            pos.setStatus(PositionManager.STATUS_DEACTIVATE);

        Long radius = Long.valueOf(sensitivity.getProgress());
        pos.setSensitive(radius);

        return pos;
    }

    private void setUpMapIfNeeded() {
        Log.d(TAG, "setUpMapIfNeeded");

        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapPosition)).getMap();

            CameraUpdate zoom = CameraUpdateFactory.zoomTo(14);
            mMap.animateCamera(zoom);

            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                addMarkerOnMap();
            }
        }
    }

    private void addMarkerOnMap() {
        // Creating a marker
        MarkerOptions markerOptions = new MarkerOptions();

        // Setting the position for the marker
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

        markerOptions.position(latLng);

        // Setting the title for the marker.
        // This will be displayed on taping the marker
        //markerOptions.title(positionToShow.getTitle());

        // Setting the marker Icon
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_blue));

        // Placing a marker on the touched position
        Marker marker = mMap.addMarker(markerOptions);

        // Showing the current location in Google Map
        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(16.0f).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mMap.moveCamera(cameraUpdate);


        // Instantiates a new CircleOptions object and defines the center and radius
        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .fillColor(Color.parseColor("#8881DAF5"))
                .strokeColor(Color.parseColor("#81BEF7"))
                .strokeWidth(1f)
                .radius(GeofenceManager.DEFAULT_RADIUS); // In meters

        // Get back the mutable Circle
        circleSensitivity = mMap.addCircle(circleOptions);

    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");

        super.onPause();
    }

    private DBHelper getHelper() {
        if (mDBHelper == null) {
            mDBHelper = OpenHelperManager.getHelper(this, DBHelper.class);
        }
        return mDBHelper;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDBHelper != null) {
            OpenHelperManager.releaseHelper();
            mDBHelper = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_position, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        circleSensitivity.setRadius(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
