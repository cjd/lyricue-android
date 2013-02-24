package org.lyricue.android;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pl.polidea.treeview.InMemoryTreeStateManager;
import pl.polidea.treeview.TreeBuilder;
import pl.polidea.treeview.TreeStateManager;
import pl.polidea.treeview.TreeViewList;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

public class PlaylistFragment extends Fragment {
	private final Set<Long> selected = new HashSet<Long>();

	public static final String PREFS_NAME = "LyricuePrefsFile";
	private static Lyricue activity = null;
	private static View v = null;
	private TreeViewList treeView;
	private static int LEVEL_NUMBER = 6;
	private TreeStateManager<Long> manager = null;
	private PlaylistAdapter adapter;
	public HashMap<Long, String> playlistmap = new HashMap<Long, String>();
	public HashMap<Long, Bitmap> imagemap = new HashMap<Long, Bitmap>();
	public boolean show_previews = false;
	private PlaylistFragment fragment = null;
	private ProgressDialog progressPlaylist = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		activity = (Lyricue) this.getActivity();
		fragment = this;
		v = (View) inflater.inflate(R.layout.playlist, null);
		treeView = (TreeViewList) v.findViewById(R.id.playlistView);
		setHasOptionsMenu(true);
		registerForContextMenu(treeView);
		return v;
	}

	@Override
	public void onResume() {
		activity.logDebug("resume playlist");
		super.onResume();
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
			load_playlist();
			return true;
		case R.id.select_playlist_menu:
			load_playlists();
			return true;
		case R.id.toggle_previews:
			toggle_previews();
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
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getGroupId() == 1) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
					.getMenuInfo();

			long itemid = treeView.getItemIdAtPosition(info.position);

			if (item.getItemId() == 0) {
				activity.logDebug("show item:" + itemid);
				activity.ld.runCommand_noreturn("display",
						String.valueOf(itemid), "");
			} else if (item.getItemId() == 1) {
				activity.logDebug("remove item:" + itemid);
				if (!activity.hostip.equals("#demo"))
					new RemoveItemTask().execute(itemid);
			}
			return true;
		} else {
			return false;
		}

	}

	void load_playlists() {
		activity.logDebug("load_playlists");
		if (activity.playlistid == 0) {
			return;
		}
		new LoadPlaylistsTask().execute();
	}

	private class LoadPlaylistsTask extends AsyncTask<Void, Void, Void> {
		protected Void doInBackground(Void... arg0) {
			if (activity.hostip.equals("#demo")) {
				activity.playlists_text = new String[1];
				activity.playlists_id = new int[1];
				activity.playlists_id[0] = 1;
				activity.playlists_text[0] = "Demo playlist";
				activity.showPlaylistsDialog(v);
				return null;
			}
			String Query = "SELECT title,id FROM playlists"
					+ " LEFT JOIN playlist ON playlist.data=playlists.id"
					+ " AND playlist.data NOT LIKE '%-%'"
					+ " AND (type='play' OR type='sub')"
					+ " WHERE data IS NULL AND playlists.id > 0"
					+ " ORDER BY id";
			LyricueDisplay ld = new LyricueDisplay(activity.hostip);

			JSONArray jArray = ld.runQuery("lyricDb", Query);
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
			} catch (JSONException e) {
				activity.logError("Error parsing data " + e.toString());
				return null;
			}
			activity.showPlaylistsDialog(v);
			return null;
		}
	}

	void load_playlist() {
		activity.logDebug("load_playlist");
		if (activity.playlistid != -1) {
			if (progressPlaylist != null)
				progressPlaylist.dismiss();
			progressPlaylist = ProgressDialog.show(activity, "",
					"Loading Playlist..", true);
		}
		new LoadPlaylistTask().execute(activity.playlistid);
	}

	private class LoadPlaylistTask extends
			AsyncTask<Integer, Void, PlaylistAdapter> {

		protected PlaylistAdapter doInBackground(Integer... arg0) {
			manager = new InMemoryTreeStateManager<Long>();
			final TreeBuilder<Long> treeBuilder = new TreeBuilder<Long>(manager);
			if (activity.playlistid > 0) {
				add_playlist(treeBuilder, activity.playlistid, 0);
			} else {
				treeBuilder.sequentiallyAddNextNode((long) 0, 0);
				playlistmap.put((long) 0,
						"No playlist loaded\nLoad one from the menu");
			}
			adapter = new PlaylistAdapter(activity, fragment, selected,
					manager, LEVEL_NUMBER);
			return adapter;
		}

		protected void onPostExecute(PlaylistAdapter result) {
			activity.logDebug("done loading playlist");
			treeView.setAdapter(result);
			manager.collapseChildren(null);
			treeView.setCollapsible(true);
			if (progressPlaylist != null)
				progressPlaylist.dismiss();
		}
	}

	void add_playlist(TreeBuilder<Long> treeBuilder, int playlistid, int level) {
		activity.logDebug("add_playlist:" + playlistid);

		if (activity.hostip.equals("#demo")) {
			load_demo_playlist(treeBuilder);
			return;
		}
		if (playlistid <= 0) {
			return;
		}
		String Query;
		if (show_previews) {
			Query = "SELECT playorder, type, data, HEX(snapshot) FROM playlist"
					+ " WHERE playlist=" + playlistid + " ORDER BY playorder";
		} else {
			Query = "SELECT playorder, type, data FROM playlist"
					+ " WHERE playlist=" + playlistid + " ORDER BY playorder";
		}
		JSONArray jArray = activity.ld.runQuery("lyricDb", Query);
		if (jArray == null) {
			return;
		}
		int newlevel = level + 1;
		for (int i = 0; i < jArray.length(); i++) {
			try {
				JSONObject results = jArray.getJSONObject(i);
				treeBuilder.sequentiallyAddNextNode(
						results.getLong("playorder"), level);
				if (results.getString("type").equals("play")
						|| results.getString("type").equals("sub")) {
					String Query2 = "SELECT * FROM playlists WHERE id="
							+ results.getString("data");
					JSONArray pArray = activity.ld.runQuery("lyricDb", Query2);
					if (pArray != null && pArray.length() > 0) {
						playlistmap.put(results.getLong("playorder"), pArray
								.getJSONObject(0).getString("title"));
						add_playlist(treeBuilder, results.getInt("data"),
								newlevel);
					}
				} else if (results.getString("type").equals("song")) {
					String Query2 = "SELECT pagetitle, lyrics FROM page WHERE pageid="
							+ results.getString("data");
					JSONArray pArray = activity.ld.runQuery("lyricDb", Query2);
					if (pArray != null && pArray.length() > 0) {
						String[] lines = pArray.getJSONObject(0)
								.getString("lyrics").split("\n");
						playlistmap.put(results.getLong("playorder"), lines[0]);
					}
				} else if (results.getString("type").equals("vers")) {
					playlistmap.put(results.getLong("playorder"), "Verses "
							+ results.getString("data"));
				} else if (results.getString("type").equals("file")) {
					playlistmap.put(results.getLong("playorder"), "File:"
							+ results.getString("data"));
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
							playlistmap.put(results.getLong("playorder"),
									"Image:" + desc);
						} else {
							playlistmap.put(results.getLong("playorder"),
									"Image: unknown");
						}
					} else {
						playlistmap.put(results.getLong("playorder"), "Image:"
								+ imageItem[1]);
					}
				} else {
					playlistmap.put(results.getLong("playorder"),
							"Unknown item type");
				}
				if (show_previews) {
					if (results.getString("HEX(snapshot)") != null) {
						byte[] imageBytes = hexStringToByteArray(results
								.getString("HEX(snapshot)"));
						imagemap.put(results.getLong("playorder"),
								BitmapFactory.decodeByteArray(imageBytes, 0,
										imageBytes.length));
					} else {
						imagemap.put(results.getLong("playorder"),null);
					}
				}
			} catch (JSONException e) {
				activity.logError("Error parsing data " + e.toString());
				return;
			}

		}
	}

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
					.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	void load_demo_playlist(TreeBuilder<Long> treeBuilder) {
		int[] DEMO_NODES = new int[] { 0, 0, 1, 1, 1, 2, 2, 1, 1, 2, 1, 0, 0,
				0, 1, 2, 3, 2, 0, 0, 1, 2, 0, 1, 2, 0, 1 };
		for (int i = 0; i < DEMO_NODES.length; i++) {
			treeBuilder.sequentiallyAddNextNode((long) i, DEMO_NODES[i]);
			playlistmap.put((long) i, "Demo Item " + i);
		}
	}

	private class RemoveItemTask extends AsyncTask<Long, Void, Void> {
		protected Void doInBackground(Long... args) {
			Long itemid = args[0];
			remove_single_item(itemid);
			load_playlist();
			return null;
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
				if (jArray != null) {
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
			return;
		}
	}

	void toggle_previews() {
		show_previews = !show_previews;
		load_playlist();
	}

}
