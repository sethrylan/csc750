package edu.ncsu.soc.motivator;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import edu.ncsu.soc.motivator.domain.Nearby;


public class PlacesServiceReceiver extends BroadcastReceiver {

    static final String LOG_TAG = "WeatherServiceReceiver";
    public static final String ACTION = "PLACES_SERVICE_ACTION";
    public static final String SENSOR = "true";
    private static final int CONNECTION_TIMEOUT_MS = 2000;
    private static final float MILLION = 1E6f;
    private static final int RADIUS_METERS = 5000;
    
    private Context context;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        // initialize preferences references
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.editor = this.preferences.edit();
        
        if(isConnected(context)) {
            float lastLatitude = this.preferences.getInt(this.context.getString(R.string.last_latitude_e6), 0) / MILLION;
            float lastLongitude = this.preferences.getInt(this.context.getString(R.string.last_longitude_e6), 0) / MILLION;

            String types = "park";
//            String types = "gym|bowling_alley|aquarium|art_gallery|bicycle_store";
            String urlString = this.context.getString(R.string.google_places_nearby_root) + "?" + "location=" + lastLatitude + "," + lastLongitude + "&radius=" + RADIUS_METERS + "&types=" + types + "&sensor=" + SENSOR 
                    + "&key=" + this.context.getString(R.string.google_api_key);
            Log.d(LOG_TAG, "url = " + urlString);
//            Toast.makeText(context, "Error encoding URL!", Toast.LENGTH_SHORT).show();
            new RetreiveJsonTask().execute(urlString);
        }
    }
    
    private static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Log.d(LOG_TAG, "Not connected.");
            return false;
        } else {
            return true;
        }
    }
    
    class RetreiveJsonTask extends AsyncTask<String, Void, String> {
        static final String LOG_TAG = "PlacesServiceReceiver.RetrieveJsonTask";
        private Exception exception;

        @Override
        protected String doInBackground(String... urls) {
            String urlString = urls[0];
            try {
                return getResponseString(urlString);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error retrieve json from: " + urlString);
                e.printStackTrace();
                return "";
            }
        }

        @Override
        protected void onPostExecute(String json) {
            if(this.exception == null) {
                if(!json.isEmpty()) {
                    editor.putLong(PlacesServiceReceiver.this.context.getString(R.string.last_places_update), System.currentTimeMillis());
                    Log.d(LOG_TAG, "JSON = " + JsonUtils.prettyPrint(json).substring(0, Integer.valueOf(context.getString(R.string.json_debug_length))));
                    editor.putString(PlacesServiceReceiver.this.context.getString(R.string.nearby_json), json);
                    // Nearby nearby = JsonUtils.createFromJson(Nearby.class, json);
                    editor.commit();
                } else {
                    Log.d(LOG_TAG, "JSON was empty.");
                }
            }
            
            long lastWeatherUpdate = preferences.getLong(context.getString(R.string.last_weather_update), 0);
            long lastPlacesUpdate = preferences.getLong(context.getString(R.string.last_places_update), 0);

            boolean isNice = preferences.getBoolean(context.getString(R.string.nice_weather), true);
            if(lastWeatherUpdate > 0 && lastPlacesUpdate > 0 ) {
                if(isNice) {
                    MotivatorAlarmService.sendNotification(context, MotivatorMapActivity.class, "The weather is " + isNice, "Nearest park is " + 1234 + " " + "meters" + " away");
                } else {
                    String weatherReason = preferences.getString(context.getString(R.string.weather_reason), "");
                    MotivatorAlarmService.sendNotification(context, MotivatorMapActivity.class, "The weather is not nice.", weatherReason , "Maybe you should find a gym");
                }            
            }
            
            // update status text in main activity
            
        }
        
        private String getResponseString(String urlString) {
            String result = "";
            try {
                HttpParams parameters = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(parameters, CONNECTION_TIMEOUT_MS);
                HttpClient client = new DefaultHttpClient(parameters);
                HttpResponse response = client.execute(new HttpGet(urlString));
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    result = EntityUtils.toString(response.getEntity());
                }
            } catch (Exception e) {
                e.printStackTrace();
                return result;
            }
            return result;
        }
    }

}
