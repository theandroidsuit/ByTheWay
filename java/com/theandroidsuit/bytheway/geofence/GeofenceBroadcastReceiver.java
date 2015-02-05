package com.theandroidsuit.bytheway.geofence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.theandroidsuit.bytheway.R;

/**
 * Created by Virginia Hernández on 14/01/15.
 */
public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    public final String TAG = this.getClass().getName();

    /*
     * Define the required method for broadcast receivers
     * This method is invoked when a broadcast Intent triggers the receiver
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"onReceive");

        // Check the action code and determine what to do
        String action = intent.getAction();

        // Intent contains information about errors in adding or removing geofences
        if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_ERROR)) {

            handleGeofenceError(context, intent);

        // Intent contains information about successful addition or removal of geofences
        } else if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_ADDED) ||
                TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_REMOVED)) {

            handleGeofenceStatus(context, intent);

        // Intent contains information about a geofence transition
        } else if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_TRANSITION)) {

            handleGeofenceTransition(context, intent);

        // The Intent contained an invalid action
        } else {
            Log.e(TAG, context.getString(R.string.invalid_action_detail, action));
            //Toast.makeText(context, R.string.invalid_action, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * If you want to display a UI message about adding or removing geofences, put it here.
     *
     * @param context A Context for this component
     * @param intent The received broadcast Intent
     */
    private void handleGeofenceStatus(Context context, Intent intent) {
        Log.d(TAG,"handleGeofenceStatus");
    }

    /**
     * Report geofence transitions to the UI
     *
     * @param context A Context for this component
     * @param intent The Intent containing the transition
     */
    private void handleGeofenceTransition(Context context, Intent intent) {
        /*
         * If you want to change the UI when a transition occurs, put the code
         * here. The current design of the app uses a notification to inform the
         * user that a transition has occurred.
         */
        Log.d(TAG,"handleGeofenceTransition");
    }

    /**
     * Report addition or removal errors to the UI, using a Toast
     *
     * @param intent A broadcast Intent sent by ReceiveTransitionsIntentService
     */
    private void handleGeofenceError(Context context, Intent intent) {
        Log.d(TAG,"handleGeofenceError");

        String msg = intent.getStringExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS);
        Log.e(TAG, msg);
        //Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}