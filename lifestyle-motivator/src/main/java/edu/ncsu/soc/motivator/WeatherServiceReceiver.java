package edu.ncsu.soc.motivator;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class WeatherServiceReceiver extends BroadcastReceiver {

    private final String BUNDLE_NAME = "MyBundle"; 
    public static final String WEATHER_SERVICE_ACTION = "WEATHER_SERVICE_ACTION";


    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Alarm went off", Toast.LENGTH_SHORT).show();
    }

}
