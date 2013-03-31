package edu.ncsu.soc.motivator;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * Service runs in the background and listens to the GPS.
 */
public class MotivatorAlarmService extends Service {

    private static final int SERVICE_ID = 1234567;

    private static final int NOTIFICATION_ID = 42;
    private static final int PENDING_INTENT_REQUEST_CODE1 = 4242;
    private static final int PENDING_INTENT_REQUEST_CODE2 = 424242;
    private static final int MIN_TIME_TO_UPDATE_LOCATION = 5000; // milliseconds
    private static final int MIN_DISTANCE_TO_UPDATE_LOCATION = 5; // meters

    private LocationManager lm;
    private Location currentLoc;
    private NotificationManager notificationManager;
    private Notification notification;

    private int proximity;
    private String proximityUnit;
    private BusStop busStop;

    /**
     * setup service managers.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_TO_UPDATE_LOCATION, MIN_DISTANCE_TO_UPDATE_LOCATION, new AlarmLocationListener());
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notification = new Notification(R.drawable.ic_launcher, "Starting Lifestyle Motivator Service", System.currentTimeMillis());
        Intent i = new Intent(this, MotivatorMapActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi_test = PendingIntent.getActivity(this, 0, i, 0);
        notification.setLatestEventInfo(this, "Lifestyle Motivator - srgainey", "Checking for opportunities...", pi_test);
        notification.flags |= Notification.FLAG_NO_CLEAR;
//        startForeground(SERVICE_ID, ntf);

    }

    /**
     * Stop the service
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancel(NOTIFICATION_ID);
        Intent alarmIntent = new Intent(getApplicationContext(), MotivatorMapActivity.class);
        PendingIntent pendingIntentAlarm = PendingIntent.getBroadcast(getApplicationContext(), PENDING_INTENT_REQUEST_CODE1, alarmIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        pendingIntentAlarm.cancel();
        Log.d("ALARMSERVICE", "current alarm is destroyed");
    }

    /**
     * Start the service and listen to GPS location
     */
    @Override
    public void onStart(Intent intent, int startId) {

        proximity = intent.getIntExtra("proximity", 1);
        proximityUnit = "meters";
        busStop = intent.getParcelableExtra("busstop");
        busStop = new BusStop();
        busStop.setLatitude(new Double("35.772052"));
        busStop.setLongitude(new Double("-78.673718"));

        Uri ringtoneUri = intent.getParcelableExtra("ringtoneUri");
        boolean vibration = intent.getBooleanExtra("vibration", false);

        PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), PENDING_INTENT_REQUEST_CODE2, new Intent(getApplicationContext(),
                this.getClass()), PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setLatestEventInfo(getApplicationContext(), "Bus Stop: " + "busstopname", "acquiring location...", pi);

        notificationManager.notify(NOTIFICATION_ID, notification);

        Log.d("ALARMSERVICE", "prox: " + proximity + ", units: " + proximityUnit + ", stop: " + busStop + ", ringtone: " + ringtoneUri + ", vibration: "
                + vibration);

        Intent alarmIntent = new Intent(getApplicationContext(), MotivatorMapActivity.class);

        alarmIntent.putExtra("ringtoneUri", ringtoneUri);
        alarmIntent.putExtra("vibration", vibration);
        PendingIntent pendingIntentAlarm = PendingIntent.getBroadcast(getApplicationContext(), PENDING_INTENT_REQUEST_CODE1, alarmIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        float proximityInput = (float) proximity;
        // if (proximityUnit.equals("Yards"))
        // proximityInput = convertYardsToMeters(proximityInput);
        lm.addProximityAlert(busStop.getLatitude(), busStop.getLongitude(), proximityInput, -1, pendingIntentAlarm);
    }

    public float convertYardsToMeters(float yards) {
        return (float) (yards * 0.9144);
    }

    public float convertMetersToYards(float meters) {
        return (float) (meters * 1.0936133);
    }

    /**
     * Service requires that this function must be overridden, but we don't need
     * it.
     */
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@link AlarmLocationListener} listens to the GPS, and updates the users
     * location
     * 
     * @author David Nufer
     * 
     */
    private class AlarmLocationListener implements LocationListener {

        /**
         * Updates the current Location and notification.
         */
        public void onLocationChanged(Location location) {
            currentLoc = location;
            Location target = new Location(location);
            target.setLatitude(busStop.getLatitude());
            target.setLongitude(busStop.getLongitude());
            float dist = currentLoc.distanceTo(target); // in meters
            // if (proximityUnit.equals("Yards")) {
            // dist = convertMetersToYards(dist);
            // }

            PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(getApplicationContext(), this.getClass()),
                    PendingIntent.FLAG_UPDATE_CURRENT);
            notification.setLatestEventInfo(getApplicationContext(), "Bus Stop: " + busStop.getName(), dist + " " + proximityUnit + " away", pi);
            notification.when = System.currentTimeMillis();
            notificationManager.notify(NOTIFICATION_ID, notification);
            Log.d("ALARMSERVICE", "location updated");
        }

        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
    }

}
