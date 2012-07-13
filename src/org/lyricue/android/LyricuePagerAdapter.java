package org.lyricue.android;

import java.util.HashMap;
import java.util.Map;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;

public class LyricuePagerAdapter extends FragmentPagerAdapter {

	public static String[] titles = new String[] { "Control", "Playlist",
			"Available Songs", "Bible", "Display" };

	public static int CONTROL_ID = 0;
	public static int PLAYLIST_ID = 1;
	public static int AVAIL_ID = 2;
	public static int BIBLE_ID = 3;
	public static int DISPLAY_ID = 4;

	public static final String PREFS_NAME = "LyricuePrefsFile";

	private Map<Integer, Fragment> mPageReferenceMap = new HashMap<Integer, Fragment>();

	public LyricuePagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public String getPageTitle(int position) {
		return titles[position];
	}

	@Override
	public int getCount() {
		return titles.length;
	}

	@Override
	public Fragment getItem(int position) {
		Fragment f = null;

		if (position == CONTROL_ID) {
			f = new ControlFragment();
		} else if (position == PLAYLIST_ID) {
			f = new PlaylistFragment();
		} else if (position == AVAIL_ID) {
			f = new AvailableSongsFragment();
		} else if (position == BIBLE_ID) {
			f = new BibleFragment();
		} else if (position == DISPLAY_ID) {
			f = new DisplayFragment();
		} else {
			f = new ControlFragment();
		}
		mPageReferenceMap.put(position, f);
		return f;
	}

	@Override
	public void destroyItem(View container, int position, Object object) {
		mPageReferenceMap.remove(Integer.valueOf(position));
	}

	public Fragment getFragment(int position) {
		return mPageReferenceMap.get(position);
	}

}