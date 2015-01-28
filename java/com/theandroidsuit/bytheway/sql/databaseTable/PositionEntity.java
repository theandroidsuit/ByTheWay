package com.theandroidsuit.bytheway.sql.databaseTable;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


/**
 * Created by Virginia Hernández on 21/01/15.
 */
@DatabaseTable
public class PositionEntity implements Parcelable{

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_SENSITIVE = "sensitive";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_STATUS = "status";


    @DatabaseField(generatedId = true, columnName = COLUMN_ID)
    private long id = -1;
    @DatabaseField(columnName = COLUMN_LATITUDE)
    private double latitude = 0d;
    @DatabaseField(columnName = COLUMN_LONGITUDE)
    private double longitude = 0d;
    @DatabaseField(columnName = COLUMN_SENSITIVE)
    private long sensitive = 10l;
    @DatabaseField(columnName = COLUMN_TITLE)
    private String title;
    @DatabaseField(columnName = COLUMN_DESCRIPTION)
    private String description;
    @DatabaseField(columnName = COLUMN_STATUS)
    private String status;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getSensitive() {
        return sensitive;
    }

    public void setSensitive(long sensitive) {
        this.sensitive = sensitive;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public PositionEntity() {
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
        dest.writeDouble(Double.valueOf(latitude));
        dest.writeDouble(Double.valueOf(longitude));
        dest.writeLong(Long.valueOf(sensitive));
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(status);
    }

    /**
     * A constructor that initializes the Position object
     **/
    public PositionEntity(long id, long lat, long lng, int sens, String title, String desc, String status){
        this.id = id;
        this.latitude = lat;
        this.longitude = lng;
        this.sensitive = sens;
        this.title = title;
        this.description = desc;
        this.status = status;
    }

    /**
     * Retrieving Position data from Parcel object
     * This constructor is invoked by the method createFromParcel(Parcel source) of
     * the object CREATOR
     **/
    private PositionEntity(Parcel in){
        this.id = in.readLong();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.sensitive = in.readLong();
        this.title = in.readString();
        this.description = in.readString();
        this.status = in.readString();
    }

    public static final Parcelable.Creator<PositionEntity> CREATOR = new Parcelable.Creator<PositionEntity>() {

        @Override
        public PositionEntity createFromParcel(Parcel source) {
            return new PositionEntity(source);
        }

        @Override
        public PositionEntity[] newArray(int size) {
            return new PositionEntity[size];
        }
    };

}
