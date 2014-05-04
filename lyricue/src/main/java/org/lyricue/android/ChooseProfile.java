package org.lyricue.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ChooseProfile extends Activity {
    private static final String TAG = Lyricue.class.getSimpleName();
    private String host = "";
    private Spinner spinProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Choosing Profile");

        setContentView(R.layout.choose_profile);
        spinProfile = (Spinner) findViewById(R.id.spinProfileSelect);
        host = getIntent().getExtras().getString("host");
        new GetProfilesTask().execute(this);
    }

    public void onClickProfile(View v) {
        Log.i(TAG, "onClickSetup");
        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();

        switch (v.getId()) {
            case R.id.buttonProfileDemo:
                editor.putString("profile", "#demo");
                editor.commit();
                Intent lyricueActivity = new Intent(getBaseContext(), Lyricue.class);
                startActivity(lyricueActivity);
                finish();
                break;
            case R.id.buttonProfileApply:
                editor.putString("profile",
                        ((Spinner) findViewById(R.id.spinProfileSelect))
                                .getSelectedItem().toString()
                );
                editor.commit();
                lyricueActivity = new Intent(getBaseContext(), Lyricue.class);
                startActivity(lyricueActivity);
                finish();
                break;
        }
    }

    private class GetProfilesTask extends
            AsyncTask<Context, Void, ArrayAdapter<String>> {

        @Override
        protected ArrayAdapter<String> doInBackground(Context... arg0) {
            LyricueDisplay ld = new LyricueDisplay(new HostItem(host));
            JSONArray jArray = ld.runQuery("lyricDb",
                    "SELECT DISTINCT(profile) FROM status WHERE TIMEDIFF(NOW(), lastupdate) < '00:00:20'");
            ArrayList<String> spinArray = new ArrayList<String>();

            if (jArray != null) {
                try {
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject results = jArray.getJSONObject(i);
                        spinArray.add(results.getString("profile"));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing data " + e.toString());
                }
            }
            if (spinArray.size() == 1) {
                Log.i(TAG, "Only one profile found");
            }
            spinArray.add("#demo");

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(arg0[0],
                    android.R.layout.simple_spinner_item, spinArray);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            return adapter;
        }

        @Override
        protected void onPostExecute(ArrayAdapter<String> result) {
            spinProfile.setAdapter(result);
        }
    }
}
