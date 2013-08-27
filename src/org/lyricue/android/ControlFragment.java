package org.lyricue.android;

import com.actionbarsherlock.app.SherlockFragment;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ControlFragment extends SherlockFragment {
	private static Lyricue activity = null;
	private View v = null;
	private LayoutInflater inflater = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		v = (View) inflater.inflate(R.layout.control, null);
		activity = (Lyricue) this.getActivity();
		activity.setQuickBar(false);
		return v;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// Checks the orientation of the screen
		if ((newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
				|| (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)) {
			ViewGroup vg = (ViewGroup) v.getParent();
			int index = vg.indexOfChild(v);
			vg.removeView(v);
			v = inflater.inflate(R.layout.control, vg, false);
			vg.addView(v, index);
			vg.invalidate();
			return;
		} else {
			super.onConfigurationChanged(newConfig);
		}
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (activity != null) {
			if (isVisibleToUser == true) {
				activity.setQuickBar(false);
			} else {
				activity.setQuickBar(true);
			}
		}
	}

}
