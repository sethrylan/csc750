package edu.ncsu.soc.motivator;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import edu.ncsu.soc.motivator.R;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MotivatorMapActivity extends MapActivity {

    protected SharedPreferences preferences;
    protected SharedPreferences.Editor editor;

    static final String LOG_TAG = "MotivatorMap";
    
    private static final int UPDATE_THRESHOLD_METERS = 0;
    private static final int UPDATE_THRESHOLD_MS = 0;

    private MapView mapView;
    private ContactsMapOverlay mapOverlay;
    private MyLocationOverlay myLocationOverlay;
    private Location currentLocation;
    private LocationManager locationManager;
    private LocationListener locationListener;
    
    private static final float MILLION = 1E6f;
    
    // Default locations only used for time application starts
//    private static final GeoPoint DEFAULT_GEOPOINT = new GeoPoint(32807476, -79958239); // Charleston location
    private static final GeoPoint DEFAULT_GEOPOINT = new GeoPoint(35772052, -78673718); // NCSU location
    private static final Location DEFAULT_LOCATION = geoPointToLocation(DEFAULT_GEOPOINT);

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);

        Location initialLocation = null;
        GeoPoint initialGeoPoint = null;
        
        this.preferences = this.getSharedPreferences(getString(R.string.shared_preferences), Context.MODE_PRIVATE);  
        this.editor = this.preferences.edit();

        // Retrieve GPS coordinates in form e6 (GeoPoint) form
        Integer lastLatitude = this.preferences.getInt(getString(R.string.last_latitude_e6), Integer.MIN_VALUE);
        Integer lastLongitude = this.preferences.getInt(getString(R.string.last_longitude_e6), Integer.MIN_VALUE);
        
        if (lastLatitude.equals(Integer.MIN_VALUE) || lastLongitude.equals(Integer.MIN_VALUE)) {            
            Toast toast = Toast.makeText(getApplicationContext(), "Using default location for first time use.", Toast.LENGTH_LONG);
            toast.show();
            initialGeoPoint = DEFAULT_GEOPOINT;
            initialLocation = DEFAULT_LOCATION;
        } else {
            initialGeoPoint = new GeoPoint(lastLatitude, lastLongitude);
            initialLocation = geoPointToLocation(initialGeoPoint);
        }
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.motivator_map);

        // configure the map and set initial position until GPS is available
        this.mapView = (MapView)findViewById(R.id.MapView);
        this.mapView.getController().setCenter(initialGeoPoint);
        this.mapView.getController().setZoom(15);

        // create the map overlay
        Drawable marker = getResources().getDrawable(R.drawable.marker);
        marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
        this.mapOverlay = new ContactsMapOverlay(this, marker);
        this.mapView.getOverlays().add(mapOverlay);
        this.myLocationOverlay = new MyLocationOverlay(this, mapView);
        this.myLocationOverlay.enableCompass();
        this.myLocationOverlay.enableMyLocation();
        this.myLocationOverlay.runOnFirstFix(new Runnable() {
            public void run() {
                mapView.getController().animateTo(myLocationOverlay.getMyLocation());
            }
        });
        mapView.getOverlays().add(myLocationOverlay);

        // configure the contacts list
        ListView list = (ListView)findViewById(R.id.ContactsList);
        list.setAdapter(new ContactsViewAdapter(this));
        
        // Add location listener
        // get reference to Location Manager
        this.locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        
        // check if GPS is enabled; if not, the send user to GPS settings
        boolean enabled = this.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        } 

        // initialize current location
        this.currentLocation = new Location(initialLocation);

        // Define an listener in an inner class
        this.locationListener = new MotivatorMapLocationListener(this.getApplicationContext(), currentLocation, mapView); 
                
        // register button clicklisteners
        // equivalent to <Button ... android:onClick="stopServiceButton"/> in layout xml
        Button startButton = ((Button)findViewById(R.id.StartServiceButton));
        Button stopButton = ((Button)findViewById(R.id.StopServiceButton));
        startButton.setOnClickListener(mStartButtonListener);
        startButton.setEnabled(true);
        stopButton.setOnClickListener(mStopButtonListener);
        stopButton.setEnabled(false);
    }

    private OnClickListener mStartButtonListener = new OnClickListener() {
        public void onClick(View v) {
            ((Button)findViewById(R.id.StartServiceButton)).setEnabled(false);
            Intent intent = new Intent(MotivatorMapActivity.this, MotivatorAlarmService.class);
//            intent.putExtra("vibration", true);
            startService(intent);
            ((Button)findViewById(R.id.StopServiceButton)).setEnabled(true);
        }
    };
    
    private OnClickListener mStopButtonListener = new OnClickListener() {
        public void onClick(View v) {
            ((Button)findViewById(R.id.StopServiceButton)).setEnabled(false);
            stopService(new Intent(MotivatorMapActivity.this, MotivatorAlarmService.class));
            ((Button)findViewById(R.id.StartServiceButton)).setEnabled(true);
        }
    };

    public void addMarkerAtCurrentLocation(String markerName) {
        int latitude = (int)(this.currentLocation.getLatitude() * MILLION);
        int longitude = (int)(this.currentLocation.getLongitude() * MILLION);
        
        this.mapOverlay.addMarker(markerName, new GeoPoint(latitude, longitude));
        
        // redraw the map
        MapView mapView = (MapView) findViewById(R.id.MapView);
        mapView.invalidate();
    }

    /**
     * Disables all location features with use GPS
     */
    private void disableLocation() {
        this.myLocationOverlay.disableMyLocation();
        this.myLocationOverlay.disableCompass();
        this.locationManager.removeUpdates(this.locationListener);
    }
    
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
    
    @Override
    public void finish() {
        super.finish();
    }
    
    @Override
    public void onStop() {
        super.onStop();
        disableLocation();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        disableLocation();
    }
    
    /**
     * Called on application start or resume
     */
    @Override
    public void onResume() {
        super.onResume();
        
        this.myLocationOverlay.enableCompass();
        this.myLocationOverlay.enableMyLocation();
        // Register the listener with the Location Manager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_THRESHOLD_MS, UPDATE_THRESHOLD_METERS, this.locationListener);
    }

    public class MotivatorMapLocationListener implements LocationListener {
        Context context;
        Location location;
        MapView mapView;

        public MotivatorMapLocationListener(Context context, Location location, MapView mapView) {
            this.context = context;
            this.location = location;
            this.mapView = mapView;
        }

        @Override
        public void onLocationChanged(Location loc) {                        
            this.location.set(loc);
            int latitude = (int)(location.getLatitude() * MILLION);
            int longitude = (int)(location.getLongitude() * MILLION);
            mapView.getController().setCenter(new GeoPoint(latitude, longitude));
            
            // Save last seen GPS location for quick app start up
            editor.putInt(getString(R.string.last_latitude_e6), latitude);
            editor.putInt(getString(R.string.last_longitude_e6), longitude);
            editor.commit();
        }

        @Override
        public void onProviderDisabled(String provider) {    }

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        
    }
    
    
    public static Location geoPointToLocation(GeoPoint gp) {
        Location result = new Location(LOCATION_SERVICE);
        result.setLatitude(gp.getLatitudeE6() / MILLION);
        result.setLongitude(gp.getLongitudeE6() / MILLION);
        return result;
    }
}