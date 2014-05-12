/*
 * This file is part of Lyricue.
 *
 *     Lyricue is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Foobar is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lyricue.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lyricue.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

public class PlaylistsDialogFragment extends DialogFragment {
    private final String TAG = this.getClass().getSimpleName();

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
