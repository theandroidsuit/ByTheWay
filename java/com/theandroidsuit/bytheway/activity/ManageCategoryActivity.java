package com.theandroidsuit.bytheway.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ListView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.theandroidsuit.bytheway.R;
import com.theandroidsuit.bytheway.adapter.CategoryListAdapter;
import com.theandroidsuit.bytheway.adapter.PositionListAdapter;
import com.theandroidsuit.bytheway.sql.databaseTable.Category;
import com.theandroidsuit.bytheway.sql.databaseTable.CategoryPosition;
import com.theandroidsuit.bytheway.sql.databaseTable.Position;
import com.theandroidsuit.bytheway.sql.utils.DBHelper;

import java.sql.SQLException;
import java.util.List;

public class ManageCategoryActivity extends ActionBarActivity {

    public final String TAG = this.getClass().getName();

    public static final int ADD_CATEGORY = 5;

    private DBHelper mDBHelper;
    private List<Category> categoriesList;
    private CategoryListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_category);


        ListView listview = (ListView) findViewById(R.id.catlistview);


        //if (null == adapter) {
        setUpAdapter();
        //}

        listview.setAdapter(adapter);

    }

    private void setUpAdapter() {
        try {

            categoriesList = getAllCategories();
            adapter = new CategoryListAdapter(this, R.layout.item_list_category_manage, categoriesList, getHelper());

        }catch (SQLException e){

        }
    }


    public void notifyTheChange(){
        try {
            categoriesList = getAllCategories();
            adapter.updateCategoryList(categoriesList);
        }catch (SQLException e){
        }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       // adapter.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CategoryListAdapter.CATEGORY_UPDATED || requestCode == ManageCategoryActivity.ADD_CATEGORY) {
            if (resultCode == RESULT_OK){
                notifyTheChange();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_manage_category, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_new_category) {

            Intent intent;
            intent = new Intent(ManageCategoryActivity.this, AddCategoryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, ManageCategoryActivity.ADD_CATEGORY);
        }

        return super.onOptionsItemSelected(item);
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

    private List<Category> getAllCategories() throws SQLException {

        QueryBuilder<Category, Integer> qbCategory = getHelper().getCategoryDao().queryBuilder();

        qbCategory.orderByRaw(Category.COLUMN_TITLE + " COLLATE NOCASE").query();

        List<Category> list = qbCategory.query();

        return list;
    }

}
