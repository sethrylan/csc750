package edu.ncsu.soc.motivator;

import edu.ncsu.soc.motivator.R;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
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
    private static final int UPDATE_THRESHOLD_MS = 1000 * 60; // milliseconds
    private static final int UPDATE_THRESHOLD_METERS = 200; // meters
    public static final int WEATHER_INTERVAL_SECONDS = 30;
    public static final int PLACES_INTERVAL_SECONDS = 30;
    private static final float MILLION = 1E6f;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private NotificationManager notificationManager;
    private Notification notification;
    
    

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();


    /**
     * @see http://developer.android.com/reference/android/app/Service.html
     */
    public class LocalBinder extends Binder {
        MotivatorAlarmService getService() {
            return MotivatorAlarmService.this;
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


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
        this.preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        this.editor = this.preferences.edit();

        this.setRepeatingAlarm(this, WeatherServiceReceiver.ACTION, WEATHER_INTERVAL_SECONDS * 1000);
        this.setRepeatingAlarm(this, PlacesServiceReceiver.ACTION, PLACES_INTERVAL_SECONDS * 1000);
        
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
                
        this.cancelAlarm(WeatherServiceReceiver.class);
        this.cancelAlarm(PlacesServiceReceiver.class);
    }

    /**
     * Start the service and listen to GPS location
     */
    @Override
    public void onStart(Intent intent, int startId) {
        sendNotification(getApplicationContext(), MotivatorMapActivity.class, "Looking up current conditions", "acquiring current location...");
    }

    private static float yardsToMeters(float yards) {
        return (float)(yards * 0.9144);
    }

    private static float metersToYards(float meters) {
        return (float)(meters * 1.0936133);
    }
    
    /**
     * Uses deprecated methods to send status message
     * @param text      text of status message
     */
    protected void sendBanner(String text) {
        this.notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());
        Intent i = new Intent(this, MotivatorMapActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
    protected static void sendNotification(Context context, Class<? extends Activity> intentClass, String ...texts) {

        if(texts.length < 1) {
            throw new IllegalArgumentException();
        }

        // create notification
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_launcher);
        
        if(texts.length > 0) {
            notificationBuilder.setContentTitle(texts[0]);
            if(texts.length > 1) {
                notificationBuilder.setContentText(texts[1]);
                if(texts.length > 2) {
                    notificationBuilder.setSubText(texts[2]);
                }
            }
        }
        
        StringBuilder params = new StringBuilder();
        String delimiter = "";
        for(String text : texts) {
            params.append(delimiter + text);
            delimiter = ", ";
        }
        Log.d(LOG_TAG, "sendNotification(): " + "context: " + context + "texts: " + params.toString() + ", class: " + intentClass.getSimpleName());
        
        // creates explicit intent for an Activity
        Intent notificationIntent = new Intent(context, intentClass);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // stack builder object contains an artificial back stack for the activity, so 
        // that navigating backward from the Activity leads out of your application to the Home screen
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MotivatorMapActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(pendingIntent);
                
        Notification notification = notificationBuilder.build();
        notification.when = System.currentTimeMillis();
        notification.flags |= Notification.FLAG_NO_CLEAR; 
        
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        // ID allows you to update the notification later on.
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    /**
     * Listens for GPS updates
     */
    private class AlarmLocationListener implements LocationListener {

        /**
         * Updates the current Location and notification.
         */
        public void onLocationChanged(Location location) {
            MotivatorAlarmService.this.setRepeatingAlarm(MotivatorAlarmService.this, WeatherServiceReceiver.ACTION, WEATHER_INTERVAL_SECONDS * 1000);
            MotivatorAlarmService.this.setRepeatingAlarm(MotivatorAlarmService.this, PlacesServiceReceiver.ACTION, PLACES_INTERVAL_SECONDS * 1000);
            
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

//    protected void setRepeatingAlarm(Class<? extends BroadcastReceiver> c, int intervalMilliseconds) {
//        this.setRepeatingAlarm(this, c, intervalMilliseconds);
//    }
    
    protected void setRepeatingAlarm(Context context, String action, int intervalMilliseconds) {
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        am.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), intervalMilliseconds, pendingIntent);
    }

//    protected void setRepeatingAlarm(Context context, Class<? extends BroadcastReceiver> c, int intervalMilliseconds) {
//        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
//        Intent intent = new Intent(context, c);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
//        am.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), intervalMilliseconds, pendingIntent);
//    }
    
    protected void cancelAlarm(Class<? extends BroadcastReceiver> c) {
        Intent intent = new Intent(this, c);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    /**
     * @see http://developer.android.com/reference/android/content/SharedPreferences.OnSharedPreferenceChangeListener.html
     */
//    @Override
//    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
//        this.setRepeatingAlarm(this, WeatherServiceReceiver.class, WEATHER_INTERVAL_SECONDS * 1000);
//        Toast.makeText(this, "Updated preferences.", Toast.LENGTH_SHORT).show();
//    }

}
