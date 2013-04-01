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

    public WeatherServiceReceiver() {
    }

    public WeatherServiceReceiver(Context context, Bundle extras, int timeoutInSeconds) {
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, this.getClass());
        intent.putExtra(BUNDLE_NAME, extras);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(System.currentTimeMillis());
        time.add(Calendar.SECOND, timeoutInSeconds);
        alarmMgr.set(AlarmManager.RTC, time.getTimeInMillis(), pendingIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Alarm went off", Toast.LENGTH_SHORT).show();
    }

}
