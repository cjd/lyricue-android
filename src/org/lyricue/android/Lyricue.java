package org.lyricue.android;

import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

import com.viewpagerindicator.TabPageIndicator;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

public class Lyricue extends FragmentActivity {

	/** Called when the activity is first created. */
	public static final String PREFS_NAME = "LyricuePrefsFile";
	private static final String TAG = Lyricue.class.getSimpleName();

	public ViewPager pager = null;
	public LyricuePagerAdapter adapter = null;
	Socket sc = null;
	DataOutputStream os = null;
	public String[][] hosts = null; 
	public int playlistid = -1;
	public String[] playlists_text = null;
	public int[] playlists_id = null;
	public String[] bibles_text = null;
	public String[] bibles_id = null;
	public String[] bibles_type = null;
	public boolean togglescreen = false;
	public boolean imageplaylist = true;
	public LyricueDisplay ld = null;
	public Map<String, Fragment> fragments = new HashMap<String, Fragment>();
	private ProgressDialog progressLoad = null;
	public int thumbnail_width = 0;
	public MyNotification notify = null;
	private static JmDNS mJmDNS = null;


	FragmentManager fragman = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		fragman = getSupportFragmentManager();
		adapter = new LyricuePagerAdapter(fragman, this.getBaseContext(), this);
		pager = (ViewPager) findViewById(R.id.viewpager);
		TabPageIndicator indicator = (TabPageIndicator) findViewById(R.id.indicator);
		pager.setAdapter(adapter);
		indicator.setViewPager(pager);
		pager.setOffscreenPageLimit(5);
		pager.setCurrentItem(0);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		thumbnail_width = Math.min(displaymetrics.widthPixels,
				displaymetrics.heightPixels) / 2;
		start_mdns();
	}
	
	private void start_mdns() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					WifiManager wifi = (WifiManager) Lyricue.this
							.getSystemService(Context.WIFI_SERVICE);
					WifiInfo wifiinfo = wifi.getConnectionInfo();
					int intaddr = wifiinfo.getIpAddress();
					byte[] byteaddr = new byte[] { (byte) (intaddr & 0xff),
							(byte) (intaddr >> 8 & 0xff),
							(byte) (intaddr >> 16 & 0xff),
							(byte) (intaddr >> 24 & 0xff) };

					mJmDNS = JmDNS.create(InetAddress.getByAddress(byteaddr));
					mJmDNS.addServiceListener(
							"_lyricue._tcp.local.", new ServiceListener() {

								@Override
								public void serviceAdded(ServiceEvent arg0) {
									System.out.println("TXT:"
											+ new String(arg0.getInfo()
													.getTextBytes()));
									Log.w(TAG, String.format(
											"serviceAdded(event=\n%s\n)",
											arg0.toString()));
								}

								@Override
								public void serviceRemoved(ServiceEvent arg0) {
									Log.w(TAG, String.format(
											"serviceRemoved(event=\n%s\n)",
											arg0.toString()));
								}

								@Override
								public void serviceResolved(ServiceEvent arg0) {
									if (arg0.getName().startsWith("Lyricue Display")) {
										byte[] txt = arg0.getInfo().getTextBytes();
										Log.i(TAG, "host:"+arg0.getInfo().getHostAddresses()[0]+":"+arg0.getInfo().getPort());
										if (txt.length > 0) {
											HashMap<String, String> txts = new HashMap<String, String>();
											for (int i = 0; i < txt.length; ++i) {
												// first byte is length of the
												// "key=value"
												int len = (txt[i] & 0xff);
												int begin_at = i + 1;
												i += len;
												String[] keypair = new String(txt, begin_at, len).split("=",2);
												txts.put(keypair[0], keypair[1]);
												System.out
													.println("TXT KeyValuePair:["+keypair[0]+"=="+keypair[1]);
												if (arg0.getInfo().getHostAddresses()[0] != hostip) {
													SharedPreferences settings = PreferenceManager
															.getDefaultSharedPreferences(Lyricue.this);
													SharedPreferences.Editor editor = settings.edit();
													editor.putString("hostip",arg0.getInfo().getHostAddresses()[0]);
													editor.commit();
													getPrefs();
												}
											}
										}
									}
								}
							});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public void getPrefs() {
		logDebug("getPrefs");
		if (progressLoad != null)
			progressLoad.dismiss();
		progressLoad = ProgressDialog.show(this, "", "Loading Preferences..",
				true);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		new GetPrefsTask().execute(this);
	}

	private class GetPrefsTask extends AsyncTask<Context, Void, Integer> {
		@Override
		protected Integer doInBackground(Context... arg0) {
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(arg0[0]);
			if (settings.getBoolean("togglescreen", true)) {
				togglescreen = true;
			} else {
				togglescreen = false;
			}

			if (settings.getBoolean("imageplaylist", true)) {
				imageplaylist = true;
			} else {
				imageplaylist = false;
			}

			hostip = settings.getString("hostip", "not set");
			logDebug("hostip:" + hostip);

			if (hostip.equals("not set") || hostip.equals("")) {
				return 1;
			}
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				notify = new MyNotification(arg0[0], hostip);
			}

			ld = new LyricueDisplay(hostip);

			if (!ld.checkRunning()) {
				return 2;
			}
			return 0;
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (progressLoad != null)
				progressLoad.dismiss();
			if (result == 0) {
				View v = (View) getWindow().getDecorView();
				if (togglescreen) {
					getWindow().addFlags(
							WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				} else {
					getWindow().clearFlags(
							WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				}
				if (hostip.equals("#demo")) {
					v.findViewById(R.id.textDemo).setVisibility(View.VISIBLE);
				} else {
					v.findViewById(R.id.textDemo).setVisibility(View.GONE);
				}
				PlaylistFragment frag1 = (PlaylistFragment) fragments
						.get("playlist");
				if (frag1 == null) {
					frag1 = (PlaylistFragment) getSupportFragmentManager()
							.findFragmentById(R.id.playlist);
					fragments.put("playlist", frag1);

				}
				playlistid = -1;
				frag1.load_playlist();
				AvailableSongsFragment frag2 = (AvailableSongsFragment) fragments
						.get("avail");
				if (frag2 != null) {
					frag2.load_available();

				}
				BibleFragment frag3 = (BibleFragment) fragments.get("bible");
				if (frag3 != null) {
					frag3.load_bible();
				}

			} else if (result == 1) {
				Intent setupActivity = new Intent(getBaseContext(),
						InitialSetup.class);
				startActivityForResult(setupActivity, 1);
				finish();
			} else if (result == 2) {
				Intent accessActivity = new Intent(getBaseContext(),
						UnableToAccess.class);
				startActivityForResult(accessActivity, 1);
				finish();
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onStart() {
		super.onStart();
		getPrefs();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public void onClickControl(View v) {
		System.err.println("onclickcontrol");
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

	public void onClickAvailable(View v) {
		System.err.println("onClickAvailable");
		switch (v.getId()) {
		case R.id.ButtonClearSongSearch:
			EditText searchString = (EditText) pager
					.findViewById(R.id.available_search);
			if (searchString != null)
				searchString.setText("");
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
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
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	void showPlaylistsDialog(View v) {
		PlaylistsDialogFragment newFragment = PlaylistsDialogFragment
				.newInstance(1);
		newFragment.show(getSupportFragmentManager(), "dialog");
	}

	public void setQuickBar(boolean visible) {
		View v = (View) getWindow().getDecorView();
		View quickBar = (View) v.findViewById(R.id.quickBar);
		View hline1 = (View) v.findViewById(R.id.hline1);
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
		Log.d(TAG, error_text);
		Toast.makeText(this, error_text, Toast.LENGTH_SHORT).show();
	}

	public void logDebug(String error_text) {
		Log.d(TAG, error_text);
	}

	public void load_playlist() {
		PlaylistFragment frag = (PlaylistFragment) fragments.get("playlist");
		if (frag == null) {
			frag = (PlaylistFragment) getSupportFragmentManager()
					.findFragmentById(R.id.playlist);
			fragments.put("playlist", frag);
		}
		if (frag != null) {
			if (frag.progressPlaylist != null)
				frag.progressPlaylist.dismiss();
			frag.progressPlaylist = ProgressDialog.show(this, "",
					"Loading Playlist..", true);
			frag.load_playlist();
		} else {
			logError("playlist fragment not found");
		}
	}
}