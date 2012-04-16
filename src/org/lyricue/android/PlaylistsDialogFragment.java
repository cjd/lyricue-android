package org.lyricue.android;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class PlaylistsDialogFragment extends DialogFragment {

	public static PlaylistsDialogFragment newInstance(int title) {
		PlaylistsDialogFragment frag = new PlaylistsDialogFragment();
		Bundle args = new Bundle();
		args.putInt("title", title);
		frag.setArguments(args);

		return frag;
	}

	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Lyricue activity = (Lyricue) this.getActivity();

		activity.logDebug("load_playlists");
		if (activity.playlistid == 0) {
			return null;
		}
		String Query = "SELECT title,id FROM playlists"
				+ " LEFT JOIN playlist ON playlist.data=playlists.id"
				+ " AND playlist.data NOT LIKE '%-%'"
				+ " AND (type='play' OR type='sub')"
				+ " WHERE data IS NULL AND playlists.id > 0" + " ORDER BY id";
		JSONArray jArray = activity.runQuery("lyricDb", Query);
		if (jArray == null) {
			return null;
		}
		try {
			activity.playlists_text = new String[jArray.length()];
			activity.playlists_id = new int[jArray.length()];

			for (int i = 0; i < jArray.length(); i++) {
				JSONObject results = jArray.getJSONObject(i);
				activity.playlists_id[i] = results.getInt("id");
				activity.playlists_text[i] = results.getString("title");
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setTitle(R.string.select_playlist);
			builder.setItems(activity.playlists_text,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Lyricue activity = (Lyricue) getActivity();
							activity.playlistid = activity.playlists_id[which];
							System.err.println("Loading "
									+ activity.playlists_text[which]);
							LyricuePagerAdapter adapter = activity.adapter;
							PlaylistFragment frag = (PlaylistFragment) adapter
									.getFragment(1);
							frag.load_playlist();
						}
					});
			AlertDialog alert = builder.create();
			return alert;
		} catch (JSONException e) {
			activity.logError("Error parsing data " + e.toString());
		}
		return null;
	}

}
