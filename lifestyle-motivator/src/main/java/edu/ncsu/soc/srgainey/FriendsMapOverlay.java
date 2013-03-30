package edu.ncsu.soc.srgainey;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class FriendsMapOverlay extends ItemizedOverlay<OverlayItem> {

    static final String LOG_TAG = "FriendsMapOverlay";

	private ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();

	private Context context;

	// TODO: change marker color for each contact; see http://stackoverflow.com/questions/9237831/setcolorfilter-on-itemizedoverlay-marker-drawable

	public FriendsMapOverlay(Context context, Drawable marker) {
		super(boundCenterBottom(marker));
		super.populate();  // populate ItemizedOverlay, or NullPointerException will be caused if the the map is scrolled prior to adding an OverlayItem
		this.context = context;
	}

	public void addMarker(String markerName, GeoPoint geoPoint) {
		for(OverlayItem item : items) {
			if(item.getTitle().equals(markerName)) {
				items.remove(item);
				break;
			}
		}
		
		OverlayItem item = new OverlayItem(geoPoint, markerName, markerName);
		items.add(item);
		super.populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return (items.get(i));
	}

	@Override
	protected boolean onTap(int i) {
		Toast.makeText(context, items.get(i).getSnippet(), Toast.LENGTH_SHORT).show();
		return true;
	}

	@Override
	public int size() {
		return items.size();
	}
}