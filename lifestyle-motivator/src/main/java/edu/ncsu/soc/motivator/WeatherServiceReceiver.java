package edu.ncsu.soc.motivator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
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
    private static final int CONNECTION_TIMEOUT_MS = 1000;

    private Context context;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        // Alternative to using an ASyncTask, you can run code in the main thread using:
//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy); 
        
        this.context = context;

        // initialize preferences references
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.editor = this.preferences.edit();
        
        if(isConnected()) {
            new RetreiveJsonTask().execute(getUrl());
        }
    }

    
    private String getUrl() {
        float lastLatitude = this.preferences.getInt(this.context.getString(R.string.last_latitude_e6), 0) / MILLION;
        float lastLongitude = this.preferences.getInt(this.context.getString(R.string.last_longitude_e6), 0) / MILLION;
        // create URL in form:
        // https://api.forecast.io/forecast/key/lat,long
        String urlString = this.context.getString(R.string.darksky_forecast_root) + "/" + this.context.getString(R.string.darksky_api_key) + "/"
                + lastLatitude + "," + lastLongitude;
        Log.d(LOG_TAG, "url = " + urlString);
        return urlString;
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
        
    private double getPreferenceDouble(int resourceId, int defaultResourceId) {
        String defaultValue = this.context.getString(defaultResourceId, "0.0");
        String keyName = this.context.getString(resourceId);
        return Double.valueOf(this.preferences.getString(keyName, defaultValue));
    }
    
    // Preferences are only set after the user goes to the preference screen, so there are many checks to set the default values.
    private List<WeatherFactor> getWeatherFactors(Forecast forecast) {
        List<WeatherFactor> factors = new ArrayList<WeatherFactor>();
        double maxCloudCover = getPreferenceDouble(R.string.max_cloud_cover, R.string.default_max_cloud_cover);
        double maxTemperature = getPreferenceDouble(R.string.max_temperature, R.string.default_max_temperature);
        double minTemperature = getPreferenceDouble(R.string.min_temperature, R.string.default_min_temperature);
        double maxWindSpeed = getPreferenceDouble(R.string.max_wind, R.string.default_max_wind);
        double minVisibility = getPreferenceDouble(R.string.min_visibility, R.string.default_min_visibility);
        double maxPrecipProbability = getPreferenceDouble(R.string.max_precip_probability, R.string.default_max_precip_probability);

        if(forecast.currently.cloudCover > maxCloudCover) {
            Log.d(LOG_TAG, "cloudCover:" + forecast.currently.cloudCover + " > " + maxCloudCover);
            factors.add(WeatherFactor.CLOUD);
        }
        if(forecast.currently.temperature > maxTemperature) {
            Log.d(LOG_TAG, "temperature:" + forecast.currently.temperature + " > " + maxTemperature);
            factors.add(WeatherFactor.TEMPERATURE_HIGH);
        }
        if(forecast.currently.temperature < minTemperature) {
            Log.d(LOG_TAG, "temperature:" + forecast.currently.temperature + " < " + minTemperature);
            factors.add(WeatherFactor.TEMPERATURE_LOW);
        }
        if(forecast.currently.windSpeed > maxWindSpeed) {
            Log.d(LOG_TAG, "windSpeed:" + forecast.currently.windSpeed + " > " + maxWindSpeed);
            factors.add(WeatherFactor.WIND);
        }
        if(forecast.currently.visibility < minVisibility) {
            Log.d(LOG_TAG, "visibility:" + forecast.currently.visibility + " < " + minVisibility);
            factors.add(WeatherFactor.VISIBILITY);
        }
        if(forecast.currently.precipProbability > maxPrecipProbability) {
            Log.d(LOG_TAG, "precipProbability:" + forecast.currently.precipProbability + " > " + maxPrecipProbability);
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
        
    
    /**
     * Any UI access must be in pre/post-execute methods
     */
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
                if(!json.isEmpty()) {
                    Log.d(LOG_TAG, "JSON = " + JsonUtils.prettyPrint(json).substring(0, Integer.valueOf(context.getString(R.string.json_debug_length))));
                    Forecast forecast = JsonUtils.createFromJson(Forecast.class, json);
                    List<WeatherFactor> factors = getWeatherFactors(forecast);
                    editor.putBoolean(WeatherServiceReceiver.this.context.getString(R.string.nice_weather), factors.size() == 0);
                    editor.putString(WeatherServiceReceiver.this.context.getString(R.string.weather_reason), getWeatherReason(factors));
                    editor.commit();
                } else {
                    Log.d(LOG_TAG, "JSON was empty.");
                }
                
                boolean isNice = preferences.getBoolean(context.getString(R.string.nice_weather), true);
                if(isNice) {
                    MotivatorAlarmService.sendNotification(context, MotivatorMapActivity.class, "The weather is " + isNice, "Nearest park is ..");
                } else {
                    String weatherReason = preferences.getString(context.getString(R.string.weather_reason), "");
                    MotivatorAlarmService.sendNotification(context, MotivatorMapActivity.class, "The weather is not nice.", weatherReason , "Maybe you should find a gym");
                }
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
