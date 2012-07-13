package org.lyricue.android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class InitialSetup extends Activity {
	private static final String TAG = Lyricue.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.initial_setup);
	}
	
	public void onClickSetup(View v) {
		Log.d(TAG,"onClickSetup");
		switch (v.getId()) {
		case R.id.buttonInitialApply:
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = settings.edit();
			editor.putString("hostip", ((EditText)findViewById(R.id.editHostname)).getText().toString());
			editor.commit();
			Intent lyricueActivity = new Intent(getBaseContext(), Lyricue.class);
			startActivity(lyricueActivity);
			finish();
			break;
		}
	}
}
