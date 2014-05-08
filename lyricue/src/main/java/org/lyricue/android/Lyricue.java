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

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

public class Lyricue extends ActionBarActivity {

    /**
     * Called when the activity is first created.
     */
    public static final String PREFS_NAME = "LyricuePrefsFile";
    private static final String TAG = Lyricue.class.getSimpleName();
    public HostItem hosts[] = null;
    public String profile = "";
    public long playlistid = (long) -1;
    public String[] playlists_text = null;
    public long[] playlists_id = null;
    public String[] bibles_text = null;
    public String[] bibles_id = null;
    public String[] bibles_type = null;
    public boolean imageplaylist = true;
    public LyricueDisplay ld = null;
    public Map<String, Fragment> fragments = new HashMap<String, Fragment>();
    public int thumbnail_width = 0;
    private ViewPager pager = null;
    private boolean togglescreen = false;
    private ProgressDialog progressLoad = null;
    private Lyricue activity = null;
    public ActionBar actionBar = null;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        activity = this;
        setContentView(R.layout.main);
        FragmentManager fragman = getSupportFragmentManager();
        if (savedInstanceState != null) {
            for (Fragment frag : fragman.getFragments()) {
                if (frag != null) {
                    Log.d(TAG, frag.toString());
                    Log.d(TAG, frag.getClass().getName());
                    fragments.put(frag.getClass().getName(), frag);
                }
            }
            hosts = (HostItem[]) savedInstanceState.getParcelableArray("hosts");
            profile = savedInstanceState.getString("profile");
            playlistid = savedInstanceState.getLong("playlistid");
            playlists_text = savedInstanceState.getStringArray("playlists_text");
            playlists_id = savedInstanceState.getLongArray("playlists_id");
            bibles_text = savedInstanceState.getStringArray("bibles_text");
            bibles_id = savedInstanceState.getStringArray("bibles_id");
            bibles_type = savedInstanceState.getStringArray("bibles_type");
            imageplaylist = savedInstanceState.getBoolean("imageplaylist");
            ld=new LyricueDisplay(hosts);
        }

        LyricuePagerAdapter adapter = new LyricuePagerAdapter(fragman, activity.getBaseContext(),
                activity);
        pager = (ViewPager) findViewById(R.id.viewpager);
        pager.setAdapter(adapter);

