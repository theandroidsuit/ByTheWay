package com.theandroidsuit.bytheway.sql.databaseTable;

import android.os.Parcel;
import android.os.Parcelable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;


/**
 * Created by Virginia Hernández on 21/01/15.
 */
@DatabaseTable
public class CategoryPosition implements Parcelable{

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ID_CATEGORY = "categoryId";
    public static final String COLUMN_ID_POSITION = "positionId";


    @DatabaseField(generatedId = true, columnName = COLUMN_ID)
    private long id = -1;
    /*@DatabaseField(columnName = COLUMN_ID_CATEGORY)
    private long categoryId;
    @DatabaseField(columnName = COLUMN_ID_POSITION)
    private long positionId;*/

    @DatabaseField(canBeNull = false, foreign = true, columnName = COLUMN_ID_CATEGORY)
    private Category category;
    @DatabaseField(canBeNull = false, foreign = true, columnName = COLUMN_ID_POSITION)
    private Position position;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    //
//    public long getCategoryId() {
//        return categoryId;
//    }
//
//    public void setCategoryId(long categoryId) {
//        this.categoryId = categoryId;
//    }
//
//    public long getPositionId() {
//        return positionId;
//    }
//
//    public void setPositionId(long positionId) {
//        this.positionId = positionId;
//    }

    public CategoryPosition() {
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
//        dest.writeLong(Long.valueOf(categoryId));
//        dest.writeLong(Long.valueOf(positionId));
        dest.writeParcelable(category, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        dest.writeParcelable(position, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
    }

    /**
     * A constructor that initializes the Position object
     **/
    public CategoryPosition(long id, Category category, Position position){
        this.id = id;
//        this.categoryId = idCat;
//        this.positionId = idPos;
        this.category = category;
        this.position = position;
    }

    /**
     * Retrieving Position data from Parcel object
     * This constructor is invoked by the method createFromParcel(Parcel source) of
     * the object CREATOR
     **/
    private CategoryPosition(Parcel in){
        this.id = in.readLong();
//        this.categoryId = in.readLong();
//        this.positionId = in.readLong();
        this.category = in.readParcelable(Category.class.getClassLoader());
        this.position = in.readParcelable(Position.class.getClassLoader());

    }

    public static final Creator<CategoryPosition> CREATOR = new Creator<CategoryPosition>() {

        @Override
        public CategoryPosition createFromParcel(Parcel source) {
            return new CategoryPosition(source);
        }

        @Override
        public CategoryPosition[] newArray(int size) {
            return new CategoryPosition[size];
        }
    };

}
