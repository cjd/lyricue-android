package org.lyricue.android;

import com.actionbarsherlock.app.SherlockDialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

public class PlaylistsDialogFragment extends SherlockDialogFragment {
	private static final String TAG = Lyricue.class.getSimpleName();

	public static PlaylistsDialogFragment newInstance(int title) {
		PlaylistsDialogFragment frag = new PlaylistsDialogFragment();
		Bundle args = new Bundle();
		args.putInt("title", title);
		frag.setArguments(args);

		return frag;
	}

	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Lyricue activity = (Lyricue) this.getActivity();

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(R.string.select_playlist);
		builder.setItems(activity.playlists_text,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Lyricue activity = (Lyricue) getActivity();
						activity.playlistid = activity.playlists_id[which];
						Log.i(TAG,"Loading "
								+ activity.playlists_text[which]);
						activity.load_playlist();
					}
				});
		AlertDialog alert = builder.create();
		return alert;
	}

}
