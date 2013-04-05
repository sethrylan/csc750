package edu.ncsu.soc.motivator;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
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

import edu.ncsu.soc.motivator.domain.Forecast;
import edu.ncsu.soc.motivator.domain.WeatherFactor;

/**
 * Uses DarkSky API (https://developer.darkskyapp.com/docs/v2) to find weather
 * forecast for a location from the json returned by
 * https://api.forecast.io/forecast/APIKEY/LATITUDE,LONGITUDE
 * 
 * @author gaineys
 */
public class WeatherServiceReceiver extends BroadcastReceiver {

    static final String LOG_TAG = "WeatherServiceReceiver";
    public static final String WEATHER_SERVICE_ACTION = "WEATHER_SERVICE_ACTION";
    private static final float MILLION = 1E6f;
    private static final int CONNECTION_TIMEOUT_MS = 10000;

    private Context context;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    public void onReceive(Context context, Intent intent) {

        // Alternative to using an ASyncTask, you can run code in the main thread using this:
//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy); 

        this.context = context;

        // initialize preferences references
        this.preferences = this.context.getSharedPreferences(this.context.getString(R.string.shared_preferences), Context.MODE_PRIVATE);
        this.editor = this.preferences.edit();
        
        if(isConnected()) {
            float lastLatitude = this.preferences.getInt(this.context.getString(R.string.last_latitude_e6), 0) / MILLION;
            float lastLongitude = this.preferences.getInt(this.context.getString(R.string.last_longitude_e6), 0) / MILLION;

            // create URL in form:
            // https://api.forecast.io/forecast/key/lat,long
            String urlString = this.context.getString(R.string.darksky_forecast_root) + "/" + this.context.getString(R.string.darksky_api_key) + "/"
                    + lastLatitude + "," + lastLongitude;
            Log.d(LOG_TAG, "url = " + urlString);
            new RetreiveJsonTask().execute(urlString);
        }
        
        String json = preferences.getString(this.context.getString(R.string.weather_json), "");

        if(!json.isEmpty()) {
            Log.d(LOG_TAG, "JSON = " + JsonUtils.prettyPrint(json));
            Forecast forecast = JsonUtils.createFromJson(Forecast.class, json);
            List<WeatherFactor> factors = getWeatherFactors(forecast);
            editor.putBoolean(this.context.getString(R.string.nice_weather), factors.size() == 0);
            editor.putString(this.context.getString(R.string.weather_reason), getWeatherReason(factors));
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
        
    private List<WeatherFactor> getWeatherFactors(Forecast forecast) {
        List<WeatherFactor> factors = new ArrayList<WeatherFactor>();
        if(forecast.currently.cloudCover > Double.valueOf(this.context.getString(R.string.max_cloud_cover))) {
            factors.add(WeatherFactor.CLOUD);
        }
        if(forecast.currently.temperature > Double.valueOf(this.context.getString(R.string.max_temperature))) {
            factors.add(WeatherFactor.TEMPERATURE_HIGH);
        }
        if(forecast.currently.temperature < Double.valueOf(this.context.getString(R.string.min_temperature))) {
            factors.add(WeatherFactor.TEMPERATURE_LOW);
        }
        if(forecast.currently.windSpeed > Double.valueOf(this.context.getString(R.string.max_wind))) {
            factors.add(WeatherFactor.WIND);
        }
        if(forecast.currently.visibility > Double.valueOf(this.context.getString(R.string.min_visibility))) {
            factors.add(WeatherFactor.VISIBILITY);
        }
        if(forecast.currently.precipProbability > Double.valueOf(this.context.getString(R.string.max_precip_probability))) {
            factors.add(WeatherFactor.PRECIPITATION);
        }
        return factors;
    }
        
    private String getWeatherReason(List<WeatherFactor> factors) {
        StringBuilder result = new StringBuilder("");
        
        if(factors.size() > 0) {
            result.append("The weather is not nice because it is");
            String delimiter = " ";
            for(WeatherFactor factor : factors) {
                result.append(delimiter);
                result.append(factor.getText());
                delimiter = " and ";
            }
        }
        return result.toString();
    }
        
    
    
    class RetreiveJsonTask extends AsyncTask<String, Void, String> {
        static final String LOG_TAG = "WeatherServiceReceiver.RetreiveJsonTask";
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
                editor.putString(WeatherServiceReceiver.this.context.getString(R.string.weather_json), json);
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
