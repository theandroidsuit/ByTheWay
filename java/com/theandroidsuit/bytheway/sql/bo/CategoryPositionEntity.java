package com.theandroidsuit.bytheway.sql.bo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Virginia Hernández on 13/01/15.
 */
public class CategoryPositionEntity extends BTWEntity implements Parcelable{


    private long categoryId;
    private long positionId;


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
