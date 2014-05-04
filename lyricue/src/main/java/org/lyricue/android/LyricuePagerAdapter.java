package org.lyricue.android;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

public class LyricuePagerAdapter extends FragmentPagerAdapter {
    public static final String PREFS_NAME = "LyricuePrefsFile";
    private static final String TAG = Lyricue.class.getSimpleName();
    private final String[] titles = new String[5];
    private int CONTROL_ID = 0;
    private int PLAYLIST_ID = 1;
    private int AVAIL_ID = 2;
    private int BIBLE_ID = 3;
    private int DISPLAY_ID = 4;
    private int pages = 5;
    private Lyricue activity = null;

    public LyricuePagerAdapter(FragmentManager fm, Context context,
                               Lyricue activity) {
        super(fm);
        Resources res = context.getResources();
        Configuration conf = res.getConfiguration();
        this.activity = activity;

        boolean isLarge = (conf.screenLayout & 0x4) == 0x4;

        boolean isLandscape = (conf.orientation == Configuration.ORIENTATION_LANDSCAPE);
        if (isLarge && isLandscape) {

            PLAYLIST_ID = 0;
            AVAIL_ID = 1;
            BIBLE_ID = 2;
            DISPLAY_ID = 3;
            CONTROL_ID = 4;
            pages = 4;
        }
        titles[CONTROL_ID] = res.getString(R.string.control);
        titles[PLAYLIST_ID] = res.getString(R.string.playlist);
        titles[AVAIL_ID] = res.getString(R.string.available);
        titles[BIBLE_ID] = res.getString(R.string.bible);
        titles[DISPLAY_ID] = res.getString(R.string.display);
    }


    @Override
    public int getCount() {
        return pages;
    }

    @Override
    public Fragment getItem(int position) {
        Log.i(TAG, "get fragment:" + position);
        Fragment f = null;
        String className = "";
        if (position == CONTROL_ID) {

            f = activity.fragments.get(ControlFragment.class.getName());
            if (f == null) {
                f = new ControlFragment();
                activity.fragments.put(f.getClass().getName(), f);
            }
        } else if (position == PLAYLIST_ID) {
            f = activity.fragments.get(PlaylistFragment.class.getName());
            if (f == null) {
                f = new PlaylistFragment();
                activity.fragments.put(f.getClass().getName(), f);
            }
        } else if (position == AVAIL_ID) {
            f = activity.fragments.get(AvailableSongsFragment.class.getName());
            if (f == null) {
                f = new AvailableSongsFragment();
                activity.fragments.put(f.getClass().getName(), f);
            }
        } else if (position == BIBLE_ID) {
            f = activity.fragments.get(BibleFragment.class.getName());
            if (f == null) {
                f = new BibleFragment();
                activity.fragments.put(f.getClass().getName(), f);
            }
        } else if (position == DISPLAY_ID) {
            f = activity.fragments.get(DisplayFragment.class.getName());
            if (f == null) {
                f = new DisplayFragment();
                activity.fragments.put(f.getClass().getName(), f);
            }
        } else {
            f = activity.fragments.get(ControlFragment.class.getName());
            if (f == null) {
                f = new ControlFragment();
                activity.fragments.put(f.getClass().getName(), f);
            }
        }

        return f;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        String key = object.getClass().getName();
        Log.d(TAG, "removing " + key);
        /*
        if (position == CONTROL_ID) {
            key = ControlFragment.class.getName();
        } else if (position == PLAYLIST_ID) {
            key = PlaylistFragment.class.getName();
        } else if (position == AVAIL_ID) {
            key = AvailableSongsFragment.class.getName();
        } else if (position == BIBLE_ID) {
            key = BibleFragment.class.getName();
        } else if (position == DISPLAY_ID) {
            key = DisplayFragment.class.getName();
        }*/
        if (key != null)
            activity.fragments.remove(key);
        super.destroyItem(container, position, object);
    }

}