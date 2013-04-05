package edu.ncsu.soc.motivator;

import java.util.Calendar;

import edu.ncsu.soc.motivator.R;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.Toast;

/**
 * Service runs in the background and sends notifications based on GPS/Places data
 */
public class MotivatorAlarmService extends Service {
    
    static final String LOG_TAG = "MotivatorAlarmService";

    protected SharedPreferences preferences;
    protected SharedPreferences.Editor editor;

    private static final int NOTIFICATION_ID = 42;
    private static final int UPDATE_THRESHOLD_MS = 5000; // milliseconds
    private static final int UPDATE_THRESHOLD_METERS = 5; // meters
    private static final int INTERVAL_SECONDS = 5;
    private static final float MILLION = 1E6f;

    private LocationManager locationManager;
    private LocationListener locationListener;
//    private Location currentLoc;
    private NotificationManager notificationManager;
    private Notification notification;

    private int proximity;
    private String proximityUnit;
//    private BusStop busStop;
    
    private WeatherServiceReceiver weatherService = null;

    /**
     * setup service managers.
     */
    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate");

        super.onCreate();
        
        // start location manager with non-aggressive threshold values to preserve battery life
        this.locationListener = new AlarmLocationListener();
        this.locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_THRESHOLD_MS, UPDATE_THRESHOLD_METERS, this.locationListener);

        // initialize notification service
        this.notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        // initialize preferences references
        this.preferences = getApplicationContext().getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE);
        this.editor = this.preferences.edit();

        AlarmManager alarmManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);         
        Intent intent = new Intent( WeatherServiceReceiver.WEATHER_SERVICE_ACTION );
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar triggerTime = Calendar.getInstance();
        triggerTime.setTimeInMillis(System.currentTimeMillis());
