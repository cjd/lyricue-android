package org.lyricue.android;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		SharedPreferences sharedPref = getPreferenceScreen().getSharedPreferences();
		sharedPref.registerOnSharedPreferenceChangeListener(this);
		EditTextPreference textPref = (EditTextPreference) findPreference("hostname");
		textPref.setSummary(textPref.getText());
		textPref = (EditTextPreference) findPreference("profile");
		textPref.setSummary(textPref.getText());
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	    @SuppressWarnings("deprecation")
		Preference pref = findPreference(key);

	    if (pref instanceof EditTextPreference) {
	        EditTextPreference textPref = (EditTextPreference) pref;
	        pref.setSummary(textPref.getText());
	    }
	}

}

