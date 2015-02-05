package com.theandroidsuit.bytheway.sql.databaseTable;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


/**
 * Created by Virginia Hernández on 21/01/15.
 */
@DatabaseTable
public class CategoryPositionEntity implements Parcelable{

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ID_CATEGORY = "categoryId";
    public static final String COLUMN_ID_POSITION = "positionId";


    @DatabaseField(generatedId = true, columnName = COLUMN_ID)
    private long id = -1;
    @DatabaseField(columnName = COLUMN_ID_CATEGORY)
    private long categoryId;
    @DatabaseField(columnName = COLUMN_ID_POSITION)
    private long positionId;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public long getPositionId() {
        return positionId;
    }

    public void setPositionId(long positionId) {
        this.positionId = positionId;
    }

    public CategoryPositionEntity() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * Storing the Position data to Parcel object
     **/
    @Override
    public void writeToParcel(Parcel dest, int flags) {


        dest.writeLong(Long.valueOf(id));
        dest.writeLong(Long.valueOf(categoryId));
        dest.writeLong(Long.valueOf(positionId));
    }

    /**
     * A constructor that initializes the Position object
     **/
    public CategoryPositionEntity(long id, long idCat, long idPos){
        this.id = id;
        this.categoryId = idCat;
        this.positionId = idPos;
    }

    /**
     * Retrieving Position data from Parcel object
     * This constructor is invoked by the method createFromParcel(Parcel source) of
     * the object CREATOR
     **/
    private CategoryPositionEntity(Parcel in){
        this.id = in.readLong();
        this.categoryId = in.readLong();
        this.positionId = in.readLong();
    }

    public static final Creator<CategoryPositionEntity> CREATOR = new Creator<CategoryPositionEntity>() {

        @Override
        public CategoryPositionEntity createFromParcel(Parcel source) {
            return new CategoryPositionEntity(source);
        }

        @Override
        public CategoryPositionEntity[] newArray(int size) {
            return new CategoryPositionEntity[size];
        }
    };

}
