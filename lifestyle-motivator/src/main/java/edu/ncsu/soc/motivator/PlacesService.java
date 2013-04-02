package edu.ncsu.soc.motivator;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.content.Context;

import android.widget.Toast;


// https://maps.googleapis.com/maps/api/place/details/json?reference=CnRtAAAAfjFQWQEeuEmlVcWiW7M5mFhJzgIcXC0pyZYRNCoxd6bgHqSanrP5geZJiQ6seZhLvrF7OskQ7KQVJTCplhBhl-wl6tN4H4T0VmI-Vc0G87I81LBS4-MQ24SodZd32B3sD4C8KpmMHnn1ZRrQZUehmxIQiarzWr6Ie21P-lgf02pN4hoUMFw9xJPPPPrTfj7Y2jDhhKZGqWw&sensor=false&key=AIzaSyCQBT4HCyzFIwEgaItsjCKTRovII_E0wqU
// https://maps.googleapis.com/maps/api/place/radarsearch/json?location=32.807476,-79.958239&radius=5000&types=gym&sensor=true&key=AIzaSyCQBT4HCyzFIwEgaItsjCKTRovII_E0wqU

public class PlacesService {

    public static final String PLACE_RADAR_JSON = "https://maps.googleapis.com/maps/api/place/radarsearch/json";
    public static final String SENSOR = "true";
    private static final int TIMEOUT_MS = 10000;
    private static final float MILLION = 1E6f;
    private static final int RADIUS_METERS = 5000;
    
    private Context context;

    public PlacesService(Context context) {
        this.context = context;
    }
    
    public void radarSearch(int latitude, int longitude) {
        String location = new StringBuilder()
            .append(latitude / MILLION)
            .append(",")
            .append(longitude / MILLION)
            .toString();
            
        String url = null;
        try {
            url = URLEncoder.encode(PLACE_RADAR_JSON + "&location=" + location + "&radius=" + RADIUS_METERS + "&types=" + "gym" + "&sensor="
                    + SENSOR + "&key=" + context.getString(edu.ncsu.soc.motivator.R.string.google_api_key), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(context, "Error encoding URL!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        String result = "";
        result = httpGet(url);
        if (result.isEmpty()) {
            Toast.makeText(context, "Can't retrieve Radar results!", Toast.LENGTH_SHORT).show();
            return;
        }
    }
    
    public String httpGet(String url) {
        String result = "";
//        try {
//            HttpParams parameters = new BasicHttpParams();
//            HttpConnectionParams.setConnectionTimeout(parameters, TIMEOUT_MS);
//            HttpClient client = new DefaultHttpClient(parameters);
//            HttpResponse response = null;
//            response = client.execute(new HttpGet(url));
//            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
//                result = EntityUtils.toString(response.getEntity());
//            }
//        } catch (Exception e) {
//            return null;
//        }
        return result;
    }


}
