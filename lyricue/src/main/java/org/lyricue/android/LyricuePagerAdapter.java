/*
 * This file is part of Lyricue.
 *
 *     Lyricue is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Foobar is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lyricue.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lyricue.android;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.ViewGroup;

public class LyricuePagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = LyricuePagerAdapter.class.getSimpleName();
    private Lyricue activity = null;
    private FragmentManager fm = null;
    private Configuration conf = null;
    private Context context = null;

    public LyricuePagerAdapter(FragmentManager fm, Context context,
                               Lyricue activity) {
        super(fm);
        this.activity = activity;
        this.fm = fm;
        this.context = context;
        Resources res = context.getResources();
        conf = res.getConfiguration();
    }


    @Override
    public int getCount() {
        boolean isLarge = (conf.screenLayout & 0x4) == 0x4;
        boolean isLandscape = (conf.orientation == Configuration.ORIENTATION_LANDSCAPE);
        if (isLarge && isLandscape) {
            return 4;
        }
        return 5;
    }

    @Override
    public Fragment getItem(int position) {
        String title = context.getString(R.string.control);
        if (position < activity.actionBar.getTabCount()) {
            title = activity.actionBar.getTabAt(position).getText().toString();
        }
        Log.i(TAG, "get fragment:" + position + ":"+ title);
        Fragment f=null;
        if (title.equals(context.getString(R.string.control))) {
            f = activity.fragments.get(ControlFragment.class.getName());
            if (f == null) {
                f = new ControlFragment();
                activity.fragments.put(f.getClass().getName(), f);
            }
        } else if (title.equals(context.getString(R.string.playlist))) {
            f = activity.fragments.get(PlaylistFragment.class.getName());
            if (f == null) {
                f = new PlaylistFragment();
                activity.fragments.put(f.getClass().getName(), f);
            }
        } else if (title.equals(context.getString(R.string.available))) {
            f = activity.fragments.get(AvailableSongsFragment.class.getName());
            if (f == null) {
                f = new AvailableSongsFragment();
                activity.fragments.put(f.getClass().getName(), f);
            }
        } else if (title.equals(context.getString(R.string.bible))) {
            f = activity.fragments.get(BibleFragment.class.getName());
            if (f == null) {
                f = new BibleFragment();
                activity.fragments.put(f.getClass().getName(), f);
            }
        } else if (title.equals(context.getString(R.string.display))) {
            f = activity.fragments.get(DisplayFragment.class.getName());
            if (f == null) {
                f = new DisplayFragment();
                activity.fragments.put(f.getClass().getName(), f);
            }
        }
        boolean isLarge = (conf.screenLayout & 0x4) == 0x4;
        boolean isLandscape = (conf.orientation == Configuration.ORIENTATION_LANDSCAPE);
        /*FragmentTransaction fragmentTransaction = fm.beginTransaction();
        if (isLarge && isLandscape) {
            fragmentTransaction.hide(fm.findFragmentById(R.layout.control));
        } else {
            fragmentTransaction.show(fm.findFragmentById(R.layout.control));
        }
        fragmentTransaction.commit();*/
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
        }
        if (key != null)
            activity.fragments.remove(key);*/
        super.destroyItem(container, position, object);
    }

}