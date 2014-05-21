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

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PlaylistFragment extends Fragment {
    private final String TAG = "Lyricue";
    private static Lyricue activity = null;
    public ProgressDialog progressPlaylist = null;
    public Long parent_playlist = (long) -1;
    public String parent_playlisttitle = "";
    private ListView listView;
    private PlaylistAdapter adapter;
    private PlaylistFragment fragment = null;
    private Long this_playlist = (long) 0;
    private String this_playlisttitle = "";

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        activity = (Lyricue) this.getActivity();
        fragment = this;
        View v = inflater.inflate(R.layout.playlist, container, false);
        listView = (ListView) v.findViewById(R.id.playlistView);
        setHasOptionsMenu(true);
        registerForContextMenu(listView);
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        Log.i(TAG, "resume playlist");
        super.onResume();
        load_playlist(this_playlist,this_playlisttitle);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.playlists_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_playlist:
                refresh();
                return true;
            case R.id.select_playlist_menu:
                load_playlists();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.playlistView) {
            menu.setHeaderTitle("Item Actions");
            menu.add(1, 0, 0, "Show Item");
            menu.add(1, 1, 0, "Remove Item");
        }
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        if (item.getGroupId() == 1) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                    .getMenuInfo();

            long itemid = listView.getItemIdAtPosition(info.position);

            if (item.getItemId() == 0) {
                Log.i(TAG, "show item:" + itemid);
                activity.ld.runCommand_noreturn("display",
                        String.valueOf(itemid), "");
            } else if (item.getItemId() == 1) {
                Log.i(TAG, "remove item:" + itemid);
                if (activity.hosts != null)
                    new RemoveItemTask().execute(itemid);
            }
            return true;
        } else {
            return false;
        }

    }

    void load_playlists() {
        Log.i(TAG, "load_playlists()");
        if (activity.playlistid == 0) {
            return;
        }
        if (this_playlist == 0) {
            this_playlist = activity.playlistid;
        }
        new LoadPlaylistsTask().execute();
    }

    void refresh() {
        new LoadPlaylistTask().execute(this_playlist);
    }

    void load_playlist(Long playlistid, String title) {
        Log.i(TAG, "load_playlist(" + playlistid + "-" + title + ")");
        this_playlist = playlistid;
        this_playlisttitle = title;
        new LoadPlaylistTask().execute(playlistid);
    }

    void add_playlist() {
        Log.i(TAG, "add_playlist:" + this_playlist);

        if (activity.hosts == null) {
            load_demo_playlist();
            return;
        }
        if (this_playlist <= 0) {
            return;
        }

        String Query;
        Query = "SELECT playlist FROM playlist WHERE data=" + this_playlist
                + " AND (type='play' OR type='sub')";
        parent_playlist = (long) activity.ld.runQuery_int("lyricDb", Query,
                "playlist");

        Query = "SELECT playorder, type, data FROM playlist"
                + " WHERE playlist=" + this_playlist + " ORDER BY playorder";
        JSONArray jArray = activity.ld.runQuery("lyricDb", Query);
        if (jArray == null) {
            return;
        }
        for (int i = 0; i < jArray.length(); i++) {
            try {
                JSONObject results = jArray.getJSONObject(i);
                if (results.getString("type").equals("play")
                        || results.getString("type").equals("sub")) {
                    String Query2 = "SELECT * FROM playlists WHERE id="
                            + results.getString("data");
                    JSONArray pArray = activity.ld.runQuery("lyricDb", Query2);
                    if (pArray != null && pArray.length() > 0) {
                        adapter.add(results.getLong("playorder"), pArray
                                .getJSONObject(0).getString("title"), results
                                .getString("type"), results.getLong("data"));
                        Log.i(TAG, "Add list:" + results.getLong("data") + "-"
                                + pArray.getJSONObject(0).getString("title"));
                    }
                } else if (results.getString("type").equals("song")) {
                    String Query2 = "SELECT pagetitle, lyrics FROM page WHERE pageid="
                            + results.getString("data");
                    JSONArray pArray = activity.ld.runQuery("lyricDb", Query2);
                    if (pArray != null && pArray.length() > 0) {
                        adapter.add(results.getLong("playorder"), pArray.getJSONObject(0).getString("lyrics"),
                                results.getString("type"), (long) 0);
                    }
                } else if (results.getString("type").equals("vers")) {
                    adapter.add(results.getLong("playorder"), "Verses "
                                    + results.getString("data"),
                            results.getString("type"), (long) 0
                    );
                } else if (results.getString("type").equals("file")) {
                    String filename = results.getString("data");
                    if (filename.startsWith("/var/tmp/lyricue-")) {
                        filename = filename.substring(
                                filename.lastIndexOf("/") + 1,
                                filename.length() - 4).replace("_", " ");
                        adapter.add(results.getLong("playorder"),
                                "Presentation: " + filename,
                                results.getString("type"), (long) 0);
                    } else {
                        adapter.add(
                                results.getLong("playorder"),
                                "File:"
                                        + results.getString("data").substring(
                                        results.getString("data")
                                                .lastIndexOf("/") + 1
                                ),
                                results.getString("type"), (long) 0
                        );
                    }
                } else if (results.getString("type").equals("imag")) {
                    String[] imageItem = results.getString("data")
                            .split(";", 2);
                    if (imageItem[0].equals("db")) {
                        String Query2 = "SELECT description FROM media WHERE id="
                                + imageItem[1];
                        JSONArray pArray = activity.ld.runQuery("mediaDb",
                                Query2);
                        if (pArray != null && pArray.length() > 0) {
                            String desc = pArray.getJSONObject(0).getString(
                                    "description");
                            adapter.add(results.getLong("playorder"), "Image:"
                                    + desc, results.getString("type"), (long) 0);
                        } else {
                            adapter.add(results.getLong("playorder"),
                                    "Image: unknown",
                                    results.getString("type"), (long) 0);
                        }
                    } else if (imageItem[0].equals("dir")) {
                        adapter.add(
                                results.getLong("playorder"),
                                "Image:"
                                        + imageItem[1].substring(imageItem[1]
                                        .lastIndexOf("/") + 1),
                                results.getString("type"), (long) 0
                        );
                    } else {
                        adapter.add(results.getLong("playorder"), "Image:"
                                        + imageItem[1], results.getString("type"),
                                (long) 0
                        );
                    }
                } else {
                    adapter.add(results.getLong("playorder"),
                            "Unknown item type", results.getString("type"),
                            (long) 0);
                }
            } catch (JSONException e) {
                activity.logError("Error parsing data " + e.toString());
                return;
            }

        }
    }

    void load_demo_playlist() {
        if (this_playlist == 1) {
            for (int i = 0; i < 13; i++) {
                adapter.add((long) i, "Demo Song " + i, "play", (long) 2);
                parent_playlist = (long) 0;
            }
        } else {
            for (int i = 0; i < 13; i++) {
                adapter.add((long) i, "Demo Item " + i, "demo", (long) 2);
                parent_playlist = (long) 1;
            }
        }

    }

    void remove_single_item(Long itemid) {
        String Query = "SELECT type,data FROM playlist WHERE playorder="
                + itemid;
        JSONArray jArray = activity.ld.runQuery("lyricDb", Query);
        if (jArray == null) {
            return;
        }
        try {
            JSONObject results = jArray.getJSONObject(0);
            if ((results.getString("type").equals("play"))
                    || (results.getString("type").equals("sub"))) {
                Query = "SELECT playorder FROM playlist WHERE playlist="
                        + results.getLong("data");
                JSONArray pArray = activity.ld.runQuery("lyricDb", Query);
                if (pArray != null) {
                    for (int i = 0; i < pArray.length(); i++) {
                        JSONObject item = pArray.getJSONObject(i);
                        remove_single_item(item.getLong("playorder"));
                    }
                }
                Query = "DELETE FROM playlist WHERE playlist="
                        + results.getLong("data");
                activity.ld.runQuery("lyricDb", Query);
                Query = "DELETE FROM playlists WHERE id="
                        + results.getLong("data");
                activity.ld.runQuery("lyricDb", Query);
            }
            Query = "DELETE FROM playlist WHERE playorder=" + itemid;
            activity.ld.runQuery("lyricDb", Query);
            Query = "DELETE FROM associations WHERE playlist=" + itemid;
            activity.ld.runQuery("lyricDb", Query);

        } catch (JSONException e) {
            activity.logError("Error parsing data " + e.toString());
        }
    }

    private class LoadPlaylistsTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... arg0) {
            if (activity.hosts == null) {
                activity.playlists_text = new String[1];
                activity.playlists_id = new long[1];
                activity.playlists_id[0] = (long) 1;
                activity.playlists_text[0] = "Demo playlist";
                activity.showPlaylistsDialog();
                return null;
            }
            String Query = "SELECT title,id FROM playlists"
                    + " LEFT JOIN playlist ON playlist.data=playlists.id"
                    + " AND playlist.data NOT LIKE '%-%'"
                    + " AND (type='play' OR type='sub')"
                    + " WHERE data IS NULL AND playlists.id > 0"
                    + " AND profile='" + activity.profile + " ' ORDER BY id";
            LyricueDisplay ld = new LyricueDisplay(activity.hosts[0]);

            JSONArray jArray = ld.runQuery("lyricDb", Query);
            if (jArray == null) {
                return null;
            }
            try {
                activity.playlists_text = new String[jArray.length()];
                activity.playlists_id = new long[jArray.length()];

                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject results = jArray.getJSONObject(i);
                    activity.playlists_id[i] = results.getLong("id");
                    activity.playlists_text[i] = results.getString("title");
                }
            } catch (JSONException e) {
                activity.logError("Error parsing data " + e.toString());
                return null;
            }
            activity.showPlaylistsDialog();
            return null;
        }
    }

    private class LoadPlaylistTask extends
            AsyncTask<Long, Void, PlaylistAdapter> {

        protected PlaylistAdapter doInBackground(Long... arg0) {
            adapter = new PlaylistAdapter(activity, fragment,
                    R.layout.playlist_item);
            if (activity.playlistid > 0) {
                add_playlist();
            } else {
                adapter.add((long) 0,
                        "No playlist loaded\nClick here to select one",
                        "unloaded", (long) 0);
            }
            return adapter;
        }

        protected void onPostExecute(PlaylistAdapter adapter) {
            listView.setAdapter(adapter);
            Button button = (Button) fragment.getView().findViewById(
                    R.id.buttonPlayUp);
            if (parent_playlist > 0) {
                button.setVisibility(View.VISIBLE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "Go up to " + parent_playlist);
                        load_playlist(parent_playlist, parent_playlisttitle);
                    }
                });
                button.setText(this_playlisttitle);
            } else {
                button.setVisibility(View.GONE);
            }
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, final View view,
                                        int position, long id) {
                    Log.i(TAG, "Clicked:" + String.valueOf(id));
                    PlaylistItem item = (PlaylistItem) parent
                            .getItemAtPosition(position);
                    if (item.type.equals("unloaded")) {
                        fragment.load_playlists();
                    } else {
                        if (item.type.equals("play") || item.type.equals("sub")) {
                            Log.i(TAG,
                                    "Load playlist:"
                                            + String.valueOf(item.data)
                            );
                            load_playlist(item.data,item.title);
                        } else {
                            activity.ld.runCommand_noreturn("display",
                                    String.valueOf(item.id), "");
                        }
                    }
                }
            });
            if (progressPlaylist != null)
                progressPlaylist.dismiss();
            Log.i(TAG, "done loading playlist");
        }
    }

    private class RemoveItemTask extends AsyncTask<Long, Void, Void> {
        protected Void doInBackground(Long... args) {
            Long itemid = args[0];
            remove_single_item(itemid);
            refresh();
            return null;
        }
    }

}
