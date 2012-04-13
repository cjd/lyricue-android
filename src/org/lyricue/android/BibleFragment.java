package org.lyricue.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BibleFragment extends Fragment {
	public static final String PREFS_NAME = "LyricuePrefsFile";

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View v = (View) inflater.inflate(R.layout.bible, null);
		return v;

	}
	

}