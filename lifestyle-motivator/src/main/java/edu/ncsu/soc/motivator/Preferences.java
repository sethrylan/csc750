package edu.ncsu.soc.motivator;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;
import edu.ncsu.soc.motivator.R;

public class Preferences extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make sure default values are applied. In a real app, you would
        // want this in a shared function that is used to retrieve the
        // SharedPreferences wherever they are needed.
        PreferenceManager.setDefaultValues(Preferences.this, R.xml.preferences, false);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

    
    @Override
    public void onResume() {
        super.onResume();
    }

    
    @Override
    public void onPause() {
        super.onPause();
        Toast.makeText(this, "pausing", Toast.LENGTH_SHORT).show();
        
        AlarmManager am = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(WeatherServiceReceiver.ACTION);
        this.sendBroadcast(intent);
        intent = new Intent(PlacesServiceReceiver.ACTION);
        this.sendBroadcast(intent);
    }
}
