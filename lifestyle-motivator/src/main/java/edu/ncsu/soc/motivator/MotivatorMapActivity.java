package edu.ncsu.soc.motivator;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

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
import android.widget.ListView;

public class MotivatorMapActivity extends MapActivity {

	static SharedPreferences settings;
	static SharedPreferences.Editor editor;

	static final String LOG_TAG = "MotivatorMap";

	private ContactsMapOverlay mapOverlay;
	private Location currentLocation;
	
	private static final float MILLION = 1E6f;
	
	// Default locations only used for time application starts
//	private static final GeoPoint DEFAULT_GEOPOINT = new GeoPoint(32807476, -79958239); // Charleston location
	private static final GeoPoint DEFAULT_GEOPOINT = new GeoPoint(35772052, -78673718); // NCSU location
	private static final Location DEFAULT_LOCATION = geoPointToLocation(DEFAULT_GEOPOINT);

	/** Called when the activity is first created. */
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		Location initialLocation = null;
		GeoPoint initialGeoPoint = null;
		
		settings = this.getPreferences(MODE_WORLD_WRITEABLE);	
		editor = settings.edit();

		// Retrieve GPS coordinates in form e6 (GeoPoint) form
		Integer lastGpsLat = settings.getInt("motivator.last_gps_lat", Integer.MIN_VALUE);
		Integer lastGpsLong = settings.getInt("motivator.last_gps_long", Integer.MIN_VALUE);
		
		if (lastGpsLat.equals(Integer.MIN_VALUE) || lastGpsLong.equals(Integer.MIN_VALUE)) {
			initialGeoPoint = DEFAULT_GEOPOINT;
			initialLocation = DEFAULT_LOCATION;
		} else {
			initialGeoPoint = new GeoPoint(lastGpsLat, lastGpsLong);
			initialLocation = geoPointToLocation(initialGeoPoint);
		}
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.motivator_map);

		// create the map overlay
		Drawable marker = getResources().getDrawable(R.drawable.marker);
		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
		mapOverlay = new ContactsMapOverlay(this, marker);

		// configure the contacts list
		ListView list = (ListView) findViewById(R.id.ContactsList);
		list.setAdapter(new ContactsViewAdapter(this));

		// configure the map and set initial position until GPS is available
		MapView mapView = (MapView) findViewById(R.id.MapView);
		mapView.getController().setCenter(initialGeoPoint);
		mapView.getController().setZoom(15);
		mapView.getOverlays().add(mapOverlay);
		
		// Add location listener
		// get reference to Location Manager
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		// check if GPS is enabled; if not, the send user to GPS settings
		boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if (!enabled) {
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);
		} 

		// initialize current location
		this.currentLocation = new Location(initialLocation);

		// Define an listener in an inner class
		LocationListener locationListener = new MotivatorMapLocationListener(this.getApplicationContext(), currentLocation, mapView); 
		
		// Register the listener with the Location Manager
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

	}
	
	public void startPlayer(View v) {
		Intent i = new Intent(this, MotivatorService.class);
		startService(i);
	}

	public void stopPlayer(View v) {
		stopService(new Intent(this, MotivatorService.class));
	}

	public void addMarkerAtCurrentLocation(String markerName) {
		int latitude = (int)(this.currentLocation.getLatitude() * MILLION);
		int longitude = (int)(this.currentLocation.getLongitude() * MILLION);
		
		mapOverlay.addMarker(markerName, new GeoPoint(latitude, longitude));
		
		// redraw the map
		MapView mapView = (MapView) findViewById(R.id.MapView);
		mapView.invalidate();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	@Override
    public void finish() {
        super.finish();
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
			editor.putInt("motivator.last_gps_lat", latitude);
			editor.putInt("motivator.last_gps_long", longitude);
			editor.commit();
		}

		@Override
		public void onProviderDisabled(String provider) {	}

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