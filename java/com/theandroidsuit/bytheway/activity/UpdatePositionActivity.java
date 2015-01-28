package com.theandroidsuit.bytheway.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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
import com.theandroidsuit.bytheway.geofence.GeofenceManager;
import com.theandroidsuit.bytheway.geofence.PositionManager;
import com.theandroidsuit.bytheway.sql.databaseTable.PositionEntity;
import com.theandroidsuit.bytheway.sql.utils.DBHelper;
import com.theandroidsuit.bytheway.util.BTWUtils;

import java.sql.SQLException;


public class UpdatePositionActivity extends ActionBarActivity implements SeekBar.OnSeekBarChangeListener, GoogleMap.OnMarkerDragListener{

    public final String TAG = this.getClass().getName();

    private static final String ID_TO_UPDATE_KEY = "idToUpdate";

    // Objects to saveStatus
    private PositionEntity posOriginal = null;
    private Long idToUpdate = null;
    private GoogleMap mMap;
    private Circle circleSensitivity;
    private SeekBar sensitivity;
    private DBHelper mDBHelper;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_position);


        try {
            final String startingFlow = getIntent().getStringExtra(BTWUtils.STARTING_FLOW) == null ?
                    BTWUtils.LIST_POSITION_ACTIVITY : getIntent().getStringExtra(BTWUtils.STARTING_FLOW);

            // Restore Values
            if (null == idToUpdate)
                idToUpdate = getIntent().getExtras().getLong(PositionManager.ID_POSITION_KEY);

            // Get Original object
            if (null == posOriginal) {
                Dao dao = getHelper().getPositionDao();
                posOriginal = (PositionEntity) dao.queryForId(idToUpdate);
            }

            setUpMapIfNeeded();
            // Setting the listener
            sensitivity = (SeekBar) findViewById(R.id.itemEditSensitive);
            sensitivity.setOnSeekBarChangeListener(this);
            sensitivity.setProgress(40);


            // Actions for consolidate changes
            final PositionEntity oldValuePos = posOriginal;

            ImageView edit = (ImageView) findViewById(R.id.imageEdit);
            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PositionEntity pos = getPositionValuesFromView();

                    updatePosition(oldValuePos, pos);

                    Intent intent;

                    if (BTWUtils.LIST_POSITION_ACTIVITY.equals(startingFlow)) {
                        intent = new Intent(getApplicationContext(), ListPositionActivity.class);
                    }else{
                        intent = new Intent(getApplicationContext(), DetailActivity.class);
                    }

                    intent.putExtra(PositionManager.ID_POSITION_KEY, posOriginal.getId());

                    // No pasamos el iniciador xq volvemos al principio.
                    // intent.putExtra(BTWUtils.STARTING_FLOW,startingFlow);

                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    startActivity(intent);
                }
            });

            final ImageView delete = (ImageView) findViewById(R.id.imageDelete);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmDelete(oldValuePos);
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void confirmDelete(final PositionEntity oldValuePos){

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Delete this position?");

        alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do your work here

                deletePosition(oldValuePos);

                Intent intent = new Intent(getApplicationContext(), ListPositionActivity.class);
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


    private void deletePosition(PositionEntity pos){
        try {
            // Executing action against database
            Dao dao = getHelper().getPositionDao();
            dao.delete(pos);
        }catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void updatePosition(PositionEntity originalPos, PositionEntity newPos){

        if (!originalPos.getStatus().equals(newPos.getStatus())){
            // The status has change! This trigger an action in geofence
            GeofenceManager geoManager = new GeofenceManager(this);

            if(PositionManager.STATUS_ACTIVATED.equals(newPos.getStatus())) {
                // Activate new Geofence!
                PositionManager.addActiveGeofence(newPos);
                geoManager.requestOneGeofence(PositionManager.translateToGeofence(newPos).toGeofence());

            }else{
                // Eliminate old Geofence!
                PositionManager.removeActiveGeofence(originalPos);
                geoManager.removeOneGeofence(PositionManager.translateToGeofence(originalPos).toGeofence());

            }
        }

        // Setter uneditable fields in newPos object
        newPos.setId(originalPos.getId());

        try {
            // Executing action against database
            Dao dao = getHelper().getPositionDao();
            dao.update(newPos);
        }catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private PositionEntity getPositionValuesFromView() {

        // Getting references from view
        EditText title = (EditText) findViewById(R.id.itemEditTitle);
        EditText description = (EditText) findViewById(R.id.itemEditDescription);
        SeekBar sensitive = (SeekBar) findViewById(R.id.itemEditSensitive);
        Switch status = (Switch) findViewById(R.id.itemEditStatus);


        // Making new Position object
        PositionEntity pos = new PositionEntity();

        // Title
        pos.setTitle(title.getText().toString());

        // Description
        pos.setDescription(description.getText().toString());

        // Status
        if(status.isChecked())
            pos.setStatus(PositionManager.STATUS_ACTIVATED);
        else
            pos.setStatus(PositionManager.STATUS_DEACTIVATE);

        // Sensitivity
        Long radius = Long.valueOf(sensitive.getProgress());
        pos.setSensitive(radius);

        pos.setLatitude(marker.getPosition().latitude);
        pos.setLongitude(marker.getPosition().longitude);

        // Return new Position object
        return pos;
    }

    private void setViewValuesFromPosition(PositionEntity pos) {

        //Getting references from view
        EditText title = (EditText) findViewById(R.id.itemEditTitle);
        EditText description = (EditText) findViewById(R.id.itemEditDescription);
        Switch status = (Switch) findViewById(R.id.itemEditStatus);


        // Setting values from database object

        // Title
        title.setText(pos.getTitle());

        // Description
        description.setText(pos.getDescription());

        // Sensitivity
        sensitivity.setProgress((int) pos.getSensitive());

        // Status
        if (PositionManager.STATUS_ACTIVATED.equals(pos.getStatus()))
            status.setChecked(true);
        else
            status.setChecked(false);
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
    protected void onResume() {
        super.onResume();

        // Setting view values
        setViewValuesFromPosition(posOriginal);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(PositionManager.POSITION_KEY, posOriginal);
        outState.putLong(ID_TO_UPDATE_KEY,idToUpdate);

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
                mMap.setOnMarkerDragListener(this);
                addMarkerOnMap();
            }
        }
    }

    private void addMarkerOnMap() {
        // Creating a marker
        MarkerOptions markerOptions = new MarkerOptions();

        // Setting the position for the marker
        LatLng latLng = new LatLng(posOriginal.getLatitude(),posOriginal.getLongitude());

        markerOptions.position(latLng);

        // Setting the title for the marker.
        // This will be displayed on taping the marker
        markerOptions.title(posOriginal.getTitle());

        // Setting the marker Icon
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_blue));
        markerOptions.draggable(true);

        // Placing a marker on the touched position
        marker = mMap.addMarker(markerOptions);

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
                .radius(posOriginal.getSensitive()); // In meters

        // Get back the mutable Circle
        circleSensitivity = mMap.addCircle(circleOptions);

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_update_position, menu);
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

    @Override
    public void onMarkerDragStart(Marker marker) {
        circleSensitivity.remove();
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {


        // Instantiates a new CircleOptions object and defines the center and radius
        CircleOptions circleOptions = new CircleOptions()
                .center(marker.getPosition())
                .fillColor(Color.parseColor("#8881DAF5"))
                .strokeColor(Color.parseColor("#81BEF7"))
                .strokeWidth(1f)
                .radius(sensitivity.getProgress()); // In meters

        // Get back the mutable Circle
        circleSensitivity = mMap.addCircle(circleOptions);
    }
}
