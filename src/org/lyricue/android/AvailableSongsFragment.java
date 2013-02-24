package org.lyricue.android;

import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class AvailableSongsFragment extends Fragment {
	Lyricue activity = null;
	View v = null;
	ListView songlist = null;
	AvailableSongsAdapter adapter = null;
	private EditText filterText = null;
	private ArrayList<AvailableSongItem> items = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = (View) inflater.inflate(R.layout.available, null);
		songlist = (ListView) v.findViewById(R.id.available_songlist);
		filterText = (EditText) v.findViewById(R.id.available_search);
		filterText.addTextChangedListener(filterTextWatcher);
		setHasOptionsMenu(true);
		activity = (Lyricue) getActivity();
		return v;

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.songs_menu, menu);
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
		case R.id.refresh_songlist:
			load_available();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getGroupId() == 2) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
					.getMenuInfo();

			if (adapter != null) {
				activity.logDebug("pos:" + info.position);
				long itemid = adapter.getItemId(info.position);
				String itemtext = adapter.getItem(info.position).main;

				if (item.getItemId() == 0) {
					activity.logDebug("add to playlist:" + itemid + "-"
							+ itemtext);
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
		activity.logDebug("resume available");
		super.onResume();
	}

	public void load_available() {
		activity.logDebug("load_available");
		new AvailableSongsTask().execute();
	}

	private class AvailableSongsTask extends
			AsyncTask<Void, Void, AvailableSongsAdapter> {
		@Override
		protected AvailableSongsAdapter doInBackground(Void... arg0) {
			items = new ArrayList<AvailableSongItem>();
			if (activity.hostip.equals("#demo")) {
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
			LyricueDisplay ld = new LyricueDisplay(activity.hostip);
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
			activity.logDebug("Songlist loaded");
			songlist.setAdapter(result);
			songlist.setTextFilterEnabled(true);
			songlist.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					activity.logDebug("pos:" + position + " pid:"
							+ activity.playlistid);
				}
			});
			registerForContextMenu(songlist);
		}
	}

	public void add_to_playlist(long itemid) {
		if (activity.playlistid < 0)
			return;
		activity.logDebug("add to playlist");
		new AddSongTask().execute(itemid);

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

	private TextWatcher filterTextWatcher = new TextWatcher() {
		@Override
		public void afterTextChanged(Editable s) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			if (adapter != null)
				adapter.getFilter().filter(s);
		}

	};
}
