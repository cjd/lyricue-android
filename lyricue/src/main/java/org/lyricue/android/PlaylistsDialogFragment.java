package org.lyricue.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

public class PlaylistsDialogFragment extends DialogFragment {
    private static final String TAG = Lyricue.class.getSimpleName();

    @SuppressWarnings("SameParameterValue")
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
                        Log.i(TAG, "Loading "
                                + activity.playlists_text[which]);
                        activity.load_playlist();
                    }
                }
        );
        return builder.create();
    }

}
