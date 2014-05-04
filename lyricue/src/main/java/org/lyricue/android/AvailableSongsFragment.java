package org.lyricue.android;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

class AvailableSongsFragment extends Fragment {
    private static final String TAG = Lyricue.class.getSimpleName();
    private Lyricue activity = null;
    private ListView songlist = null;
    private AvailableSongsAdapter adapter = null;
    private ArrayList<AvailableSongItem> items = null;

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.available, null);
        songlist = (ListView) v.findViewById(R.id.available_songlist);
        setHasOptionsMenu(true);
        activity = (Lyricue) getActivity();
        return v;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.songs_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat
                .getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String arg0) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String arg0) {
                // TODO Auto-generated method stub if (adapter != null)
                adapter.getFilter().filter(arg0);
                return false;
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.available_songlist) {
            menu.setHeaderTitle("Item Actions");
            menu.add(2, 0, 0, "Add to playlist");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_songlist:
                load_available();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        if (item.getGroupId() == 2) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                    .getMenuInfo();

            if (adapter != null) {
                Log.i(TAG, "pos:" + info.position);
                long itemid = adapter.getItemId(info.position);
                String itemtext = adapter.getItem(info.position).main;

                if (item.getItemId() == 0) {
                    Log.i(TAG, "add to playlist:" + itemid + "-" + itemtext);
                    add_to_playlist(itemid);
                }
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onResume() {
        Log.i(TAG, "resume available");
        super.onResume();
    }

    public void load_available() {
        Log.i(TAG, "load_available");
        new AvailableSongsTask().execute();
    }

    void add_to_playlist(long itemid) {
        if (activity.playlistid < 0)
            return;
        Log.i(TAG, "add to playlist");
        new AddSongTask().execute(itemid);

    }

    private class AvailableSongsTask extends
            AsyncTask<Void, Void, AvailableSongsAdapter> {
        @Override
        protected AvailableSongsAdapter doInBackground(Void... arg0) {
            items = new ArrayList<AvailableSongItem>();
            if (activity.hosts == null) {
                for (int a = 0; a < 100; a++) {
                    items.add(a, new AvailableSongItem());
                    items.get(a).main = "Demo Song " + a;
                    items.get(a).small = "First line of song here";
                }
                Collections.sort(items);
                adapter = new AvailableSongsAdapter(
                        activity.getApplicationContext(), items);
                return adapter;
            }

            LyricueDisplay ld = new LyricueDisplay(activity.hosts[0]);
            String Query = "SELECT COUNT(id) AS count FROM lyricMain WHERE id > 0";
            int size = ld.runQuery_int("lyricDb", Query, "count");
            if (size > 0) {
                for (int start = 0; start < size; start = start + 100) {
                    Query = "SELECT id,title,songnum,book FROM lyricMain WHERE id > 0 LIMIT "
                            + start + ", 100";
                    JSONArray jArray = ld.runQuery("lyricDb", Query);

                    if (jArray != null) {
                        try {
                            for (int i = 0; i < jArray.length(); i++) {
                                JSONObject results = jArray.getJSONObject(i);
                                items.add(i, new AvailableSongItem());
                                items.get(i).main = results.getString("title");
                                if (!results.getString("songnum").equals("0")) {
                                    items.get(i).small = results
                                            .getString("songnum") + " - ";
                                } else {
                                    items.get(i).small = "";
                                }
                                items.get(i).small = items.get(i).small
                                        + results.getString("book");
                                items.get(i).id = results.getInt("id");
                            }
                        } catch (JSONException e) {
                            activity.logError("Error parsing data "
                                    + e.toString());
                        }
                    }
                }

                Collections.sort(items);
                adapter = new AvailableSongsAdapter(
                        activity.getApplicationContext(), items);
                return adapter;
            }
            return null;
        }

        protected void onPostExecute(AvailableSongsAdapter result) {
            Log.i(TAG, "Songlist loaded");
            songlist.setAdapter(result);
            songlist.setTextFilterEnabled(true);
            songlist.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    Log.i(TAG, "pos:" + position + " pid:"
                            + activity.playlistid);
                }
            });
            registerForContextMenu(songlist);
        }
    }

    private class AddSongTask extends AsyncTask<Long, Void, Void> {
        protected Void doInBackground(Long... args) {
            Long itemid = args[0];
            String Query = "SELECT MAX(playorder) as playorder FROM playlist";
            int playorder = activity.ld.runQuery_int("lyricDb", Query,
                    "playorder");
            playorder++;

            Query = "SELECT MAX(id) as id FROM playlists";
            int playlist = activity.ld.runQuery_int("lyricDb", Query, "id");
            playlist++;

            Query = "INSERT INTO playlist (playorder, playlist, data, type) VALUES ("
                    + playorder
                    + ", "
                    + activity.playlistid
                    + ", "
                    + playlist
                    + ", \"play\")";
            activity.ld.runQuery("lyricDb", Query);

            Query = "SELECT title,pageid,keywords FROM lyricMain, page WHERE songid=id AND id="
                    + itemid + " ORDER BY pagenum";
            JSONArray jArray = activity.ld.runQuery("lyricDb", Query);
            if (jArray == null) {
                return null;
            }
            String title = "Unknown";
            for (int i = 0; i < jArray.length(); i++) {
                try {
                    JSONObject results = jArray.getJSONObject(i);
                    title = results.getString("title");
                    playorder++;
                    Query = "INSERT INTO playlist (playorder, playlist, data,type) VALUES ("
                            + playorder
                            + ", "
                            + playlist
                            + ","
                            + results.getString("pageid") + ", \"song\")";
                    activity.ld.runQuery("lyricDb", Query);
                } catch (JSONException e) {
                    activity.logError("Error parsing data " + e.toString());
                    return null;
                }
            }

            Query = "INSERT INTO playlists (id,title,ref) VALUES (" + playlist
                    + ",\"" + title + "\"," + itemid + ")";
            activity.ld.runQuery("lyricDb", Query);
            return null;
        }
    }
}
