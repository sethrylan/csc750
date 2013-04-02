package edu.ncsu.soc.motivator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

/**
 * Uses DarkSky API (https://developer.darkskyapp.com/docs/v2) to find weather
 * forecast for a location from the json returned by
 * https://api.forecast.io/forecast/APIKEY/LATITUDE,LONGITUDE
 * 
 * @author gaineys
 */
public class WeatherServiceReceiver extends BroadcastReceiver {

    public static final String WEATHER_SERVICE_ACTION = "WEATHER_SERVICE_ACTION";

    private static final float MILLION = 1E6f;
    private static final int CONNECTION_TIMEOUT_MS = 5000;

    private Context context;
    private SharedPreferences preferences;
    
    @Override
    public void onReceive(Context context, Intent intent) {

        // Alternatively, the ASyncTask class may be used
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy); 

        this.context = context;

        // initialize preferences references
        this.preferences = this.context.getSharedPreferences(this.context.getString(R.string.shared_preferences), Context.MODE_PRIVATE);

        if (isConnected()) {
            float lastLatitude = this.preferences.getInt(this.context.getString(R.string.last_latitude_e6), 0) / MILLION;
            float lastLongitude = this.preferences.getInt(this.context.getString(R.string.last_longitude_e6), 0) / MILLION;

            // create URL in form:
            // https://api.forecast.io/forecast/7d0f47e0b42b30ddbe05872d55fb695a/32.807476,-79.958239
            String urlString = this.context.getString(R.string.darksky_forecast_root) + "/" + this.context.getString(R.string.darksky_api_key) + "/"
                    + lastLatitude + "," + lastLongitude;

            String json = getResponseString(urlString);
            Forecast forecast = JsonUtils.createFromJson(Forecast.class, json);
            Log.d("WeatherServiceReceiver", "url = " + urlString);
            Log.d("WeatherServiceReceiver", "JSON = " + JsonUtils.prettyPrint(json));

        } else {
            Log.d("WeatherServiceReceiver", "Not connected.");
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
        } catch(Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.context.getSystemService(this.context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            return false;
        } else {
            return true;
        }
    }
    
    class RetreiveJsonTask extends AsyncTask<String, Void, String> {
        private Exception exception;
        protected String doInBackground(String... urls) {
            try {
                String result = "";
                String urlString = urls[0];
                HttpParams parameters = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(parameters, CONNECTION_TIMEOUT_MS);
                HttpClient client = new DefaultHttpClient(parameters);
                HttpResponse response = client.execute(new HttpGet(urlString));
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    result = EntityUtils.toString(response.getEntity());
                }
                return result;
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(String json) {
            if(this.exception == null) {
                // send json to WeatherServiceReceiver
            }
        }
    }
}
