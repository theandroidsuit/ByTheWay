package com.theandroidsuit.bytheway.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

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
import com.theandroidsuit.bytheway.sql.databaseTable.Category;
import com.theandroidsuit.bytheway.sql.databaseTable.CategoryPosition;
import com.theandroidsuit.bytheway.sql.databaseTable.Position;
import com.theandroidsuit.bytheway.sql.utils.DBHelper;
import com.theandroidsuit.bytheway.util.BTWUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class AddPositionByFormActivity extends ActionBarActivity implements SeekBar.OnSeekBarChangeListener, GoogleMap.OnMarkerDragListener{


    public final String TAG = this.getClass().getName();

    private DBHelper mDBHelper;

    static ArrayAdapter<String> dataAdapter;
    private static final int CATEGORY_ADDED = 1;

    private GoogleMap mMap;
    private SeekBar sensitivity;

    private static Circle circleSensitivity;
    private static Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_position_by_form2);

        final String startingFlow = getIntent().getStringExtra(BTWUtils.STARTING_FLOW) == null ?
                BTWUtils.ADD_POSITION_ACTIVITY : getIntent().getStringExtra(BTWUtils.STARTING_FLOW);


        setUpMapIfNeeded();
        setupListeners();
        setUpCategory();

        if (null == savedInstanceState) // Solo la primera vez
            putStuffOnView();

        ImageView add = (ImageView) findViewById(R.id.imageEdit);
        add.setOnClickListener(new View.OnClickListener() {
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

        ImageView search = (ImageView) findViewById(R.id.imageSearch);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchAddress();
            }
        });

        ImageView addCategory = (ImageView) findViewById(R.id.addCategory);
        addCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddCategoryActivity.class);
                startActivityForResult(intent, CATEGORY_ADDED);
            }
        });
    }

    private void putStuffOnView() {
        // Use this method to put some stuff into view, only first time
        // Check if we were successful in obtaining the map.
        if (mMap != null) {
            try {
                addMarkerOnMap(null, null);
            }catch (BTWOperationError e){  }
        }

        sensitivity.setProgress(40);
    }

    private void setupListeners() {
        // Setting the listener
        mMap.setOnMarkerDragListener(this);
        sensitivity = (SeekBar) findViewById(R.id.itemEditSensitive);
        sensitivity.setOnSeekBarChangeListener(this);
    }

    private void searchAddress(){
        EditText locationDesc = (EditText) findViewById(R.id.itemEditAddress);
        String addressStr = locationDesc.getText().toString();
        if(null == addressStr || addressStr.isEmpty()){
            // Toast for invalid data
            Toast.makeText(getApplicationContext(), R.string.invalid_address, Toast.LENGTH_LONG).show();
            return;
        }

        try {
            Address address = getlocationByAddressString(addressStr);
            if (null != address) {
                addMarkerOnMap(address, Integer.valueOf(sensitivity.getProgress()));
            }

        }catch (IOException e){

        }catch (BTWOperationError bto){

        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CATEGORY_ADDED) {
            if (resultCode == RESULT_OK) {

                String newCategoryName = data.getStringExtra("newCategoryName");
                setUpCategory();

                // Select new Category in spinner
                if (!newCategoryName.equals(null)) {
                    int spinnerPosition = dataAdapter.getPosition(newCategoryName);
                    Spinner spinnerCat = (Spinner) findViewById(R.id.itemEditCategory);

                    spinnerCat.setSelection(spinnerPosition);
                }

                // Notify the change! (I think this is not necessary 'cause I've created the adapter)
                dataAdapter.notifyDataSetChanged();

                Toast.makeText(getApplicationContext(), R.string.category_added, Toast.LENGTH_SHORT);
            }
        }

    }

    private void setUpCategory() {

        Spinner spinnerCat = (Spinner) findViewById(R.id.itemEditCategory);
        List<String> list = new ArrayList<>();

        Dao dao;
        try {
            dao = getHelper().getCategoryDao();
            List<Category> listCategory = dao.queryForAll();
            if (null != listCategory && !listCategory.isEmpty()){
                for(Category item: listCategory) {
                    list.add(item.getTitle());
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }


        dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerCat.setAdapter(dataAdapter);
    }

    private void addPosition()throws BTWOperationError{
        Dao dao;
        try {

            // Create Position
            dao = getHelper().getPositionDao();
            Position position  = getPositionValuesFromView();
            dao.create(position);

            // Getting Category
            dao = getHelper().getCategoryDao();
            Spinner spinner = (Spinner)findViewById(R.id.itemEditCategory);

            String catName = spinner.getSelectedItem().toString();
            List<Category> catList = dao.queryForEq(Category.COLUMN_TITLE,catName);
            Category cat = null;
            if (null != catList && !catList.isEmpty()){
                cat = catList.get(0);
            }

            // Create Category - Position Relationship
            CategoryPosition catPos = new CategoryPosition();
            catPos.setCategory(cat);
            catPos.setPosition(position);
//            catPos.setPositionId(position.getId());
//            if (null != cat){
//                catPos.setCategoryId(cat.getId());
//            }else{
//                catPos.setCategoryId(1l); // Category by Default
//            }

            dao = getHelper().getCategoryPositionDao();
            dao.create(catPos);


            addNewGeofence(position);
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void addNewGeofence(Position position) {
        GeofenceManager geoManager = new GeofenceManager(getApplicationContext());
        geoManager.requestOneGeofence(PositionManager.translateToGeofence(position).toGeofence());
        PositionManager.addActiveGeofence(position);
    }


    private Position getPositionValuesFromView() throws BTWOperationError {
        Position pos = new Position();


        EditText locationDesc = (EditText) findViewById(R.id.itemEditAddress);
        String addressStr = locationDesc.getText().toString();

        try {
            if (null != marker){
                pos.setLatitude(marker.getPosition().latitude);
                pos.setLongitude(marker.getPosition().longitude);
            } else { //Dudo de este else, debería eliminarlo
                Address location = getlocationByAddressString(addressStr);

                if (null != location) {
                    double longitude = location.getLongitude();
                    double latitude = location.getLatitude();
                    pos.setLatitude(latitude);
                    pos.setLongitude(longitude);
                }

            }

        }catch (IOException e){
            BTWOperationError error = new BTWOperationError();
            error.setName("LocationError");
            error.setDescription(getString(R.string.no_location_error));
            throw error;
        }

        EditText title = (EditText) findViewById(R.id.itemEditTitle);
        EditText description = (EditText) findViewById(R.id.itemEditDescription);
        SeekBar sensitive = (SeekBar) findViewById(R.id.itemEditSensitive);
        Switch status = (Switch) findViewById(R.id.itemEditStatus);

        pos.setTitle(title.getText().toString());
        pos.setDescription(description.getText().toString());

        if(status.isChecked())
            pos.setStatus(PositionManager.STATUS_ACTIVATED);
        else
            pos.setStatus(PositionManager.STATUS_DEACTIVATE);

        Long radius = Long.valueOf(sensitive.getProgress());
        pos.setSensitive(radius);

        return pos;
    }

    private Address getlocationByAddressString(String addressStr) throws IOException{
        Geocoder coder = new Geocoder(this);
        List<Address> address = coder.getFromLocationName(addressStr, 5);
        if (address == null) {
            Toast.makeText(getApplicationContext(), R.string.invalid_address, Toast.LENGTH_LONG).show();
            return null;
        }
        return address.get(0);
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

        //sensitivity.setOnSeekBarChangeListener(null);
    }


    private void setUpMapIfNeeded() {
        Log.d(TAG, "setUpMapIfNeeded");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapPosition);

        mapFragment.setRetainInstance(true);

        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = mapFragment.getMap();

            CameraUpdate zoom = CameraUpdateFactory.zoomTo(14);
            mMap.animateCamera(zoom);
        }
    }


    private void addMarkerOnMap(Address address, Integer radius) throws BTWOperationError{

        Double latitude;
        Double longitude;
        if(null == address) {
            Location location = getCurrentLocation();
            latitude = location.getLatitude();
            longitude = location.getLongitude();

        }else {
            latitude = address.getLatitude();
            longitude = address.getLongitude();
        }

        int sensitInt = PositionManager.DEFAULT_RADIUS;
        if(null != radius)
            sensitInt = radius.intValue();

        mMap.clear();
        // Creating a marker
        MarkerOptions markerOptions = new MarkerOptions();

        // Setting the position for the marker
        LatLng latLng = new LatLng(latitude,longitude);

        markerOptions.position(latLng);

        // Setting the title for the marker.
        // This will be displayed on taping the marker
        //markerOptions.title(positionToShow.getTitle());

        // Setting the marker Icon
        markerOptions.icon(BitmapDescriptorFactory.fromResource(PositionManager.MARKER_IMAGE_RESOURCE));
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
                .fillColor(Color.parseColor(PositionManager.SENSIVILITY_FILL_COLOR))
                .strokeColor(Color.parseColor(PositionManager.SENSIVILITY_BORDER_COLOR))
                .strokeWidth(1f)
                .radius(sensitInt); // In meters

        // Get back the mutable Circle
        circleSensitivity = mMap.addCircle(circleOptions);

    }

    private Location getCurrentLocation() throws BTWOperationError {
        Location location = MapsActivity.getCurrentLocation();

        if (null == location) {

            BTWOperationError error = new BTWOperationError();
            error.setName("LocationError");
            error.setDescription(getString(R.string.no_location_error));
            throw error;
        }

        return location;
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
    public void onMarkerDragEnd(Marker newMarker) {
        // Instantiates a new CircleOptions object and defines the center and radius

        marker.remove();

        // Creating a marker
        MarkerOptions markerOptions = new MarkerOptions();

        // Setting the position for the marker
        markerOptions.position(newMarker.getPosition());

        // Setting the marker Icon
        markerOptions.icon(BitmapDescriptorFactory.fromResource(PositionManager.MARKER_IMAGE_RESOURCE));
        markerOptions.draggable(true);

        // Placing a marker on the touched position
        marker = mMap.addMarker(markerOptions);

        CircleOptions circleOptions = new CircleOptions()
                .center(newMarker.getPosition())
                .fillColor(Color.parseColor(PositionManager.SENSIVILITY_FILL_COLOR))
                .strokeColor(Color.parseColor(PositionManager.SENSIVILITY_BORDER_COLOR))
                .strokeWidth(1f)
                .radius(sensitivity.getProgress()); // In meters

        // Get back the mutable Circle
        circleSensitivity.remove();
        circleSensitivity = mMap.addCircle(circleOptions);
    }
}
