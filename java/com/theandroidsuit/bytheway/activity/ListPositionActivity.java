package com.theandroidsuit.bytheway.activity;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Spinner;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.theandroidsuit.bytheway.R;
import com.theandroidsuit.bytheway.adapter.PositionByCategoryListAdapter;
import com.theandroidsuit.bytheway.adapter.PositionListAdapter;
import com.theandroidsuit.bytheway.geofence.PositionManager;
import com.theandroidsuit.bytheway.sql.databaseTable.Category;
import com.theandroidsuit.bytheway.sql.databaseTable.CategoryPosition;
import com.theandroidsuit.bytheway.sql.databaseTable.Position;
import com.theandroidsuit.bytheway.sql.utils.DBHelper;
import com.theandroidsuit.bytheway.util.BTWUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ListPositionActivity extends ActionBarActivity implements Spinner.OnItemSelectedListener{

    private static final String IDS_PARAMS_KEY = "ids_params_key";
    private static final String STARTING_FLOW_KEY = "starting_flow_key";

    public final String TAG = this.getClass().getName();
    private DBHelper mDBHelper;
    private String mode;

    private PositionListAdapter adapter;
    private PositionByCategoryListAdapter expansibleAdapter;

    private List<Position> positionList;
    private List<String> listHeaders;
    private HashMap<String, List<Position>> dataMap;

    private static long[] idsParam;
    private static String startingFlow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_position);


        if (null == savedInstanceState) {
            mode = getIntent().getStringExtra(PositionManager.LIST_MODE_KEY);

            idsParam = getIntent().getLongArrayExtra(PositionManager.LIST_ID_POSITION_KEY);
            getIntent().removeExtra(PositionManager.LIST_ID_POSITION_KEY);

            startingFlow = getIntent().getStringExtra(BTWUtils.STARTING_FLOW) == null ?
                    BTWUtils.LIST_POSITION_ACTIVITY : getIntent().getStringExtra(BTWUtils.STARTING_FLOW);
        }

        // Setup the listener
        Spinner spinner = (Spinner) findViewById(R.id.modeList);
        spinner.setOnItemSelectedListener(this);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLongArray(IDS_PARAMS_KEY, idsParam);
        outState.putString(STARTING_FLOW_KEY, startingFlow);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        idsParam = savedInstanceState.getLongArray(IDS_PARAMS_KEY);
        startingFlow = savedInstanceState.getString(STARTING_FLOW_KEY);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDBHelper != null) {
            OpenHelperManager.releaseHelper();
            mDBHelper = null;
        }
    }

    /**********************************************************/
    /* LISTENER METHODS                                       */
    /**********************************************************/

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        String option = (String) parent.getItemAtPosition(position);

        if ("All Locations".equals(option)){
            mode = PositionManager.LIST_MODE_TITLE;
            setListByMode(idsParam, startingFlow);
        }else if ("Ordered by Category".equals(option)){
            mode = PositionManager.LIST_MODE_CATEGORY;
            setListByMode(null, startingFlow);
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**********************************************************/
    /* AUXILIAR METHODS                                       */
    /**********************************************************/

    private DBHelper getHelper() {
        if (mDBHelper == null) {
            mDBHelper = OpenHelperManager.getHelper(this, DBHelper.class);
        }
        return mDBHelper;
    }


    /**********************************************************/
    /* BUSINESS METHODS                                       */
    /**********************************************************/

    private void setListByMode(long[] idsStr, String startingFlow) {
        try {

            if (null == mode || PositionManager.LIST_MODE_TITLE.equals(mode)) {

                ListView listview = (ListView) findViewById(R.id.listview);
                ExpandableListView listviewToHide = (ExpandableListView) findViewById(R.id.listviewExp);

                listview.setVisibility(View.VISIBLE);
                listviewToHide.setVisibility(View.INVISIBLE);

                if (null != idsStr){
                    Spinner spinner = (Spinner) findViewById(R.id.modeList);
                    spinner.setVisibility(View.INVISIBLE);
                }

                if (null == adapter) {
                    positionList = getAllPositionsByTitle(idsStr);

                    adapter = new PositionListAdapter(this, R.layout.item_list_position, positionList, getHelper(), startingFlow);
                }

                listview.setAdapter(adapter);

            }else if (PositionManager.LIST_MODE_CATEGORY.equals(mode)){
                ExpandableListView listview = (ExpandableListView) findViewById(R.id.listviewExp);
                ListView listviewToHide = (ListView) findViewById(R.id.listview);
                listview.setVisibility(View.VISIBLE);
                listviewToHide.setVisibility(View.INVISIBLE);

                if(expansibleAdapter == null) {
                    List<Category> listCategories = getCategoriesInUse();
                    listHeaders = categoryToString(listCategories);

                    dataMap = getAllPositionsByCategory(listCategories);

                    expansibleAdapter = new PositionByCategoryListAdapter(this, getHelper(), listHeaders, dataMap, startingFlow);
                }

                listview.setAdapter(expansibleAdapter);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<String> categoryToString(List<Category> listCat){

        if (null == listCat || listCat.isEmpty()){
            return null;
        }

        List<String> list = new ArrayList<String>();

        for(Category item: listCat){
            list.add(item.getTitle());
        }

        return list;
    }


    private List<Category> getCategoriesInUse() throws SQLException{

        QueryBuilder<CategoryPosition, Integer> qbCatPos = getHelper().getCategoryPositionDao().queryBuilder();
        QueryBuilder<Category, Integer> qbCategory = getHelper().getCategoryDao().queryBuilder();

        qbCategory.orderByRaw(Category.COLUMN_TITLE + " COLLATE NOCASE").query();

        List<Category> list = qbCategory.join(qbCatPos).distinct().query();
        return list;
    }

    private List<Position> getAllPositionsByTitle(long[] idsParam) throws SQLException {
        List<Position> list = new ArrayList();

        Dao dao = getHelper().getPositionDao();
        if (null != idsParam){
            //String[] ids = idsStr.split(PositionManager.ID_SEPARATOR);

            for(int i = 0; i < idsParam.length; i++) {
                Position pos = (Position) dao.queryForId(Long.valueOf(idsParam[i]));
                list.add(pos);
            }
        }else{
            list = dao.queryBuilder().orderByRaw(Position.COLUMN_TITLE + " COLLATE NOCASE").query();
        }
        return list;
    }

    private HashMap<String, List<Position>> getAllPositionsByCategory(List<Category> listCat) throws SQLException {
        HashMap<String, List<Position>> map = new HashMap<>();
        List<Position> list = new ArrayList();

        for(Category cat: listCat){
            QueryBuilder<CategoryPosition, Integer> qbCatPos = getHelper().getCategoryPositionDao().queryBuilder();
            QueryBuilder<Position, Integer> qbPosition = getHelper().getPositionDao().queryBuilder();

            qbCatPos.where().eq(CategoryPosition.COLUMN_ID_CATEGORY,cat.getId());
            qbPosition.orderByRaw(Position.COLUMN_TITLE + " COLLATE NOCASE");

            list = qbPosition.join(qbCatPos).query();

            map.put(cat.getTitle(),list);
        }


        //return list;
        return map;
    }

    public void notifyTheChange(){
        try {
            if (null == mode || PositionManager.LIST_MODE_TITLE.equals(mode)) {
                positionList = getAllPositionsByTitle(idsParam);

                adapter.updateAdapterData(positionList);

            }else {
                List<Category> listCategories = getCategoriesInUse();
                listHeaders = categoryToString(listCategories);
                dataMap = getAllPositionsByCategory(listCategories);

                expansibleAdapter.updateAdapterData(listHeaders, dataMap);

//                expansibleAdapter.notifyDataSetChanged();
            }
        }catch (SQLException e){
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PositionListAdapter.POSITION_UPDATED) {
            if (resultCode == RESULT_OK){
                notifyTheChange();
            }
        }else if (requestCode == PositionByCategoryListAdapter.POSITION_UPDATED_EXT) {
            if (resultCode == RESULT_OK) {
                notifyTheChange();
            }
        }
    }
}
