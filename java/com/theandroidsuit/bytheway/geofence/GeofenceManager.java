package com.theandroidsuit.bytheway.geofence;

import android.content.Context;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.theandroidsuit.bytheway.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Virginia Hernández on 21/01/15.
 */
public class GeofenceManager {

    public static final String INIT_GEOFENCE_KEY = "init_geofence_key";
    public static final int DEFAULT_RADIUS = 40;
    private Context context;

    public final String TAG = this.getClass().getName();

    /*
    * An instance of an inner class that receives broadcasts from listeners and from the
    * IntentService that receives geofence transition events
    */
    private GeofenceBroadcastReceiver mBroadcastReceiver;

    // Add geofences handler
    private static GeofenceRegister mGeofenceRegister;
    private static GeofenceRemover mGeofenceRemover;

    // Store the current request
    private GeofenceUtils.REQUEST_TYPE mRequestType;


    // An intent filter for the broadcast receiver
    private IntentFilter mIntentFilter;

    public GeofenceManager(Context ctx){
        this.context = ctx;
    }

    public void setupGeofenceSystem() {
        Log.d(TAG, "setupGeofenceSystem");

        // Create a new broadcast receiver to receive updates from the listeners and service
        mBroadcastReceiver = new GeofenceBroadcastReceiver();

        // Instantiate a Geofence to register and remover
        mGeofenceRegister = new GeofenceRegister(context);
        mGeofenceRemover = new GeofenceRemover(context);

        // Create an intent filter for the broadcast receiver
        mIntentFilter = new IntentFilter();

        // Action for broadcast Intents that report successful addition of geofences
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_ADDED);

        // Action for broadcast Intents that report successful removal of geofences
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_REMOVED);

        // Action for broadcast Intents containing various types of geofencing errors
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCE_ERROR);

        // All Location Services sample apps use this category
        mIntentFilter.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);

        // Register the broadcast receiver to receive status updates
        LocalBroadcastManager.getInstance(context).registerReceiver(mBroadcastReceiver, mIntentFilter);
    }


    /**************************************************/
    /* GEOFENCE MANAGER METHODS */
    /**************************************************/

    public void requestGeofences() {
        Log.d(TAG,"requestGeofences");

        /*
         * Record the request as an ADD. If a connection error occurs, the app can automatically
         * restart the add request if Google Play services can fix the error
         */
        mRequestType = GeofenceUtils.REQUEST_TYPE.ADD;

        /*
         * Check for Google Play services. Do this after setting the request type.
         * If connecting to Google Play services fails, onActivityResult is eventually
         * called, and it needs to know what type of request was in progress.
         */
        if (!servicesConnected()) {
            return;
        }

        // Start the request. Fail if there's already a request in progress
        try {
            // Try to add geofences
            mGeofenceRegister.registerGeofences(PositionManager.getGeofencesToActivate());

        } catch (UnsupportedOperationException e) {
            // Notify user that previous request hasn't finished.
            // Toast.makeText(context, R.string.add_geofences_already_requested_error, Toast.LENGTH_LONG).show();
        }
    }

    public void requestOneGeofence(Geofence geofence) {
        Log.d(TAG,"requestOneGeofence");

        /*
         * Record the request as an ADD. If a connection error occurs, the app can automatically
         * restart the add request if Google Play services can fix the error
         */
        mRequestType = GeofenceUtils.REQUEST_TYPE.ADD;

        /*
         * Check for Google Play services. Do this after setting the request type.
         * If connecting to Google Play services fails, onActivityResult is eventually
         * called, and it needs to know what type of request was in progress.
         */
        if (!servicesConnected()) {
            return;
        }

        // Start the request. Fail if there's already a request in progress
        try {
            // Try to add geofences
            List<Geofence> list = new ArrayList<>();
            list.add(geofence);
            mGeofenceRegister.registerGeofences(list);

        } catch (UnsupportedOperationException e) {
            // Notify user that previous request hasn't finished.
            // Toast.makeText(context, R.string.add_geofences_already_requested_error, Toast.LENGTH_LONG).show();
        }
    }

    public void removeOneGeofence(Geofence geofence) {
        Log.d(TAG,"removeOneGeofence");

        /*
         * Record the request as an REMOVE. If a connection error occurs, the app can automatically
         * restart the remove request if Google Play services can fix the error
         */
        mRequestType = GeofenceUtils.REQUEST_TYPE.REMOVE;

        /*
         * Check for Google Play services. Do this after setting the request type.
         * If connecting to Google Play services fails, onActivityResult is eventually
         * called, and it needs to know what type of request was in progress.
         */
        if (!servicesConnected()) {
            return;
        }

        // Start the request. Fail if there's already a request in progress
        try {
            // Try to remove geofences
            List<Geofence> list = new ArrayList<>();
            list.add(geofence);
            mGeofenceRemover.removeGeofences(list);

        } catch (UnsupportedOperationException e) {
            // Notify user that previous request hasn't finished.
            // Toast.makeText(context, R.string.remove_geofences_already_requested_error, Toast.LENGTH_LONG).show();
        }
    }



    public void removeAllActiveGeofences(){
        Log.d(TAG,"removeAllActiveGeofences");

        /*
         * Record the request as an REMOVE. If a connection error occurs, the app can automatically
         * restart the remove request if Google Play services can fix the error
         */
        mRequestType = GeofenceUtils.REQUEST_TYPE.REMOVE;

        /*
         * Check for Google Play services. Do this after setting the request type.
         * If connecting to Google Play services fails, onActivityResult is eventually
         * called, and it needs to know what type of request was in progress.
         */
        if (!servicesConnected()) {
            return;
        }

        // Start the request. Fail if there's already a request in progress
        try {
            // Try to remove geofences
            mGeofenceRemover.removeGeofences(PositionManager.getGeofencesToActivate());

        } catch (UnsupportedOperationException e) {
            // Notify user that previous request hasn't finished.
            // Toast.makeText(context, R.string.remove_geofences_already_requested_error, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {
        Log.d(TAG,"servicesConnected");

        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d(TAG, context.getString(R.string.play_services_available));

            return true;

        } else {
            // Google Play services was not available for some reason

            // In debug mode, log the status
            Log.d(TAG, context.getString(R.string.play_services_unavailable));

            return false;
        }
    }

    public void releaseReceiver() {
        removeAllActiveGeofences();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mBroadcastReceiver);
    }

}
