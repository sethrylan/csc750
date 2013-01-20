package edu.ncsu.soc.srgainey.test;

import java.util.ArrayList;
import java.util.List;

import android.test.AndroidTestCase;
import edu.ncsu.soc.srgainey.FriendMapper;
import edu.ncsu.soc.srgainey.FriendViewAdapter;

public class FriendViewAdapterTest extends AndroidTestCase {

    static final String LOG_TAG = "FriendViewAdapterTest";

	private FriendViewAdapter adapter;
	
	@SuppressWarnings("serial")
	public static final List<String> testNames = new ArrayList<String>() {{
		add("Friend One");
		add("Friend Two");
		add("Friend Three");
		add("Friend Four");
		add("Friend Five");
		add("Friend Six");
		add("Friend Seven");
		add("Friend Eight");		
	}};
	
	public class MockFriendViewAdapter extends FriendViewAdapter {
		
		public MockFriendViewAdapter(FriendMapper friendMapper) {
			super(friendMapper);
		}
		
		@Override
		protected List<String> getFriendNames() {
			return testNames;
		}
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		adapter = new MockFriendViewAdapter(new FriendMapper());
	}
	
	public void testGetCount() {
		assertEquals(testNames.size(), adapter.getCount());
	}
	
	public void testGetItem() {
		for(int i = 0; i < adapter.getCount(); i++) {
			assertTrue(adapter.getItem(i) instanceof String);
			assertFalse(((String)adapter.getItem(i)).isEmpty());
		}
	}
	
	
}
