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


// https://maps.googleapis.com/maps/api/place/details/json?reference=CnRtAAAAfjFQWQEeuEmlVcWiW7M5mFhJzgIcXC0pyZYRNCoxd6bgHqSanrP5geZJiQ6seZhLvrF7OskQ7KQVJTCplhBhl-wl6tN4H4T0VmI-Vc0G87I81LBS4-MQ24SodZd32B3sD4C8KpmMHnn1ZRrQZUehmxIQiarzWr6Ie21P-lgf02pN4hoUMFw9xJPPPPrTfj7Y2jDhhKZGqWw&sensor=false&key=AIzaSyCQBT4HCyzFIwEgaItsjCKTRovII_E0wqU

public class PlacesServiceReceiver extends BroadcastReceiver {

    static final String LOG_TAG = "WeatherServiceReceiver";
    public static final String PLACES_SERVICE_ACTION = "PLACES_SERVICE_ACTION";
    public static final String SENSOR = "true";
    private static final int CONNECTION_TIMEOUT_MS = 10000;
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
//        this.preferences = this.context.getSharedPreferences(this.context.getString(R.string.shared_preferences), Context.MODE_PRIVATE);
        this.editor = this.preferences.edit();
        
        if(isConnected()) {
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
        
        String json = preferences.getString(this.context.getString(R.string.nearby_json), "");

        if(!json.isEmpty()) {
            Log.d(LOG_TAG, "JSON = " + JsonUtils.prettyPrint(json).substring(0, Integer.valueOf(context.getString(R.string.json_debug_length))));
            Nearby nearby = JsonUtils.createFromJson(Nearby.class, json);
//            editor.putBoolean(this.context.getString(R.string.nice_weather), factors.size() == 0);
//            editor.putString(this.context.getString(R.string.weather_reason), getWeatherReason(factors));
            editor.commit();
        } else {
            Log.d(LOG_TAG, "JSON was empty.");
        }

    }
    
    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager)this.context.getSystemService(this.context.CONNECTIVITY_SERVICE);
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
                editor.putString(PlacesServiceReceiver.this.context.getString(R.string.nearby_json), json);
                editor.commit();
            }
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
