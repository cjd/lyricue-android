package org.lyricue.android;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ControlFragment extends Fragment {
	private static Lyricue activity = null;
	View v = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = (View) inflater.inflate(R.layout.control, null);
		activity = (Lyricue) this.getActivity();
		activity.setQuickBar(false);
		return v;
	}


	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (activity != null && isVisibleToUser == true) { 
			activity.setQuickBar(false);
			
		} else if (activity != null && isVisibleToUser == false) {
			activity.setQuickBar(true);
		}
	}

}
