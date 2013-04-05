package edu.ncsu.soc.motivator;

import java.util.List;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

import edu.ncsu.soc.motivator.domain.Nearby;

public class PlacesViewAdapter extends BaseAdapter {

    static final String LOG_TAG = "PlacesViewAdapter";
    protected MotivatorMapActivity motivatorMap;
    protected List<Nearby.PlaceResult> places;
    private static final float MILLION = 1E6f;
    
    public PlacesViewAdapter(MotivatorMapActivity motivatorMap, List<Nearby.PlaceResult> places) {
        this.motivatorMap = motivatorMap;
        this.places = places;
    }
    
    public void setPlaces(List<Nearby.PlaceResult> places) {
        this.places = places;
    }

    @Override
    public int getCount() {
        return places.size();
    }

    @Override
    public Object getItem(int position) {
        return places.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView = new TextView(motivatorMap);
        textView.setText(places.get(position).name);
        textView.setClickable(true);
        textView.setOnClickListener(new PlaceListener());
        return textView;
    }

    public class PlaceListener implements OnClickListener {
        public void onClick(View view) {
            TextView textView = (TextView)view;
            // Background color are coded as Alpha+RGB in hexadecimal
//            textView.setBackgroundColor(Color.BLUE);
            String name = textView.getText().toString();
            for(Nearby.PlaceResult place : places) {
                if(place.name.equalsIgnoreCase(name)) {
                    motivatorMap.addMarker(textView.getText().toString(), (int)(place.geometry.location.lat * MILLION),  (int)(place.geometry.location.lng * MILLION));
                }
            }
        }
    }
}