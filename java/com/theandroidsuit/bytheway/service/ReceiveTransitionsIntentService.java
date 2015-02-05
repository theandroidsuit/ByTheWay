package com.theandroidsuit.bytheway.service;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;


import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.theandroidsuit.bytheway.activity.DetailActivity;
import com.theandroidsuit.bytheway.activity.ListPositionActivity;
import com.theandroidsuit.bytheway.activity.MapsActivity;
import com.theandroidsuit.bytheway.R;
import com.theandroidsuit.bytheway.geofence.GeofenceUtils;
import com.theandroidsuit.bytheway.geofence.LocationServiceErrorMessages;
import com.theandroidsuit.bytheway.geofence.PositionManager;
import com.theandroidsuit.bytheway.sql.databaseTable.PositionEntity;
import com.theandroidsuit.bytheway.sql.utils.DBHelper;

/**
 * Created by Virginia Hernández on 14/01/15.
 */

/**
 * This class receives geofence transition events from Location Services, in the
 * form of an Intent containing the transition type and geofence id(s) that triggered
 * the event.
 */
public class ReceiveTransitionsIntentService extends IntentService {

    public final String TAG = this.getClass().getName();
    private DBHelper mDBHelper;


    /**
     * Sets an identifier for this class' background thread
     */
    public ReceiveTransitionsIntentService() {
        super("ReceiveTransitionsIntentService");
    }

    /**
     * Handles incoming intents
     * @param intent The Intent sent by Location Services. This Intent is provided
     * to Location Services (inside a PendingIntent) when you call addGeofences()
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "onHandleIntent");

        // Create a local broadcast Intent
        Intent broadcastIntent = new Intent();

        // Give it the category for all intents sent by the Intent Service
        broadcastIntent.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);

        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (null != event) {

            // First check for errors
            if (event.hasError()) {

                // Get the error code
                int errorCode = event.getErrorCode();

                // Get the error message
                String errorMessage = LocationServiceErrorMessages.getErrorString(this, errorCode);

                // Log the error
                Log.e(TAG, getString(R.string.geofence_transition_error_detail, errorMessage)
                );

                // Set the action and error message for the broadcast intent
                broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCE_ERROR)
                        .putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, errorMessage);

                // Broadcast the error *locally* to other components in this app
                LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

                // If there's no error, get the transition type and create a notification
            } else {

                // Get the type of transition (entry or exit)
                int transition = event.getGeofenceTransition();

                // Test that a valid transition was reported
                if ((transition == Geofence.GEOFENCE_TRANSITION_ENTER) ||
                    (transition == Geofence.GEOFENCE_TRANSITION_EXIT)) {


                    // Post a notification
                    List<Geofence> geofences = event.getTriggeringGeofences();
                    String[] geofenceIds = new String[geofences.size()];
                    for (int index = 0; index < geofences.size(); index++) {
                        geofenceIds[index] = geofences.get(index).getRequestId();
                    }
                    String ids = TextUtils.join(GeofenceUtils.GEOFENCE_ID_DELIMITER, geofenceIds);
                    List<String> names = new ArrayList<String>();

                    // This is for notification
                    for (String id : geofenceIds) {
                        // TODO
                        int idNum = Integer.valueOf(id).intValue();
                        try {
                            // Executing action against database
                            Dao dao = getHelper().getPositionDao();
                            PositionEntity pos = (PositionEntity) dao.queryForId(idNum);
                            names.add(pos.getTitle());
                        }catch (SQLException e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }

                    String transitionType = getTransitionString(transition);

                    // Now we must to register into APP
                    if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                        sendNotification(transitionType, names);
                        vibrate();
                    }


                    // Log the transition type and a message
                    Log.d(TAG, getString(R.string.geofence_transition_notification_title,
                                    transitionType,
                                    ids));

                    Log.d(TAG, getString(R.string.geofence_transition_notification_text));

                    // An invalid transition was reported
                } else {
                    // Always log as an error
                    Log.e(TAG, getString(R.string.geofence_transition_invalid_type, transition));
                }
            }
        }
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(500);
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the main Activity.
     * @param transitionType The type of transition that occurred.
     *
     */
    private void sendNotification(String transitionType, List<String> names) {

        Log.d(TAG, "sendNotification");

        Intent notificationIntent = null;

        if (names.size() > 1){
            // Create an explicit content Intent that starts the main Activity
            notificationIntent = new Intent(getApplicationContext(), ListPositionActivity.class);

            String idsStr = listToString(names);
            notificationIntent.putExtra(PositionManager.LIST_ID_POSITION_KEY, idsStr);
        }else{
            // Create an explicit content Intent that starts the main Activity
            notificationIntent = new Intent(getApplicationContext(), DetailActivity.class);

            notificationIntent.putExtra(PositionManager.ID_POSITION_KEY, names.get(0));
        }
        // Construct a task stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Adds the main Activity to the task stack as the parent
        stackBuilder.addParentStack(MapsActivity.class);
        
        // Push the content Intent onto the stack
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Set the notification contents
        builder.setSmallIcon(R.drawable.fav)
               .setContentTitle(getString(R.string.geofence_transition_notification_title, transitionType, names))
               .setContentText(getString(R.string.geofence_transition_notification_text))
               .setContentIntent(notificationPendingIntent)
               .setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

    private String listToString(List<String> names) {
        StringBuffer sb = new StringBuffer();
        for(String name: names){
            sb.append(name);
        }
        return sb.toString();
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     * @param transitionType A transition type constant defined in Geofence
     * @return A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        Log.d(TAG, "getTransitionString");

        switch (transitionType) {

            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);

            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);

            default:
                return getString(R.string.geofence_transition_unknown);
        }
    }


    private DBHelper getHelper() {
        if (mDBHelper == null) {
            mDBHelper = OpenHelperManager.getHelper(this, DBHelper.class);
        }
        return mDBHelper;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (mDBHelper != null) {
            OpenHelperManager.releaseHelper();
            mDBHelper = null;
        }
        return super.onUnbind(intent);
    }


}
