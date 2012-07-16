package org.lyricue.android;

import java.io.DataOutputStream;
import java.net.Socket;

import com.viewpagerindicator.TabPageIndicator;

import android.app.ProgressDialog;
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
		adapter = new LyricuePagerAdapter(fragman,this.getBaseContext());
		pager = (ViewPager) findViewById(R.id.viewpager);
		TabPageIndicator indicator = (TabPageIndicator) findViewById(R.id.indicator);
		pager.setAdapter(adapter);
		indicator.setViewPager(pager);
		pager.setOffscreenPageLimit(5);
		getPrefs();
		pager.setCurrentItem(0);
	}

	private void getPrefs() {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		hostip = settings.getString("hostip", "not set");
		logDebug("hostip:" + hostip);
		if (hostip.equals("not set") || hostip.equals("")) {
			Intent setupActivity = new Intent(getBaseContext(),
					InitialSetup.class);
			startActivityForResult(setupActivity, 1);
			finish();
			return;
		}
		if (settings.getBoolean("togglescreen", true)) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		ProgressDialog progressSongs = ProgressDialog.show(this,"","Connecting to Lyricue",true);
		ld = new LyricueDisplay(hostip);
		if (!ld.checkRunning()) {
			progressSongs.dismiss();
			Intent accessActivity = new Intent(getBaseContext(),
					UnableToAccess.class);
			startActivityForResult(accessActivity, 1);
			finish();
		}
		progressSongs.dismiss();
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
		PlaylistFragment frag = (PlaylistFragment) getSupportFragmentManager().findFragmentById(R.id.playlist);
		if (frag != null) {
			frag.load_playlist();
		} else {
			frag = (PlaylistFragment) adapter.getFragment(1);
			if (frag != null) {
				frag.load_playlist();
			} else {
				logError("playlist fragment not found");
			}
		}
	}
	
}