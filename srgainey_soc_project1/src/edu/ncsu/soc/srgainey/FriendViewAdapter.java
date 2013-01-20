package edu.ncsu.soc.srgainey;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class FriendViewAdapter extends BaseAdapter {

    static final String LOG_TAG = "FriendViewAdapter";

	protected FriendMapper friendMapper;

	protected List<String> names = new ArrayList<String>();

	public FriendViewAdapter(FriendMapper friendMapper) {
		this.friendMapper = friendMapper;
		names = getFriendNames();
	}
	
	protected List<String> getFriendNames() {
		List<String> friendNames = new ArrayList<String>();
		Uri ContactsUri = ContactsContract.Contacts.CONTENT_URI;  // URI for the provide as authority + path
		Cursor contacts = this.friendMapper.getContentResolver().query(ContactsUri, null, null, null, null);  // project, selectionClause and selectionArgs left null
		while (contacts.moveToNext()) {   		// iterate over contracts and add contact display name to names list
			String contactName = contacts.getString(contacts.getColumnIndex(PhoneLookup.DISPLAY_NAME));
			friendNames.add(contactName);
		}
		return friendNames;
	}

	@Override
	public int getCount() {
		return names.size();
	}

	@Override
	public Object getItem(int position) {
		return names.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView textView = new TextView(friendMapper);
		textView.setText(names.get(position));
		textView.setClickable(true);
		textView.setOnClickListener(new FriendListener());
		return textView;
	}

	public class FriendListener implements OnClickListener {
		public void onClick(View view) {
			TextView textView = (TextView) view;
			// Background color are coded as Alpha+RGB in hexadecimal
			textView.setBackgroundColor(Color.BLUE);
			friendMapper.addMarkerAtCurrentLocation(textView.getText().toString());
		}
	}
}