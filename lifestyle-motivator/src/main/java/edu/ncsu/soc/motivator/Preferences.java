package edu.ncsu.soc.motivator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    private EditTextPreference maxTemperaturePreference;
    private EditTextPreference minTemperaturePreference;
    private EditTextPreference maxCloudPreference;
    private EditTextPreference maxWindPreference;
    private EditTextPreference minVisibilityPreference;
    private EditTextPreference maxPrecipProbabilityPreference;
    
    protected SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences =  PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        // Make sure default values are applied. In a real app, you would
        // want this in a shared function that is used to retrieve the
        // SharedPreferences wherever they are needed.
        PreferenceManager.setDefaultValues(Preferences.this, R.xml.preferences, false);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        
        // here the SharedPreference key is used as the layout ID
        this.maxTemperaturePreference = (EditTextPreference)getPreferenceScreen().findPreference(getString(R.string.max_temperature));
        this.minTemperaturePreference = (EditTextPreference)getPreferenceScreen().findPreference(getString(R.string.min_temperature));
        this.maxCloudPreference = (EditTextPreference)getPreferenceScreen().findPreference(getString(R.string.max_cloud_cover));
        this.maxWindPreference = (EditTextPreference)getPreferenceScreen().findPreference(getString(R.string.max_wind));
        this.minVisibilityPreference = (EditTextPreference)getPreferenceScreen().findPreference(getString(R.string.min_visibility));
        this.maxPrecipProbabilityPreference = (EditTextPreference)getPreferenceScreen().findPreference(getString(R.string.max_precip_probability));

    }
    
    @Override
    public void onResume() {
        super.onResume();
        this.maxTemperaturePreference.setSummary(preferences.getString(getString(R.string.max_temperature), "0.0"));
        this.minTemperaturePreference.setSummary(preferences.getString(getString(R.string.min_temperature), "0.0"));
        this.maxCloudPreference.setSummary(preferences.getString(getString(R.string.max_cloud_cover), "0.0"));
        this.maxWindPreference.setSummary(preferences.getString(getString(R.string.max_wind), "0.0"));
        this.minVisibilityPreference.setSummary(preferences.getString(getString(R.string.min_visibility), "0.0"));
        this.maxPrecipProbabilityPreference.setSummary(preferences.getString(getString(R.string.max_precip_probability), "0.0"));

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    
    @Override
    public void onPause() {
        super.onPause();        
        Intent intent = new Intent(WeatherServiceReceiver.ACTION);
        this.sendBroadcast(intent);
        intent = new Intent(PlacesServiceReceiver.ACTION);
        this.sendBroadcast(intent);
        
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        Toast.makeText(this, "Recalculating Weather Preferences.", Toast.LENGTH_LONG).show();
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.max_temperature))) {
            maxTemperaturePreference.setSummary(sharedPreferences.getString(key, "0.0"));
        }
        if (key.equals(getString(R.string.min_temperature))) {
            minTemperaturePreference.setSummary(sharedPreferences.getString(key, "0.0"));
        }
        if (key.equals(getString(R.string.max_cloud_cover))) {
            maxCloudPreference.setSummary(sharedPreferences.getString(key, "0.0"));
        }
        if (key.equals(getString(R.string.max_wind))) {
            maxWindPreference.setSummary(sharedPreferences.getString(key, "0.0"));
        }
        if (key.equals(getString(R.string.min_visibility))) {
            minVisibilityPreference.setSummary(sharedPreferences.getString(key, "0.0"));
        }
        if (key.equals(getString(R.string.max_precip_probability))) {
            maxPrecipProbabilityPreference.setSummary(sharedPreferences.getString(key, "0.0"));
        }
    }
}
