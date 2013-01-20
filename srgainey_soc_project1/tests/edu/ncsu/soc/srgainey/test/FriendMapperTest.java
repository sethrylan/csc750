package edu.ncsu.soc.srgainey.test;

import android.app.Instrumentation;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.widget.ListView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import edu.ncsu.soc.srgainey.FriendMapper;
import edu.ncsu.soc.srgainey.FriendsMapOverlay;

public class FriendMapperTest extends ActivityInstrumentationTestCase2<FriendMapper> {

	public FriendMapperTest(String name) {
		super("edu.ncsu.soc.srgainey", FriendMapper.class);
		setName(name);
	}

	static final String LOG_TAG = "FriendMapperTest";

	private FriendMapper friendMapperActivity;
	private MapView mapView;
	private ListView listView;
	private Instrumentation instrumentation;
	private LocationManager locationManager;

	private static final float MILLION = 1E6f;

	private static final GeoPoint TEST_GEOPOINT_1 = new GeoPoint(32807476, -79958239); // Charleston
	private static final GeoPoint TEST_GEOPOINT_2 = new GeoPoint(35772052, -78673718); // NCSU location

	protected void setUp() throws Exception {
		super.setUp();
		instrumentation = getInstrumentation();
		friendMapperActivity = getActivity(); // get a references to the app under test
		mapView = (MapView)friendMapperActivity.findViewById(edu.ncsu.soc.project1.R.id.MapView);
		listView = (ListView)friendMapperActivity.findViewById(edu.ncsu.soc.project1.R.id.FriendList);
		locationManager = (LocationManager)getInstrumentation().getContext().getSystemService(Context.LOCATION_SERVICE);
	}
	
	public void testPreConditions() {
	    assertNotNull(mapView);
	    assertNotNull(listView);
	    assertTrue(listView.getCount() > 0);
	    for(int i = 0; i < listView.getCount(); i++) {
	    	Object listViewItem = listView.getItemAtPosition(i);
	    	assertTrue(listViewItem instanceof String);
	    }
	    assertNotNull(mapView.getOverlays());
	    assertEquals("There should be one map overlay.", 1, mapView.getOverlays().size());
		assertEquals("There should be zero map overlay items.", 0, ((FriendsMapOverlay)mapView.getOverlays().get(0)).size());
	}

	@UiThreadTest
	public void testAddMarkerAtCurrentLocation() {
		//	UI tests can be run in their own thread, or can be annotated; i.e.,
		//		friendMapperActivity.runOnUiThread(new Runnable() {
		//			public void run() {	
		listView.requestFocusFromTouch();
		for (int i = 0; i < listView.getCount(); i++) {
			listView.setSelection(i);
			listView.performItemClick(listView.getAdapter().getView(i, null, null), i, i);
		}
	}

	@UiThreadTest
	public void testViewFocuses() {
		mapView.requestFocus();
		assertFalse(mapView.hasFocus());
		assertTrue(mapView.hasWindowFocus());
		assertTrue(listView.hasFocus());
		assertTrue(listView.hasWindowFocus());
		
		listView.requestFocus();
		assertFalse(mapView.hasFocus());
		assertTrue(mapView.hasWindowFocus());
		assertTrue(listView.hasFocus());
		assertTrue(listView.hasWindowFocus());

		mapView.performClick();
		assertFalse(mapView.hasFocus());
		assertTrue(mapView.hasWindowFocus());
		assertTrue(listView.hasFocus());
		assertTrue(listView.hasWindowFocus());
		
		listView.performClick();
		assertFalse(mapView.hasFocus());
		assertTrue(mapView.hasWindowFocus());
		assertTrue(listView.hasFocus());
		assertTrue(listView.hasWindowFocus());
	}
	
	
    public void testGps() {
		friendMapperActivity.runOnUiThread(new Runnable() {
			public void run() {
				listView.requestFocusFromTouch();
		    	Location testLocation1 = new Location(LocationManager.GPS_PROVIDER) {{
		    		setLatitude(TEST_GEOPOINT_1.getLatitudeE6() / MILLION);
		    		setLongitude(TEST_GEOPOINT_1.getLongitudeE6() / MILLION);
		    	}};
				setLocation(testLocation1);
			}
		});

		instrumentation.waitForIdleSync();
		mapView.postInvalidate();

        assertTrue(withinPrecision(mapView.getMapCenter().getLatitudeE6(), TEST_GEOPOINT_1.getLatitudeE6(), 1000));
        assertTrue(withinPrecision(mapView.getMapCenter().getLongitudeE6(), TEST_GEOPOINT_1.getLongitudeE6(), 1000));
		
		friendMapperActivity.runOnUiThread(new Runnable() {
			public void run() {
				listView.requestFocusFromTouch();					
		    	Location testLocation2 = new Location(LocationManager.GPS_PROVIDER) {{
		    		setLatitude(TEST_GEOPOINT_2.getLatitudeE6() / MILLION);
		    		setLongitude(TEST_GEOPOINT_2.getLongitudeE6() / MILLION);
		    	}};
		    	setLocation(testLocation2);
			}
		});

		instrumentation.waitForIdleSync();
		mapView.postInvalidate();                
    }
    
    
    private void setLocation(Location newLocation) {
    	this.locationManager.addTestProvider(LocationManager.GPS_PROVIDER, false, false, false, false, false, false, false, android.location.Criteria.POWER_LOW, android.location.Criteria.ACCURACY_FINE);      
        this.locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
        this.locationManager.setTestProviderStatus(LocationManager.GPS_PROVIDER, LocationProvider.AVAILABLE, null, System.currentTimeMillis());         

        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.set(newLocation);
        location.setAccuracy(1);
        this.locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location);      

    }

    private boolean withinPrecision(int number, int target, int precision) {
    	return (number <= (target+precision)) && (number >= (target-precision));
    }

}


