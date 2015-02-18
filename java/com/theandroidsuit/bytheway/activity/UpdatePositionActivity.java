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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
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
import com.theandroidsuit.bytheway.sql.databaseTable.Category;
import com.theandroidsuit.bytheway.sql.databaseTable.CategoryPosition;
import com.theandroidsuit.bytheway.sql.databaseTable.Position;
import com.theandroidsuit.bytheway.sql.utils.DBHelper;
import com.theandroidsuit.bytheway.util.BTWUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class UpdatePositionActivity extends ActionBarActivity implements SeekBar.OnSeekBarChangeListener, GoogleMap.OnMarkerDragListener{

    public final String TAG = this.getClass().getName();

    private static final String ID_TO_UPDATE_KEY = "idToUpdate";

    private Position posEdited = null;
    private Category catEdited = null;

    private DBHelper mDBHelper;
    private Long idToUpdate = null;

    static ArrayAdapter<String> dataAdapter;
    private static final int CATEGORY_ADDED = 1;

    private GoogleMap mMap;
    private SeekBar sensitivity;

    private static Circle circleSensitivity;
    private static Marker marker;

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

            // Actions for consolidate changes
            Dao dao = getHelper().getPositionDao();
            final Position oldValuePos = (Position) dao.queryForId(idToUpdate);

            // Get Original object
            if (null == posEdited) {
                posEdited = oldValuePos;
            }


            setUpMapIfNeeded();
            setupListeners();
            setUpCategory();

            catEdited = getCategoryByPosition(posEdited);
            setSelectedCategorySpinner(catEdited.getTitle());

            if (null == savedInstanceState) // Solo la primera vez
                putStuffOnMap();

            ImageView edit = (ImageView) findViewById(R.id.imageEdit);
            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Position pos = getPositionValuesFromView();

                    updatePosition(oldValuePos, pos);

                    Intent intent;

                    if (BTWUtils.LIST_POSITION_ACTIVITY.equals(startingFlow)) {
                        intent = new Intent(getApplicationContext(), ListPositionActivity.class);
                    }else{
                        intent = new Intent(getApplicationContext(), DetailActivity.class);
                    }

                    intent.putExtra(PositionManager.ID_POSITION_KEY, posEdited.getId());
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

            ImageView addCategory = (ImageView) findViewById(R.id.addCategory);
            addCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), AddCategoryActivity.class);
                    startActivityForResult(intent, CATEGORY_ADDED);
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private void confirmDelete(final Position oldValuePos){

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


    private void deletePosition(Position pos){
        try {
            // Executing action against database
            Dao dao = getHelper().getPositionDao();
            dao.delete(pos);
        }catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void updatePosition(Position originalPos, Position newPos){

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


            // Getting New Category
            Category cat = getCategoryFromView();

            // Getting Old Category - Position Relationship (if exist)
            CategoryPosition catPosOld = getCategoryPositionByPosition(newPos);

            // Update/Create Category - Position Relationship
            dao = getHelper().getCategoryPositionDao();
            if (null != catPosOld){

                catPosOld.setCategory(cat);
                //catPosOld.setCategoryId(cat.getId());
                dao.update(catPosOld);

            }else {
                CategoryPosition catPos = new CategoryPosition();
                catPos.setCategory(cat);
                catPos.setPosition(newPos);
//                catPos.setPositionId(newPos.getId());
//                if (null != cat){
//                    catPos.setCategoryId(cat.getId());
//                }else{
//                    catPos.setCategoryId(0l);
//                }

                dao.create(catPos);
            }


        }catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private Category getCategoryFromView()throws SQLException{
        Category cat = null;

        Dao dao = getHelper().getCategoryDao();
        Spinner spinner = (Spinner)findViewById(R.id.itemEditCategory);

        String catName = spinner.getSelectedItem().toString();
        List<Category> catList = dao.queryForEq(Category.COLUMN_TITLE,catName);
        if (null != catList && !catList.isEmpty()){
            cat = catList.get(0);
        }

        return cat;
    }

    private CategoryPosition getCategoryPositionByPosition(Position position) throws SQLException {
        Dao dao = getHelper().getCategoryPositionDao();
        List<CategoryPosition> catPosList = dao.queryForEq(CategoryPosition.COLUMN_ID_POSITION, position.getId());

        CategoryPosition catPos = null;
        if (null != catPosList && !catPosList.isEmpty()){
            catPos = catPosList.get(0);
        }
        return catPos;
    }

    private Category getCategoryByPosition(Position position) throws SQLException {
        Category cat = null;
        CategoryPosition catPos = getCategoryPositionByPosition(position);

        if (null != catPos){
            Dao dao = getHelper().getCategoryDao();
            cat = (Category) dao.queryForId(catPos.getCategory().getId());
        }

        return cat;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CATEGORY_ADDED) {
            if (resultCode == RESULT_OK) {

                String newCategoryName = data.getStringExtra("newCategoryName");
                setUpCategory();

                // Select new Category in spinner
                if (!newCategoryName.equals(null)) {
                    setSelectedCategorySpinner(newCategoryName);
                }

                // Notify the change! (I think this is not necessary 'cause I've created the adapter)
                dataAdapter.notifyDataSetChanged();
            }
        }

    }

    private void setSelectedCategorySpinner(String categoryName) {
        int spinnerPosition = dataAdapter.getPosition(categoryName);
        Spinner spinnerCat = (Spinner) findViewById(R.id.itemEditCategory);

        spinnerCat.setSelection(spinnerPosition);
    }


    private Position getPositionValuesFromView() {

        // Getting references from view
        EditText title = (EditText) findViewById(R.id.itemEditTitle);
        EditText description = (EditText) findViewById(R.id.itemEditDescription);
        SeekBar sensitive = (SeekBar) findViewById(R.id.itemEditSensitive);
        Switch status = (Switch) findViewById(R.id.itemEditStatus);


        // Making new Position object
        Position pos = new Position();

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



    private void setViewValuesFromPosition(Position pos) {

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
        setViewValuesFromPosition(posEdited);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Position pos = getPositionValuesFromView();
        try {
            Category cat = getCategoryFromView();
            outState.putParcelable(PositionManager.CATEGORY_KEY, cat);
        }catch (SQLException e){
        }
        outState.putParcelable(PositionManager.POSITION_KEY, pos);
        outState.putLong(ID_TO_UPDATE_KEY,idToUpdate);

        //outState.putParcelable("ll", circleSensitivity);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        posEdited = savedInstanceState.getParcelable(PositionManager.POSITION_KEY);
        catEdited = savedInstanceState.getParcelable(PositionManager.CATEGORY_KEY);
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

    private void putStuffOnMap() {
        // Check if we were successful in obtaining the map.
        if (mMap != null) {
            addMarkerOnMap();
        }
    }

    private void setupListeners() {
        // Setting the listener
        mMap.setOnMarkerDragListener(this);
        sensitivity = (SeekBar) findViewById(R.id.itemEditSensitive);
        sensitivity.setOnSeekBarChangeListener(this);
    }

    private void addMarkerOnMap() {
        // Creating a marker
        MarkerOptions markerOptions = new MarkerOptions();

        // Setting the position for the marker
        LatLng latLng = new LatLng(posEdited.getLatitude(), posEdited.getLongitude());

        markerOptions.position(latLng);

        // Setting the title for the marker.
        // This will be displayed on taping the marker
        markerOptions.title(posEdited.getTitle());

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
                .radius(posEdited.getSensitive()); // In meters

        // Get back the mutable Circle
        circleSensitivity = mMap.addCircle(circleOptions);

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

        // Setting the title for the marker.
        // This will be displayed on taping the marker
        markerOptions.title(posEdited.getTitle());

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
