package com.theandroidsuit.bytheway.geofence;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;
import com.theandroidsuit.bytheway.R;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Virginia Hernández on 14/01/15.
 */

/**
 * Class for connecting to Location Services and removing geofences.
 * <p>
 * <b>
 * Note: Clients must ensure that Google Play services is available before removing geofences.
 * </b> Use GooglePlayServicesUtil.isGooglePlayServicesAvailable() to check.
 * <p>
 * To use a GeofenceRemover, instantiate it, then call either RemoveGeofencesById() or
 * RemoveGeofencesByIntent(). Everything else is done automatically.
 *
 */
public class GeofenceRemover implements GoogleApiClient.ConnectionCallbacks,
                                        GoogleApiClient.OnConnectionFailedListener,
                                        ResultCallback<Status>{

    public final String TAG = this.getClass().getName();

    private Context mContext;
    protected GoogleApiClient mGoogleApiClient; // Provides the entry point to Google Play services.
    protected List<Geofence> geofencesToRemove;
    private PendingIntent mGeofencePendingIntent; // Used when requesting to add or remove geofences.



    public GeofenceRemover (Context ctx){
        Log.d(TAG,"GeofenceRemover");

        this.mContext = ctx;
    }

    /**
     * Removes geofences, which stops further notifications when the device enters or exits
     * previously registered geofences.
     */
    public void removeGeofences(List<Geofence> geofences) {
        Log.d(TAG,"removeGeofences");

        geofencesToRemove = geofences;

        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG,"onConnected");

        if (!mGoogleApiClient.isConnected()) {
            //Toast.makeText(mContext, mContext.getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if (null != geofencesToRemove && !geofencesToRemove.isEmpty()) {
                // Remove geofences.
                List<String> idsGFList = geofencesToIds(geofencesToRemove);

                LocationServices.GeofencingApi.removeGeofences(
                        mGoogleApiClient,
                        idsGFList)
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

    public PendingIntent getRequestPendingIntent() {
        Log.d(TAG,"getRequestPendingIntent");

        return getGeofencePendingIntent();
    }

    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.e(TAG, "onConnectionSuspended: " + cause);

        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }





    private List<String> geofencesToIds(List<Geofence> values) {
        Log.d(TAG,"geofencesToIds");

        List<String> toRemove = new ArrayList<>();

        for(Geofence item: values){
            toRemove.add(item.getRequestId());
        }

        return toRemove;
    }



    /**
     * Runs when the result of calling addGeofences() and removeGeofences() becomes available.
     * Either method can complete successfully or with an error.
     *
     * Since this activity implements the {@link ResultCallback} interface, we are required to
     * define this method.
     *
     * @param status The Status returned through a PendingIntent when addGeofences() or
     * removeGeofences() get called.
     */
   public void onResult(Status status) {
        Log.d(TAG,"onResult");

        if (status.isSuccess()) {
            // Successfully removed

            // Update the UI. Adding geofences enables the Remove Geofences button, and removing
            // geofences enables the Add Geofences button.
            Toast.makeText(mContext,
                    mContext.getString(R.string.remove_geofences_intent_success),
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = LocationServiceErrorMessages.getRemoveError(mContext,status.getStatusCode());

            Log.e(TAG, errorMessage);
        }
   }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        Log.d(TAG,"getGeofencePendingIntent");

        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(mContext, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}