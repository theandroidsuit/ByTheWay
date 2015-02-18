package com.theandroidsuit.bytheway.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.theandroidsuit.bytheway.R;
import com.theandroidsuit.bytheway.error.BTWOperationError;
import com.theandroidsuit.bytheway.geofence.GeofenceManager;
import com.theandroidsuit.bytheway.geofence.PositionManager;
import com.theandroidsuit.bytheway.sql.databaseTable.Category;
import com.theandroidsuit.bytheway.sql.databaseTable.Position;
import com.theandroidsuit.bytheway.sql.utils.DBHelper;
import com.theandroidsuit.bytheway.util.BTWUtils;

import java.sql.SQLException;

public class AddCategoryActivity extends Activity {


    public final String TAG = this.getClass().getName();

    private DBHelper mDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);

        final Intent intent = getIntent();
        ImageView add = (ImageView) findViewById(R.id.imageAddCategory);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            
                try {
                    String categoryName = addCategory();
                    
                    intent.putExtra("newCategoryName", categoryName);
                    setResult(RESULT_OK, intent);

                } catch (BTWOperationError oe) {
                    setResult(RESULT_CANCELED);
                }

               finish();
            }
        });
    }



    private String addCategory()throws BTWOperationError{
        Dao dao;
        try {
            dao = getHelper().getCategoryDao();
            Category cat = new Category();

            EditText et = (EditText) findViewById(R.id.itemAddCategory);
            String title = et.getText().toString();

            cat.setTitle(title);
            cat.setStatus(PositionManager.STATUS_ACTIVATED);

            dao.create(cat);
            
            return cat.getTitle();

        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
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
