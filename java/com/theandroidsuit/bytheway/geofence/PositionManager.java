package com.theandroidsuit.bytheway.geofence;

import android.content.Context;

import com.google.android.gms.location.Geofence;
import com.theandroidsuit.bytheway.R;
import com.theandroidsuit.bytheway.sql.databaseTable.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Virginia Hernández on 14/01/15.
 */
public class PositionManager {

    public static final String ID_SEPARATOR = "#";

    public static final String LIST_ID_POSITION_KEY = "listIdPositions";
    public static final String ID_POSITION_KEY = "idPosition";
    public static final String POSITION_KEY = "position";
    public static final String CATEGORY_KEY = "category";
    public static final String CATEGORY_POSITION_KEY = "category_position";


    public static final String LOCATION_SELECTED_KEY = "location_selected_key";

    public static final String CATEGORY_DEFAULT = "Default";

    public static final String STATUS_ACTIVATED = "on";
    public static final String STATUS_DEACTIVATE = "off";


    public static final String SENSIVILITY_FILL_COLOR = "#8881DAF5";
    public static final String SENSIVILITY_BORDER_COLOR = "#81BEF7";

    public static final String LIST_MODE_KEY = "mode";
    public static final String LIST_MODE_TITLE = "modeTitle";
    public static final String LIST_MODE_CATEGORY = "modeCategory";

    public static final int MARKER_IMAGE_RESOURCE = R.drawable.marker_favs;

    public static final int DEFAULT_RADIUS = 40;

    /* GEOFENCES */
    private static List<Geofence> geofencesToActivate;


    public static void setGeofencesToActivate(List<Position> values){
        geofencesToActivate = new ArrayList<Geofence>();

        for(Position item: values){
            SimpleGeofence gf = translateToGeofence(item);

            geofencesToActivate.add(gf.toGeofence());
        }
    }


    public static void addActiveGeofence(Position positionEntity){
        if(null == geofencesToActivate){
            geofencesToActivate = new ArrayList<>();
        }
        SimpleGeofence geofence = translateToGeofence(positionEntity);

        geofencesToActivate.add(geofence.toGeofence());
    }

    public static void removeActiveGeofence(Position positionEntity) {
        if (null == geofencesToActivate) {
            return;
        } else{
            Geofence geofence = translateToGeofence(positionEntity).toGeofence();
            Geofence toDelete = null;
            for(Geofence item: geofencesToActivate){

                if(item.getRequestId().equals(geofence.getRequestId())){
                    toDelete = item;
                    break;
                }
            }

            if(null != toDelete) {
                geofencesToActivate.remove(toDelete);
            }
        }
    }

    public static List<Geofence> getGeofencesToActivate() {
        return geofencesToActivate;
    }



    public static String getPositionName(long identifier){
        return identifier + ID_SEPARATOR;
    }


    public static SimpleGeofence translateToGeofence(Position pos){
        SimpleGeofence gf = new SimpleGeofence(
                String.valueOf(pos.getId()),
                pos.getLatitude(),
                pos.getLongitude(),
                pos.getTitle(),
                pos.getSensitive(),
                GeofenceUtils.GEOFENCE_EXPIRATION_IN_MILLISECONDS,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);

        return gf;
    }
}
