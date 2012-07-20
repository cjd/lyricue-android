package org.lyricue.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class UnableToAccess extends Activity {
	private static final String TAG = Lyricue.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.unable_to_access);
	}

	@Override
	protected void onResume() {
		super.onResume();
		retry_connection();
	}

	public void onClickSetup(View v) {
		Log.d(TAG,"onClickSetup");
		switch (v.getId()) {
		case R.id.buttonRetry:
			retry_connection();
			break;
		case R.id.buttonDemo:
			setup_demo();
			break;
		case R.id.buttonSettings:
			Intent settingsActivity = new Intent(getBaseContext(),
					Preferences.class);
			startActivity(settingsActivity);
			break;
		case R.id.buttonQuit:
			finish();
			break;
		}
	}
	
	public void setup_demo() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		SharedPreferences.Editor editor = settings.edit();
        editor.putString("hostip", "#demo");
        editor.commit();
		retry_connection();
	}
	
	public void retry_connection() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		String hostip = settings.getString("hostip", "");
		LyricueDisplay ld = new LyricueDisplay(hostip);
		if (!ld.checkRunning()) {
			Context context = getApplicationContext();
			CharSequence text = "Retry Failed";
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		} else {
			Intent lyricueActivity = new Intent(getBaseContext(), Lyricue.class);
			startActivity(lyricueActivity);
			finish();
		}
	}
}
