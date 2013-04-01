package edu.ncsu.soc.motivator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;


/**
 * Uses DarkSky API (https://developer.darkskyapp.com/docs/v2) to find weather forecast 
 * for a location from the json returned by https://api.forecast.io/forecast/APIKEY/LATITUDE,LONGITUDE
 * @author gaineys
 */
public class WeatherServiceReceiver extends BroadcastReceiver {

    public static final String WEATHER_SERVICE_ACTION = "WEATHER_SERVICE_ACTION";

    private Context context;
    
    // https://api.forecast.io/forecast/7d0f47e0b42b30ddbe05872d55fb695a/32.807476,-79.958239
        
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        if(isConnected()) {
//            String json = getResponseString();
        } else {
            Log.d("WeatherServiceReceiver", "Not connected.");
        }
        Toast.makeText(context, "WeatherServiceReceiver", Toast.LENGTH_SHORT).show();
    }

    private String getResponseString(URL url) {
        String result = "";
        try {
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
               result += line;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager)this.context.getSystemService(this.context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            return false;
        } else {
            return true;
        }
    }

    
}
