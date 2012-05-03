package org.lyricue.android;

import java.io.DataOutputStream;
import java.net.Socket;

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
	public LyricueDisplay ld = null;
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
		ld = new LyricueDisplay(hostip);
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
			ld.runCommand_noreturn("display", "prev_page", "");
			break;
		case R.id.ButtonNextPage:
			ld.runCommand_noreturn("display", "next_page", "");
			break;
		case R.id.ButtonPrevSong:
			ld.runCommand_noreturn("display", "prev_song", "");
			break;
		case R.id.ButtonNextSong:
			ld.runCommand_noreturn("display", "next_song", "");
			break;
		case R.id.ButtonBlank:
			ld.runCommand_noreturn("blank", "", "");
			break;
		case R.id.ButtonRedisplay:
			ld.runCommand_noreturn("display", "current", "");
			break;
		}
	}
	
	public void onClickAvailable(View v) {
		System.err.println("onClickAvailable");
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
	
}