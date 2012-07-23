package org.lyricue.android;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;

public class LyricuePagerAdapter extends FragmentPagerAdapter {
	public String[] titles = new String[5];

	public int CONTROL_ID = 0;
	public int PLAYLIST_ID = 1;
	public int AVAIL_ID = 2;
	public int BIBLE_ID = 3;
	public int DISPLAY_ID = 4;
	public int pages = 5;
	Lyricue activity = null;

	public static final String PREFS_NAME = "LyricuePrefsFile";

	public LyricuePagerAdapter(FragmentManager fm, Context context,
			Lyricue activity) {
		super(fm);
		Resources res = context.getResources();
		Configuration conf = res.getConfiguration();
		this.activity = activity;

		boolean isLarge = (conf.screenLayout & 0x4) == 0x4;

		boolean isLandscape = (conf.orientation == Configuration.ORIENTATION_LANDSCAPE);
		if (isLarge && isLandscape) {

			AVAIL_ID = 1;
			BIBLE_ID = 2;
			DISPLAY_ID = 3;
			PLAYLIST_ID = 4;
			pages = 4;
		}
		titles[CONTROL_ID] = res.getString(R.string.control);
		titles[PLAYLIST_ID] = res.getString(R.string.playlist);
		titles[AVAIL_ID] = res.getString(R.string.available);
		titles[BIBLE_ID] = res.getString(R.string.bible);
		titles[DISPLAY_ID] = res.getString(R.string.display);
	}

	@Override
	public String getPageTitle(int position) {
		return titles[position];
	}

	@Override
	public int getCount() {
		return pages;
	}

	@Override
	public Fragment getItem(int position) {
		Fragment f = null;

		if (position == CONTROL_ID) {
			f = new ControlFragment();
			activity.fragments.put("control", f);
		} else if (position == PLAYLIST_ID) {
			f = new PlaylistFragment();
			activity.fragments.put("playlist", f);
		} else if (position == AVAIL_ID) {
			f = new AvailableSongsFragment();
			activity.fragments.put("avail", f);
		} else if (position == BIBLE_ID) {
			f = new BibleFragment();
			activity.fragments.put("bible", f);
		} else if (position == DISPLAY_ID) {
			f = new DisplayFragment();
			activity.fragments.put("display", f);
		} else {
			f = new ControlFragment();
			activity.fragments.put("control", f);
		}

		return f;
	}

	@Override
	public void destroyItem(View container, int position, Object object) {
		String key = null;
		if (position == CONTROL_ID) {
			;
			key = "control";
		} else if (position == PLAYLIST_ID) {
			key = "playlist";
		} else if (position == AVAIL_ID) {
			key = "avail";
		} else if (position == BIBLE_ID) {
			key = "bible";
		} else if (position == DISPLAY_ID) {
			key = "display";
		}
		if (key != null)
			activity.fragments.remove(key);
	}

}