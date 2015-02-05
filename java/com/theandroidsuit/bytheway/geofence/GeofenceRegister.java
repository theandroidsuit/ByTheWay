package com.theandroidsuit.bytheway.geofence;


import com.google.android.gms.common.ConnectionResult;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;
import com.theandroidsuit.bytheway.R;
import com.theandroidsuit.bytheway.service.ReceiveTransitionsIntentService;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


import java.util.List;
/**
 * Created by Virginia Hernández on 14/01/15.
 */

/**
 * Class for connecting to Location Services and requesting geofences.
 * <b>
 * Note: Clients must ensure that Google Play services is available before requesting geofences.
 * </b> Use GooglePlayServicesUtil.isGooglePlayServicesAvailable() to check.
 *
 *
 * To use a GeofenceRequester, instantiate it and call AddGeofence(). Everything else is done
 * automatically.
 *
 */

public class GeofenceRegister implements    GoogleApiClient.ConnectionCallbacks,
                                            GoogleApiClient.OnConnectionFailedListener,
                                            ResultCallback<Status>{

    public final String TAG = this.getClass().getName();

    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private List<Geofence> geofencesToAdd;
    private PendingIntent mGeofencePendingIntent;

    public GeofenceRegister(Context context){
        Log.d(TAG,"GeofenceRegister");

        mContext = context;
    }

    public void registerGeofences(List<Geofence> geofences){
        Log.d(TAG,"registerGeofences");

        geofencesToAdd = geofences;

        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG,"onConnected");

        if (!mGoogleApiClient.isConnected()) {
            //Toast.makeText(mContext, mContext.getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        try {

            if (null != geofencesToAdd && !geofencesToAdd.isEmpty()) {
                mGeofencePendingIntent = getGeofencePendingIntent();
                LocationServices.GeofencingApi.addGeofences(
                        mGoogleApiClient,
                        geofencesToAdd,
                        mGeofencePendingIntent)
                        .setResultCallback(this);
            }
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            logSecurityException(securityException);
        }
    }

    private void logSecurityException(SecurityException securityException) {
        Log.e(TAG, "Invalid location permission. " +
                "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }


    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.e(TAG, "onConnectionSuspended: " + i);

        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onResult(Status status) {
        Log.d(TAG,"onResult");

        if (status.isSuccess()) {
            // Successfully registered

            // Update the UI. Adding geofences enables the Remove Geofences button, and removing
            // geofences enables the Add Geofences button.
            Log.i(TAG,mContext.getString(R.string.add_geofences_result_success));
            //Toast.makeText(mContext, mContext.getString(R.string.add_geofences_result_success), Toast.LENGTH_SHORT).show();

        } else if (status.hasResolution()) {
            // Google provides a way to fix the issue
                    /*
                    status.startResolutionForResult(
                            mContext,     // your current activity used to receive the result
                            RESULT_CODE); // the result code you'll look for in your
                    // onActivityResult method to retry registering
                    */
        } else {
            // No recovery. Weep softly or inform the user.
            Log.e(TAG, "Registering failed: " + status.getStatusMessage());
        }
    }

    /**
     * Get a PendingIntent to send with the request to add Geofences. Location
     * Services issues the Intent inside this PendingIntent whenever a geofence
     * transition occurs for the current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence
     * transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        Log.d(TAG,"getGeofencePendingIntent");

        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        } else {
            Intent intent = new Intent(mContext, ReceiveTransitionsIntentService.class);
            return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

}
