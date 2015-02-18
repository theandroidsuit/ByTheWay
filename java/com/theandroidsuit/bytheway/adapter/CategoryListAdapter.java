package com.theandroidsuit.bytheway.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.j256.ormlite.dao.Dao;
import com.theandroidsuit.bytheway.R;
import com.theandroidsuit.bytheway.activity.DetailActivity;
import com.theandroidsuit.bytheway.activity.ManageCategoryActivity;
import com.theandroidsuit.bytheway.activity.UpdateCategoryActivity;
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

/**
 * Created by Virginia Hernández on 19/01/15.
 */
public class CategoryListAdapter extends BaseAdapter {

    public static final int CATEGORY_UPDATED = 2;
    public static final int CATEGORY_DELETED = 1;
    public final String TAG = this.getClass().getName();

    private int resource;
    private LayoutInflater inflater;
    private Context context;
    private ManageCategoryActivity callback;
    private DBHelper mDBHelper;
    private List<Category> data;

    public CategoryListAdapter(Context ctx, int resourceId, List<Category> objs, DBHelper helper) {
        super();
        data = objs;
        resource = resourceId;
        inflater = LayoutInflater.from(ctx);
        context = ctx;
        callback = (ManageCategoryActivity) ctx;
        mDBHelper = helper;
    }

    public void updateCategoryList(List<Category> newlist) {
        data.clear();
        data.addAll(newlist);
        this.notifyDataSetChanged();
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {

        /* create a new view of my layout and inflate it in the row */
        convertView = (RelativeLayout) inflater.inflate(resource, null);

        final Category category = getItem(pos);

        TextView txtTitle = (TextView) convertView.findViewById(R.id.categoryListItemTitle);
        txtTitle.setText(category.getTitle());

        txtTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO: Go to detail
                Intent intent = new Intent(context, UpdateCategoryActivity.class);

                intent.putExtra(PositionManager.CATEGORY_KEY, category);

                ((Activity)context).startActivityForResult(intent, CATEGORY_UPDATED);


            }
        });



        ImageView delete = (ImageView) convertView.findViewById(R.id.categoryListDeleteItem);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Category.DEFAULT_CATEGORY_ID == category.getId()){
                    Toast.makeText(context, R.string.cannot_delete_default_category, Toast.LENGTH_LONG);
                }else{
                    List<CategoryPosition> listPositionsByCategory = getPositionsByCategory(category);
                    if(null == listPositionsByCategory || listPositionsByCategory.isEmpty()){
                        deleteCategory(category);
                        callback.notifyTheChange();
                    }else{
                        Toast.makeText(context, R.string.category_no_empty, Toast.LENGTH_LONG);
                    }
                }

            }


        });

        return convertView;
    }


    private void deleteCategory(Category cat){
        Dao dao;

        try{
            dao = mDBHelper.getCategoryDao();
            dao.delete(cat);
        }catch (SQLException e){
            Log.e(TAG,e.getMessage());
        }
    }

    private List<CategoryPosition> getPositionsByCategory(Category category) {
        Dao dao;
        List<CategoryPosition> list;
        try{
            dao = mDBHelper.getCategoryPositionDao();
            list = dao.queryForEq(CategoryPosition.COLUMN_ID_CATEGORY, category.getId());

            return list;
        }catch (SQLException e){
            Log.e(TAG,e.getMessage());
        }

        return null;
    }


    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Category getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).getId();
    }


}