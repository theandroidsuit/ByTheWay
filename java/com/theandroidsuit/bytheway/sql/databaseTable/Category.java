package com.theandroidsuit.bytheway.sql.databaseTable;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


/**
 * Created by Virginia Hernández on 21/01/15.
 */
@DatabaseTable
public class Category implements Parcelable{

    public static final long   DEFAULT_CATEGORY_ID = 1l;
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_STATUS = "status";


    @DatabaseField(generatedId = true, columnName = COLUMN_ID, allowGeneratedIdInsert = true)
    private long id = -1;
    @DatabaseField(columnName = COLUMN_TITLE)
    private String title;
    @DatabaseField(columnName = COLUMN_STATUS)
    private String status;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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

    public Category() {
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
    public Category(long id, String title, String status){
        this.id = id;
        this.title = title;
        this.status = status;
    }

    /**
     * Retrieving Position data from Parcel object
     * This constructor is invoked by the method createFromParcel(Parcel source) of
     * the object CREATOR
     **/
    private Category(Parcel in){
        this.id = in.readLong();
        this.title = in.readString();
        this.status = in.readString();
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {

        @Override
        public Category createFromParcel(Parcel source) {
            return new Category(source);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

}