        actionBar = getSupportActionBar();
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                // When the tab is selected, switch to the
                // corresponding page in the ViewPager.
                pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
            }

            @Override
            public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
            }
        };

        Resources res = getResources();
        Configuration conf = res.getConfiguration();
        boolean isLandscape = (conf.orientation == Configuration.ORIENTATION_LANDSCAPE);
        boolean isLarge = (conf.screenLayout & 0x4) == 0x4;

        if (isLarge && isLandscape) {
            activity.setQuickBar(false);
        }
        actionBar.addTab(actionBar.newTab().setText(R.string.control)
                .setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText(R.string.playlist)
                .setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText(R.string.available)
                .setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText(R.string.bible)
                .setTabListener(tabListener));
        actionBar.addTab(actionBar.newTab().setText(R.string.display)
                .setTabListener(tabListener));

        pager.setOffscreenPageLimit(actionBar.getTabCount());
        pager.setCurrentItem(0);
        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                getSupportActionBar().setSelectedNavigationItem(position);
            }
        });

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        thumbnail_width = Math.min(displaymetrics.widthPixels,
                displaymetrics.heightPixels) / 2;
        if (profile.equals("")) {
            getPrefs();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        for (String key : fragments.keySet()) {
            if (getSupportFragmentManager() != null) {
                if (fragments.get(key) != null) {
                    getSupportFragmentManager().putFragment(outState, key, fragments.get(key));
                }
            }
        }
        outState.putParcelableArray("hosts", hosts);
        outState.putString("profile", profile);
        outState.putLong("playlistid", playlistid);
        outState.putStringArray("playlists_text", playlists_text);
        outState.putLongArray("playlists_id", playlists_id);
        outState.putStringArray("bibles_text", bibles_text);
        outState.putStringArray("bibles_id", bibles_id);
        outState.putStringArray("bibles_type", bibles_type);
        outState.putBoolean("imageplaylist", imageplaylist);
        super.onSaveInstanceState(outState);
    }

    void getPrefs() {
        Log.i(TAG, "getPrefs");
        if (progressLoad != null)
            progressLoad.dismiss();
        progressLoad = ProgressDialog.show(this, "", "Loading Preferences..",
                true);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        new GetPrefsTask().execute(this);
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop()");
        super.onStop();
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "onStart()");
        super.onStart();
    }

    @Override
    protected void onRestart() {
        Log.i(TAG, "onRestart()");
        MyNotification notify = new MyNotification(activity, hosts);
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
    }

    public void onClickControl(View v) {
        switch (v.getId()) {
            case R.id.ButtonPrevPage:
            case R.id.ButtonQuickPP:
                ld.runCommand_noreturn("display", "prev_page", "");
                break;
            case R.id.ButtonNextPage:
            case R.id.ButtonQuickNP:
                ld.runCommand_noreturn("display", "next_page", "");
                break;
            case R.id.ButtonPrevSong:
            case R.id.ButtonQuickPS:
                ld.runCommand_noreturn("display", "prev_song", "");
                break;
            case R.id.ButtonNextSong:
            case R.id.ButtonQuickNS:
                ld.runCommand_noreturn("display", "next_song", "");
                break;
            case R.id.ButtonBlank:
            case R.id.ButtonQuickBL:
                ld.runCommand_noreturn("blank", "", "");
                break;
            case R.id.ButtonRedisplay:
            case R.id.ButtonQuickRS:
                ld.runCommand_noreturn("display", "current", "");
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_menu:
                Intent settingsActivity = new Intent(getBaseContext(),
                        Preferences.class);
                startActivity(settingsActivity);
                return true;
            case R.id.profile_menu:
                SharedPreferences settings = PreferenceManager
                        .getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("profile", "");
                editor.commit();
                getPrefs();
                return true;
            case R.id.server_menu:
                Intent serverActivity = new Intent(getBaseContext(),
                        ServerActivity.class);
                if (profile.equals("#demo")) {
                    serverActivity.putExtra("host", "#demo");
                } else {
                    Log.i(TAG, "selecting " + hosts[0].toString());
                    serverActivity.putExtra("host", hosts[0].toString());
                }
                serverActivity.putExtra("profile", profile);
                startActivity(serverActivity);
                return true;
            case R.id.exit_menu:
                NotificationManager notificationManager = (NotificationManager) this
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancelAll();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        PlaylistFragment frag = (PlaylistFragment) fragments.get(PlaylistFragment.class.getName());
        Button button = (Button) frag.getView().findViewById(
                R.id.buttonPlayUp);
        if (button.getVisibility() == View.VISIBLE) {
            Log.i(TAG, "Go up to " + frag.parent_playlist);
            frag.load_playlist(frag.parent_playlist);
            return;
        }

        // Otherwise defer to system default behavior.
        super.onBackPressed();
    }

    void showPlaylistsDialog() {
        PlaylistsDialogFragment newFragment = PlaylistsDialogFragment
                .newInstance(1);
        newFragment.show(getSupportFragmentManager(), "dialog");
    }

    public void setQuickBar(boolean visible) {
        View v = getWindow().getDecorView();
        View quickBar = v.findViewById(R.id.quickBar);
        View hline1 = v.findViewById(R.id.hline1);
        if (quickBar != null && hline1 != null) {
            if (visible) {
                quickBar.setVisibility(View.VISIBLE);
                hline1.setVisibility(View.VISIBLE);
            } else {
                quickBar.setVisibility(View.INVISIBLE);
                hline1.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void logError(String error_text) {
        Log.e(TAG, error_text);
        Toast.makeText(this, error_text, Toast.LENGTH_SHORT).show();
    }

    public void load_playlist() {
        PlaylistFragment frag = (PlaylistFragment) fragments.get(PlaylistFragment.class.getName());
        if (frag != null) {
            if (frag.progressPlaylist != null)
                frag.progressPlaylist.dismiss();
            frag.progressPlaylist = ProgressDialog.show(this, "",
                    "Loading Playlist..", true);
            frag.load_playlist(playlistid);
        } else {
            logError("playlist fragment not found");
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Resources res = getBaseContext().getResources();
        Configuration conf = res.getConfiguration();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        boolean isLarge = (conf.screenLayout & 0x4) == 0x4;
        boolean isLandscape = (conf.orientation == Configuration.ORIENTATION_LANDSCAPE);
        if (isLarge && isLandscape) {
            Log.d(TAG,"rotate: landscape");
            fragmentTransaction.hide(getSupportFragmentManager().findFragmentById(R.layout.control));
        } else {
            Log.d(TAG,"rotate: portrait");
            fragmentTransaction.show(getSupportFragmentManager().findFragmentById(R.layout.control));
        }
        fragmentTransaction.commit();
    }

    private class GetPrefsTask extends AsyncTask<Context, Void, Integer> {
        final Integer SELECT_PROFILE = 1;
        final Integer DEMO_MODE = 2;
        final Integer SUCCESS = 0;
        String found_host = "";
        int found_port = 0;

        @Override
        protected Integer doInBackground(Context... arg0) {
            SharedPreferences settings = PreferenceManager
                    .getDefaultSharedPreferences(arg0[0]);
            togglescreen = settings.getBoolean("togglescreen", true);
            imageplaylist = settings.getBoolean("imageplaylist", true);
            profile = settings.getString("profile", "not set");
            Log.i(TAG, "profile:" + profile);
            if (profile.equals("#demo")) {
                hosts = null;
                return DEMO_MODE;
            }

            if (settings.getString("hostname", "").isEmpty()) {
                // Find a display server to talk to (doesn't matter which - we
                // are just using it to ask the DB for a full list)
                Log.i(TAG, "start multicast dns()");
                try {
                    WifiManager wifi = (WifiManager) Lyricue.this
                            .getSystemService(Context.WIFI_SERVICE);
                    MulticastLock lock = wifi.createMulticastLock("mylock");
                    lock.acquire();
                    WifiInfo wifiinfo = wifi.getConnectionInfo();
                    int intaddr = wifiinfo.getIpAddress();
                    byte[] byteaddr = new byte[]{(byte) (intaddr & 0xff),
                            (byte) (intaddr >> 8 & 0xff),
                            (byte) (intaddr >> 16 & 0xff),
                            (byte) (intaddr >> 24 & 0xff)};

                    JmDNS mJmDNS = JmDNS.create(InetAddress.getByAddress(byteaddr));

                    mJmDNS.addServiceListener("_lyricue._tcp.local.",
                            new ServiceListener() {

                                @Override
                                public void serviceAdded(ServiceEvent arg0) {
                                }

                                @Override
                                public void serviceRemoved(ServiceEvent arg0) {
                                }

                                @Override
                                public void serviceResolved(ServiceEvent arg0) {
                                    if (arg0.getName().contains("Display")) {
                                        Log.i(TAG, "host:"
                                                + arg0.getInfo()
                                                .getHostAddresses()[0]
                                                + ":"
                                                + arg0.getInfo().getPort());
                                        found_host = arg0.getInfo()
                                                .getHostAddresses()[0];
                                        found_port = arg0.getInfo().getPort();
                                    }
                                }
                            }
                    );
                    lock.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                found_host = settings.getString("hostname", "10.0.2.2");
                found_port = 2346;
            }
            for (long stop = System.nanoTime() + TimeUnit.SECONDS.toNanos(5); stop > System
                    .nanoTime(); ) {
                if (found_port != 0)
                    break;
                //noinspection EmptyCatchBlock
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
            Log.i(TAG, "Found values:" + found_host + ":" + found_port);
            if (found_port == 0) {
                profile = "#demo";
                hosts = null;
                return DEMO_MODE;
            } else {
                HostItem[] myhosts = new HostItem[1];
                myhosts[0] = new HostItem(found_host, found_port);

                if (profile.equals("not set") || profile.equals("")) {
                    return SELECT_PROFILE;
                }
                LyricueDisplay ld = new LyricueDisplay(myhosts);
                JSONArray jArray = ld
                        .runQuery(
                                "lyricDb",
                                "SELECT host, type FROM status WHERE TIMEDIFF(NOW(), lastupdate) < '00:00:02' AND profile='"
                                        + profile + "'"
                        );
                if (jArray != null) {
                    try {
                        hosts = new HostItem[jArray.length()];
                        for (int i = 0; i < jArray.length(); i++) {
                            JSONObject results = jArray.getJSONObject(i);
                            if (results.getString("type").equals("normal")
                                    || results.getString("type").equals(
                                    "simple")
                                    || results.getString("type").equals(
                                    "headless")) {
                                hosts[i] = new HostItem(results.getString("host"));
                                if (android.os.Build.MODEL.equals("google_sdk")
                                        || android.os.Build.MODEL.equals("sdk")) {
                                    hosts[i].hostname = "10.0.2.2";
                                }
                                Log.i(TAG, "Adding host:" + hosts[i].toString());

                            }
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing data " + e.toString());
                    }
                    return SUCCESS;
                } else {
                    return SELECT_PROFILE;
                }
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (progressLoad != null)
                progressLoad.dismiss();
            Log.i(TAG, "return:" + result);
            if ((result.equals(SUCCESS)) || (result.equals(DEMO_MODE))) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    //noinspection UnusedAssignment
                    MyNotification notify = new MyNotification(activity, hosts);
                }
                ld = new LyricueDisplay(hosts);

                View v = getWindow().getDecorView();
                if (togglescreen) {
                    getWindow().addFlags(
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else {
                    getWindow().clearFlags(
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
                if (result.equals(DEMO_MODE)) {
                    v.findViewById(R.id.textDemo).setVisibility(View.VISIBLE);
                } else {
                    v.findViewById(R.id.textDemo).setVisibility(View.GONE);
                }
                PlaylistFragment frag1 = (PlaylistFragment) fragments
                        .get(PlaylistFragment.class.getName());
                playlistid = (long) -1;
                if (frag1 != null) {
                    frag1.refresh();
                }
                AvailableSongsFragment frag2 = (AvailableSongsFragment) fragments
                        .get(AvailableSongsFragment.class.getName());
                if (frag2 != null) {
                    frag2.load_available();

                }
                BibleFragment frag3 = (BibleFragment) fragments.get(BibleFragment.class.getName());
                if (frag3 != null) {
                    frag3.load_bible();
                }

            } else if (result.equals(SELECT_PROFILE)) {
                Intent profileActivity = new Intent(getBaseContext(),
                        ChooseProfile.class);
                profileActivity.putExtra("host", found_host + ":" + found_port);
                startActivityForResult(profileActivity, 1);
                finish();
            }
        }
    }


}