//        triggerTime.add(Calendar.SECOND, INTERVAL_SECONDS);         
        alarmManager.setInexactRepeating(AlarmManager.RTC, triggerTime.getTimeInMillis(), INTERVAL_SECONDS * 1000, pendingIntent);

        /*
         * Registration here equivalent to declarative method in AndroidManifest:
         *      <receiver android:name=".WeatherServiceReceiver">
         *           <intent-filter>
         *               <action android:name="WEATHER_SERVICE_ACTION" />
         *           </intent-filter>
         *       </receiver>
         */
        this.weatherService = new WeatherServiceReceiver();
        registerReceiver(this.weatherService, new IntentFilter(WeatherServiceReceiver.WEATHER_SERVICE_ACTION));
        
        sendBanner("Starting Lifestyle Motivator Service");
    }

    /**
     * Stop the service
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        this.locationManager.removeUpdates(this.locationListener);
        this.notificationManager.cancel(NOTIFICATION_ID);
        
//        Intent alarmIntent = new Intent(getApplicationContext(), MotivatorMapActivity.class);
//        PendingIntent pendingIntentAlarm = PendingIntent.getBroadcast(getApplicationContext(), PENDING_INTENT_REQUEST_CODE1, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
//        pendingIntentAlarm.cancel();
        
        if(this.weatherService != null) {
            unregisterReceiver(this.weatherService);
        }
    }

    /**
     * Start the service and listen to GPS location
     */
    @Override
    public void onStart(Intent intent, int startId) {

        proximity = intent.getIntExtra("proximity", 1);
        proximityUnit = "meters";
//        busStop = intent.getParcelableExtra("busstop");
//        busStop = new BusStop();
//        busStop.setLatitude(new Double("35.772052"));
//        busStop.setLongitude(new Double("-78.673718"));

//        Uri ringtoneUri = intent.getParcelableExtra("ringtoneUri");
//        boolean vibration = intent.getBooleanExtra("vibration", false);
                
        sendNotification(getApplicationContext(), MotivatorMapActivity.class, "Looking up current conditions", "acquiring current location...");

//        sendNotification(getApplicationContext(), "Bus Stop: " + "busstopname", "acquiring location...", MotivatorMapActivity.class);

//        Intent alarmIntent = new Intent(getApplicationContext(), MotivatorMapActivity.class);
//        alarmIntent.putExtra("ringtoneUri", ringtoneUri);
//        alarmIntent.putExtra("vibration", vibration);
//        PendingIntent pendingIntentAlarm = PendingIntent.getBroadcast(getApplicationContext(), PENDING_INTENT_REQUEST_CODE1, alarmIntent,  PendingIntent.FLAG_CANCEL_CURRENT);
//        float proximityInput = (float) proximity;
        // if (proximityUnit.equals("Yards"))
        // proximityInput = convertYardsToMeters(proximityInput);
//        lm.addProximityAlert(busStop.getLatitude(), busStop.getLongitude(), proximityInput, -1, pendingIntentAlarm);
    }

    private static float yardsToMeters(float yards) {
        return (float)(yards * 0.9144);
    }

    private static float metersToYards(float meters) {
        return (float)(meters * 1.0936133);
    }

    /**
     * Unneeded abstract method
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    /**
     * Uses deprecated methods to send status message
     * @param text      text of status message
     */
    protected void sendBanner(String text) {
        this.notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());
        Intent i = new Intent(this, MotivatorMapActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi_test = PendingIntent.getActivity(this, 0, i, 0);
        this.notification.setLatestEventInfo(this, "", "", null);
        this.notification.flags |= Notification.FLAG_NO_CLEAR;
        this.notificationManager.notify(NOTIFICATION_ID, this.notification);
    }

    /**
     * Method to send notification
     * @param context       context
     * @param intentClass   notification intent
     * @param texts         context title, content text and subtext
     */
    protected void sendNotification(Context context, Class<? extends Activity> intentClass, String ...texts) {


        // create notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext()).setSmallIcon(R.drawable.ic_launcher);
        
        if(texts.length > 0) {
            notificationBuilder.setContentTitle(texts[0]);
            Log.d(LOG_TAG, "sendNotification(): " + "context: " + context + ", title: " + texts[0] + ", class: " + intentClass.getSimpleName());
            if(texts.length > 1) {
                notificationBuilder.setContentText(texts[1]);
                Log.d(LOG_TAG, "sendNotification(): " + "context: " + context + ", title: " + texts[0] + ", text: " + texts[1] + ", class: " + intentClass.getSimpleName());
                if(texts.length > 2) {
                    notificationBuilder.setSubText(texts[2]);
                    Log.d(LOG_TAG, "sendNotification(): " + "context: " + context + ", title: " + texts[0] + ", text: " + texts[1] + ", subtext :" + texts[2] + ", class: " + intentClass.getSimpleName());
                }
            }
        }
        
        // creates explicit intent for an Activity
        Intent notificationIntent = new Intent(context, intentClass);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // stack builder object contains an artificial back stack for the activity, so 
        // that navigating backward from the Activity leads out of your application to the Home screen
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addParentStack(MotivatorMapActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(pendingIntent);
                
        this.notification = notificationBuilder.build();
        this.notification.when = System.currentTimeMillis();
        this.notification.flags |= Notification.FLAG_NO_CLEAR; 
        
        // ID allows you to update the notification later on.
        this.notificationManager.notify(NOTIFICATION_ID, this.notification);
    }

    /**
     * Listens for GPS updates
     */
    private class AlarmLocationListener implements LocationListener {

        /**
         * Updates the current Location and notification.
         */
        public void onLocationChanged(Location location) {
//            currentLoc = location;
//            Location target = new Location(location);
//            target.setLatitude(busStop.getLatitude());
//            target.setLongitude(busStop.getLongitude());
//            float dist = currentLoc.distanceTo(target); // in meters
            // if (proximityUnit.equals("Yards")) {
            // dist = convertMetersToYards(dist);
            // }

            
            boolean isNice = MotivatorAlarmService.this.preferences.getBoolean(getString(R.string.nice_weather), true);
            if(isNice) {
                sendNotification(getApplicationContext(), MotivatorMapActivity.class, "The weather is " + isNice, "Nearest park is " + 1234 + " " + proximityUnit + " away");
            } else {
                String weatherReason = MotivatorAlarmService.this.preferences.getString(getString(R.string.weather_reason), "");
                sendNotification(getApplicationContext(), MotivatorMapActivity.class, "The weather is not nice.", weatherReason , "Maybe you should find a gym");
            }
//            sendNotification(getApplicationContext(), "Bus Stop: " + "name", 1234 + " " + proximityUnit + " away", MotivatorMapActivity.class);
            
            int latitude = (int)(location.getLatitude() * MILLION);
            int longitude = (int)(location.getLongitude() * MILLION);            
            editor.putInt(getString(R.string.last_latitude_e6), latitude);
            editor.putInt(getString(R.string.last_longitude_e6), longitude);
            editor.commit();
        }

        public void onProviderDisabled(String provider) {

        }

        public void onProviderEnabled(String provider) {

        }

        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    }

}
