package com.theandroidsuit.bytheway.activity;

import android.app.Activity;
import android.content.Intent;
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
import com.theandroidsuit.bytheway.geofence.PositionManager;
import com.theandroidsuit.bytheway.sql.databaseTable.Category;
import com.theandroidsuit.bytheway.sql.utils.DBHelper;

import java.sql.SQLException;

public class UpdateCategoryActivity extends Activity {


    public final String TAG = this.getClass().getName();

    private DBHelper mDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_category);

        final Intent intent = getIntent();

        final Category category = intent.getParcelableExtra(PositionManager.CATEGORY_KEY);

        EditText et = (EditText) findViewById(R.id.itemEditCategory);
        et.setText(category.getTitle());

        ImageView add = (ImageView) findViewById(R.id.imageEditCategory);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            
                try {
                    updateCategory(category);
                    setResult(RESULT_OK, intent);

                    //callback.notifyTheChange();
                } catch (BTWOperationError oe) {
                    setResult(RESULT_CANCELED);
                }

               finish();
            }
        });
    }



    private void updateCategory(Category cat)throws BTWOperationError{
        Dao dao;
        try {
            dao = getHelper().getCategoryDao();

            EditText et = (EditText) findViewById(R.id.itemEditCategory);
            String title = et.getText().toString();

            cat.setTitle(title);

            dao.update(cat);

        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }

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
