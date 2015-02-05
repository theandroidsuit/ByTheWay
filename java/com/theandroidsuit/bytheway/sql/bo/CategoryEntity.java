package com.theandroidsuit.bytheway.sql.bo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Virginia Hernández on 13/01/15.
 */
public class CategoryEntity extends BTWEntity implements Parcelable{


    private String title;
    private String status;



    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public CategoryEntity() {
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
        dest.writeString(title);
        dest.writeString(status);
    }

    /**
     * A constructor that initializes the Position object
     **/
    public CategoryEntity(long id, String title, String status){
        this.id = id;
        this.title = title;
        this.status = status;
    }

    /**
     * Retrieving Position data from Parcel object
     * This constructor is invoked by the method createFromParcel(Parcel source) of
     * the object CREATOR
     **/
    private CategoryEntity(Parcel in){
        this.id = in.readLong();
        this.title = in.readString();
        this.status = in.readString();
    }

    public static final Creator<CategoryEntity> CREATOR = new Creator<CategoryEntity>() {

        @Override
        public CategoryEntity createFromParcel(Parcel source) {
            return new CategoryEntity(source);
        }

        @Override
        public CategoryEntity[] newArray(int size) {
            return new CategoryEntity[size];
        }
    };
}
