package org.lyricue.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ControlFragment extends Fragment {
    private static Lyricue activity = null;
    private View v = null;
    private LayoutInflater inflater = null;

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.inflater = inflater;
        v = inflater.inflate(R.layout.control, container, false);
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
        } else {
            super.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (activity != null) {
            if (isVisibleToUser) {
                activity.setQuickBar(false);
            } else {
                activity.setQuickBar(true);
            }
        }
    }

}
