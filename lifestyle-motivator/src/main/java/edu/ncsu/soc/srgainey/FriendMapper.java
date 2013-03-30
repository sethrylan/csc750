package edu.ncsu.soc.srgainey;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

import edu.ncsu.soc.motivator.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ListView;

public class FriendMapper extends MapActivity {

    static final String LOG_TAG = "FriendMapper";

	private FriendsMapOverlay mapOverlay;
	private Location currentLocation;
	
	private static final float MILLION = 1E6f;
		
	private static final GeoPoint INITIAL_GEOPOINT = new GeoPoint(32807476, -79958239); // Charleston location
//	private static final GeoPoint INITIAL_GEOPOINT = new GeoPoint(35772052, -78673718); // NCSU location
	private static final Location INITIAL_LOCATION = new Location(LOCATION_SERVICE) {{
		setLatitude(INITIAL_GEOPOINT.getLatitudeE6() / MILLION);
		setLongitude(INITIAL_GEOPOINT.getLongitudeE6() / MILLION);
	}};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_map);

		// create the map overlay
		Drawable marker = getResources().getDrawable(R.drawable.marker);
		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
		mapOverlay = new FriendsMapOverlay(this, marker);

		// configure the friends list
		ListView list = (ListView) findViewById(R.id.FriendList);
		list.setAdapter(new FriendViewAdapter(this));

		// configure the map
		MapView mapView = (MapView) findViewById(R.id.MapView);
		mapView.getController().setCenter(INITIAL_GEOPOINT);
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
		this.currentLocation = new Location(INITIAL_LOCATION);

		// Define an listener in an inner class
		LocationListener locationListener = new FriendMapperLocationListener(this.getApplicationContext(), currentLocation, mapView); 
		
		// Register the listener with the Location Manager
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

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
	
	public class FriendMapperLocationListener implements LocationListener {
		Context context;
		Location location;
		MapView mapView;

		public FriendMapperLocationListener(Context context, Location location, MapView mapView) {
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
		}

		@Override
		public void onProviderDisabled(String provider) {	}

		@Override
		public void onProviderEnabled(String provider) {}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}

	}
}