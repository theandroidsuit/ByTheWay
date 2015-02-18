package com.theandroidsuit.bytheway.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.theandroidsuit.bytheway.R;
import com.theandroidsuit.bytheway.geofence.PositionManager;
import com.theandroidsuit.bytheway.sql.databaseTable.Category;
import com.theandroidsuit.bytheway.sql.databaseTable.CategoryPosition;
import com.theandroidsuit.bytheway.sql.databaseTable.Position;
import com.theandroidsuit.bytheway.sql.utils.DBHelper;
import com.theandroidsuit.bytheway.util.BTWUtils;

import java.sql.SQLException;
import java.util.List;


public class DetailActivity extends ActionBarActivity {

    public final String TAG = this.getClass().getName();

    private Category categoryToShow = null;
    private Position positionToShow = null;
    private Long idToShow = null;

    private GoogleMap mMap;
    private ImageView image = null;
    private TextView title = null;
    private TextView description = null;
    private TextView category = null;


    private DBHelper mDBHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_activity);

        try {
            final String startingFlow = getIntent().getStringExtra(BTWUtils.STARTING_FLOW) == null ?
                    BTWUtils.DETAIL_POSITION_ACTIVITY : getIntent().getStringExtra(BTWUtils.STARTING_FLOW);

            if(null == idToShow)
                idToShow = getIntent().getExtras().getLong(PositionManager.ID_POSITION_KEY);

            if(null == positionToShow) {
                Dao dao = getHelper().getPositionDao();
                positionToShow = (Position) dao.queryForId(idToShow);
                categoryToShow = getCategoryByPosition(positionToShow);
            }

            setUpMapIfNeeded();

            image = (ImageView) findViewById(R.id.itemStatus);
            title = (TextView) findViewById(R.id.itemTitle);
            description = (TextView) findViewById(R.id.itemDescription);
            category = (TextView) findViewById(R.id.itemCategory);

            final Long idToUpdate = positionToShow.getId();
            ImageView updateButton = (ImageView) findViewById(R.id.imageEdit);
            updateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(getApplicationContext(), UpdatePositionActivity.class);

                    intent.putExtra(PositionManager.ID_POSITION_KEY, idToUpdate);
                    intent.putExtra(BTWUtils.STARTING_FLOW, startingFlow);

                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                    startActivity(intent);
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setViewValuesFromPosition();
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

    private void setViewValuesFromPosition() {


        title.setText(positionToShow.getTitle());
        description.setText(positionToShow.getDescription());
        category.setText(categoryToShow.getTitle());

        String uri = "drawable/off";

        if(PositionManager.STATUS_ACTIVATED.equals(positionToShow.getStatus()))
            uri = "drawable/fav";

        int imageResource = getResources().getIdentifier(uri, null, this.getPackageName());
        Drawable imageDrw = getResources().getDrawable(imageResource);

        image.setImageDrawable(imageDrw);
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(PositionManager.POSITION_KEY, positionToShow);
        outState.putLong(PositionManager.ID_POSITION_KEY,idToShow);
        outState.putParcelable(PositionManager.CATEGORY_KEY, categoryToShow);
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
        LatLng latLng = new LatLng(positionToShow.getLatitude(),positionToShow.getLongitude());

        markerOptions.position(latLng);

        // Setting the title for the marker.
        // This will be displayed on taping the marker
        markerOptions.title(positionToShow.getTitle());

        // Setting the marker Icon
        markerOptions.icon(BitmapDescriptorFactory.fromResource(PositionManager.MARKER_IMAGE_RESOURCE));

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
                .radius(positionToShow.getSensitive()); // In meters

        // Get back the mutable Circle
        mMap.addCircle(circleOptions);

    }
}
