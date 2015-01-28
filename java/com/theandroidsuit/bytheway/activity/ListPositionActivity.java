package com.theandroidsuit.bytheway.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.theandroidsuit.bytheway.R;
import com.theandroidsuit.bytheway.adapter.PositionListAdapter;
import com.theandroidsuit.bytheway.geofence.PositionManager;
import com.theandroidsuit.bytheway.sql.databaseTable.PositionEntity;
import com.theandroidsuit.bytheway.sql.utils.DBHelper;
import com.theandroidsuit.bytheway.util.BTWUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class ListPositionActivity extends ActionBarActivity {

    public final String TAG = this.getClass().getName();
    private DBHelper mDBHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_position);


        String idsStr = getIntent().getStringExtra(PositionManager.LIST_ID_POSITION_KEY);
        final String startingFlow = getIntent().getStringExtra(BTWUtils.STARTING_FLOW) == null ?
                BTWUtils.LIST_POSITION_ACTIVITY : getIntent().getStringExtra(BTWUtils.STARTING_FLOW);

        try {

            Dao dao = getHelper().getPositionDao();
            List<PositionEntity> positionList = new ArrayList<PositionEntity>();
            if (null != idsStr){
                String[] ids = idsStr.split(PositionManager.ID_SEPARATOR);

                for(int i = 0; i < ids.length; i++) {
                    PositionEntity pos = (PositionEntity) dao.queryForId(Long.valueOf(ids[i]));
                    positionList.add(pos);
                }
            }else{
                positionList = dao.queryForAll();
            }

            final ListView listview = (ListView) findViewById(R.id.listview);

            final PositionListAdapter adapter = new PositionListAdapter(this,
                            R.layout.item_list_position, positionList, getHelper(), startingFlow);

            listview.setAdapter(adapter);

        } catch (SQLException e) {
            e.printStackTrace();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_position, menu);
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
}
