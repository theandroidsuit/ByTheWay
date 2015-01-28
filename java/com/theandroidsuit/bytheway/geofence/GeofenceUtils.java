package com.theandroidsuit.bytheway.geofence;

import android.text.format.DateUtils;

/**
 * Created by Virginia Hernández on 14/01/15.
 */

/**
 * This class defines constants used by location sample apps.
 */
public final class GeofenceUtils {
	
    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * DateUtils.HOUR_IN_MILLIS;
    
    public static final float GEOFENCE_STANDARD_RADIUS = 100f;

    // Used to track what type of geofence removal request was made.
    public enum REMOVE_TYPE {INTENT, LIST}

    // Used to track what type of request is in process
    public enum REQUEST_TYPE {ADD, REMOVE}

    /*
     * A log tag for the application
     */
    /* App Prefix to Geofences*/
    public static final String PREFIX_GEOFENCE = "com.amazingteam.saguntoenfallas.geofence.";
    
    // Intent actions
    public static final String ACTION_CONNECTION_ERROR =
    		PREFIX_GEOFENCE + "ACTION_CONNECTION_ERROR";

    public static final String ACTION_CONNECTION_SUCCESS =
            PREFIX_GEOFENCE + "ACTION_CONNECTION_SUCCESS";

    public static final String ACTION_GEOFENCES_ADDED =
            PREFIX_GEOFENCE + "ACTION_GEOFENCES_ADDED";

    public static final String ACTION_GEOFENCES_REMOVED =
            PREFIX_GEOFENCE + "ACTION_GEOFENCES_DELETED";

    public static final String ACTION_GEOFENCE_ERROR =
            PREFIX_GEOFENCE + "ACTION_GEOFENCES_ERROR";

    public static final String ACTION_GEOFENCE_TRANSITION =
            PREFIX_GEOFENCE + "ACTION_GEOFENCE_TRANSITION";

    public static final String ACTION_GEOFENCE_TRANSITION_ERROR =
                    PREFIX_GEOFENCE + "ACTION_GEOFENCE_TRANSITION_ERROR";

    // The Intent category used by all Location Services sample apps
    public static final String CATEGORY_LOCATION_SERVICES =
                    PREFIX_GEOFENCE + "CATEGORY_LOCATION_SERVICES";

    // Keys for extended data in Intents
    public static final String EXTRA_CONNECTION_CODE =
    		PREFIX_GEOFENCE + "EXTRA_CONNECTION_CODE";

    public static final String EXTRA_CONNECTION_ERROR_CODE =
            PREFIX_GEOFENCE + "EXTRA_CONNECTION_ERROR_CODE";

    public static final String EXTRA_CONNECTION_ERROR_MESSAGE =
            PREFIX_GEOFENCE + "EXTRA_CONNECTION_ERROR_MESSAGE";

    public static final String EXTRA_GEOFENCE_STATUS =
            PREFIX_GEOFENCE + "EXTRA_GEOFENCE_STATUS";

    /*
     * Keys for flattened geofences stored in SharedPreferences
     */
    public static final String KEY_LATITUDE = PREFIX_GEOFENCE + "KEY_LATITUDE";

    public static final String KEY_LONGITUDE = PREFIX_GEOFENCE + "KEY_LONGITUDE";

    public static final String KEY_RADIUS = PREFIX_GEOFENCE + "KEY_RADIUS";

    public static final String KEY_EXPIRATION_DURATION =
            PREFIX_GEOFENCE + "KEY_EXPIRATION_DURATION";

    public static final String KEY_TRANSITION_TYPE =
            PREFIX_GEOFENCE + "KEY_TRANSITION_TYPE";

    // The prefix for flattened geofence keys
    public static final String KEY_PREFIX = PREFIX_GEOFENCE + "KEY";

    // Invalid values, used to test geofence storage when retrieving geofences
    public static final long INVALID_LONG_VALUE = -999l;

    public static final float INVALID_FLOAT_VALUE = -999.0f;

    public static final int INVALID_INT_VALUE = -999;

    /*
     * Constants used in verifying the correctness of input values
     */
    public static final double MAX_LATITUDE = 90.d;

    public static final double MIN_LATITUDE = -90.d;

    public static final double MAX_LONGITUDE = 180.d;

    public static final double MIN_LONGITUDE = -180.d;

    public static final float MIN_RADIUS = 1f;

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    // A string of length 0, used to clear out input fields
    public static final String EMPTY_STRING = new String();

    public static final CharSequence GEOFENCE_ID_DELIMITER = ",";



}
