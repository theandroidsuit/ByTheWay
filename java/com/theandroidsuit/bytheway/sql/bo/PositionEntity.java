package com.theandroidsuit.bytheway.sql.bo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Virginia Hernández on 13/01/15.
 */
public class PositionEntity extends BTWEntity implements Parcelable{

    private double latitude = 0d;
    private double longitude = 0d;
    private long sensitive = 10l;
    private String title;
    private String description;
    private String status;


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
