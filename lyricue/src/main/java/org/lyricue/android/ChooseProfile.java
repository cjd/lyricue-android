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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ChooseProfile extends Activity {
    private final String TAG = "Lyricue";
    private String host = "";
    private ListView listProfile = null;
    String profile = "#demo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "Choosing Profile");

        setContentView(R.layout.choose_profile);
        listProfile = (ListView) findViewById(R.id.listProfileSelect);
        listProfile.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listProfile.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                profile = adapterView.getItemAtPosition(i).toString();
            }
        });
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
                editor.putString("profile", profile);
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
            ArrayList<String> listArray = new ArrayList<String>();

            if (jArray != null) {
                try {
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject results = jArray.getJSONObject(i);
                        listArray.add(results.getString("profile"));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing data " + e.toString());
                }
            }
            if (listArray.size() == 1) {
                Log.i(TAG, "Only one profile found");
            }
            listArray.add("#demo");

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(arg0[0],
                    android.R.layout.simple_list_item_single_choice, listArray);
            //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            return adapter;
        }

        @Override
        protected void onPostExecute(ArrayAdapter<String> result) {
            listProfile.setAdapter(result);
        }
    }
}
