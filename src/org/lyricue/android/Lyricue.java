package org.lyricue.android;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.viewpagerindicator.TitlePageIndicator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
	public String hostip = "";
	public int playlistid = -1;
	public String[] playlists_text = null;
	public int[] playlists_id = null;
	public boolean togglescreen = false;
	FragmentManager fragman = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		fragman = getSupportFragmentManager();
		adapter = new LyricuePagerAdapter(fragman);
		pager = (ViewPager) findViewById(R.id.viewpager);
		TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
		pager.setAdapter(adapter);
		indicator.setViewPager(pager);
		pager.setOffscreenPageLimit(4);
		getPrefs();
		pager.setCurrentItem(0);
	}

	private void getPrefs() {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		hostip = settings.getString("hostip", "");
		logDebug("hostip:" + settings.getString("hostip", "not set"));
		if (hostip.equals("")) {
			Intent settingsActivity = new Intent(getBaseContext(),
					Preferences.class);
			startActivityForResult(settingsActivity, 1);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			getPrefs();
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onStart() {
		getPrefs();
		super.onStart();
	}

	public void onClickControl(View v) {
		System.err.println("onclickcontrol");
		switch (v.getId()) {
		case R.id.ButtonPrevPage:
			runCommand_noreturn("display", "prev_page", "");
			break;
		case R.id.ButtonNextPage:
			runCommand_noreturn("display", "next_page", "");
			break;
		case R.id.ButtonPrevSong:
			runCommand_noreturn("display", "prev_song", "");
			break;
		case R.id.ButtonNextSong:
			runCommand_noreturn("display", "next_song", "");
			break;
		case R.id.ButtonBlank:
			runCommand_noreturn("blank", "", "");
			break;
		case R.id.ButtonRedisplay:
			runCommand_noreturn("display", "current", "");
			break;
		}
	}
	
	public void onClickAvailable(View v) {
		System.err.println("onclickavailable");
		switch (v.getId()) {
		case R.id.ButtonClearSongSearch:
			EditText searchString = (EditText) pager.findViewById(R.id.available_search);
			if (searchString != null) searchString.setText("");
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

	
	public void logError(String error_text) {
		Log.d(TAG, error_text);
		Toast.makeText(this, error_text, Toast.LENGTH_SHORT).show();
	}

	public void logDebug(String error_text) {
		Log.d(TAG, error_text);
	}

	public void runCommand_noreturn(final String command, final String option1,
			final String option2) {
		new Thread(new Runnable() {
			public void run() {
				LyricueDisplay ld = new LyricueDisplay(hostip);
				ld.runCommand(command, option1, option2);
			}
		}).start();
	}

	public String runCommand(String command, String option1, String option2) {
		String result = "";
		if (sc == null) {
			try {
				sc = new Socket(hostip, 2346);
			} catch (UnknownHostException e) {
				logError("Don't know about host: " + hostip);
			} catch (IOException e) {
				logError("Couldn't get I/O socket for the connection to: "
						+ hostip);
			}
		}
		if (sc != null && os == null) {
			try {
				os = new DataOutputStream(sc.getOutputStream());
			} catch (UnknownHostException e) {
				logError("Don't know about host: " + hostip);
			} catch (IOException e) {
				logError("Couldn't get I/O output for the connection to: "
						+ hostip);
			}
		}
		if (sc != null && os != null) {
			try {
				os.writeBytes(command + ":" + option1 + ":" + option2 + "\n");
				os.flush();
				InputStream is = sc.getInputStream();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is, "utf-8"), 128);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				is.close();
				result = sb.toString();
				try {
					os.close();
					sc.close();
					os = null;
					sc = null;
				} catch (IOException f) {
				}
			} catch (UnknownHostException e) {
				logError("Trying to connect to unknown host: " + e);
			} catch (IOException e) {
				logError("IOException:  " + e);
			}
		} else {
			if (sc != null) {
				try {
					sc.close();
					sc = null;
				} catch (IOException e) {
					logError("IOException:  " + e);
				}
			}
			if (os != null) {
				try {
					os.close();
					os = null;
				} catch (IOException e) {
					logError("IOException:  " + e);
				}
			}
		}
		return result;
	}

	public JSONArray runQuery(String database, String query) {
		String result = runCommand("query", database, query);
		if (result == "") {
			return null;
		} else {
			try {
				JSONObject json = new JSONObject(result);
				JSONArray jArray = json.getJSONArray("results");
				return jArray;
			} catch (JSONException e) {
				logError("Error parsing data " + e.toString());
				return null;
			}
		}
	}
	
	public String runQuery_with_result(String database, String query, String retval) {
		JSONArray jArray = runQuery(database, query);
		if (jArray == null) {
			return null;
		}
		String retstring = "";
		try {
			retstring = jArray.getJSONObject(0).getString(retval);
		} catch (JSONException e) {
			logError("Error parsing data " + e.toString());
		}
		return retstring;
	}
}