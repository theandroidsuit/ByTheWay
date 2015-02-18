package com.theandroidsuit.bytheway.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

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

public class AddAndDisplayFromRequestActivity extends ActionBarActivity {

    public final String TAG = this.getClass().getName();

    private static final int NUM_SEGMENTS = 5;
    private DBHelper mDBHelper;
    private static Position pos;
    private static Category cat;
    private static CategoryPosition catPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //setContentView(R.layout.activity_detail_activity);


        if (savedInstanceState == null) {
            // First incarnation of this activity.

            // Reincarnated activity.
            Intent intent = getIntent();
            Uri data = intent.getData();


            List<String> segments = data.getPathSegments();

            /*
            * 1 - Title
            * 2 - Description
            * 3 - Category (Title)
            * 4 - Latitude
            * 5 - Longitude
            * */


            if(segments.size() != NUM_SEGMENTS){
                // ERROR
                finish();
            }else{

                try {
                    pos = new Position();
                    pos.setTitle(segments.get(0));
                    pos.setDescription(segments.get(1));

                    String latStr = segments.get(3);
                    double latitude = Double.valueOf(latStr);
                    pos.setLatitude(latitude);

                    String lonStr = segments.get(4);
                    double longitude = Double.valueOf(lonStr);
                    pos.setLongitude(longitude);

                    // Status
                    pos.setStatus(PositionManager.STATUS_ACTIVATED);
                    // Sensitive
                    pos.setSensitive(PositionManager.DEFAULT_RADIUS);

                    // Dar de alta la Position
                    addPosition(pos);

                    // Dar de alta la Category (si necesario)
                    String catTitle = segments.get(2);
                    List<Category> listCategory = getHelper().getCategoryDao().queryForEq(Category.COLUMN_TITLE,catTitle);

                    if(null == listCategory || listCategory.isEmpty()){
                        cat = new Category();
                        // Dar de alta la category
                        cat.setTitle(catTitle);
                        cat.setStatus(PositionManager.STATUS_ACTIVATED);
                        addCategory(cat);
                    }else{
                        // Relacionar la position con la categoria encontrada
                        cat = listCategory.get(0);
                    }


                    // Dar de alta la Category_Position
                    catPos = addCategoryPosition(pos, cat);
                }catch (Exception e){
                    Log.d(TAG, e.getMessage());
                }
            }
        }


        // Go to detail
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(PositionManager.ID_POSITION_KEY, pos.getId());

       // startActivity(intent);



        TaskStackBuilder.create(this)
                // Add all of this activity's parents to the back stack
                .addNextIntentWithParentStack(intent)
                        // Navigate up to the closest parent
                .startActivities();

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(PositionManager.POSITION_KEY, pos);
        outState.putParcelable(PositionManager.CATEGORY_KEY, cat);
        outState.putParcelable(PositionManager.CATEGORY_POSITION_KEY, catPos);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        pos = savedInstanceState.getParcelable(PositionManager.POSITION_KEY);
        cat = savedInstanceState.getParcelable(PositionManager.CATEGORY_KEY);
        catPos = savedInstanceState.getParcelable(PositionManager.CATEGORY_POSITION_KEY);
    }

    private void addPosition(Position pos) {
        Dao dao;
        try{
            dao = getHelper().getPositionDao();
            dao.create(pos);
        }catch(SQLException e){

        }
    }

    private void addCategory(Category cat) {
        Dao dao;
        try{
            dao = getHelper().getCategoryDao();
            dao.create(cat);
        }catch(SQLException e){

        }
    }

    private CategoryPosition addCategoryPosition(Position pos, Category cat) {
        Dao dao;
        try{
            CategoryPosition catPos = new CategoryPosition();
            catPos.setCategory(cat);
            catPos.setPosition(pos);

            dao = getHelper().getCategoryPositionDao();
            dao.create(catPos);

            return catPos;
        }catch(SQLException e){

        }
        return null;
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

}
