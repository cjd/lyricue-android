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
	private boolean collapsible;
	public HashMap<Long, String> playlistmap = new HashMap<Long, String>();

	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		boolean newCollapsible;
		activity = (Lyricue) this.getActivity();
		v = (View) inflater.inflate(R.layout.playlist, null);
		treeView = (TreeViewList) v.findViewById(R.id.playlistView);

		if (savedInstanceState == null) {
			load_playlist();
		} else {
			manager = (TreeStateManager<Long>) savedInstanceState
					.getSerializable("treeManager");
			newCollapsible = savedInstanceState.getBoolean("collapsible");
			adapter = new PlaylistAdapter(activity, this, selected, manager,
					LEVEL_NUMBER);
			treeView.setAdapter(adapter);
			setCollapsible(newCollapsible);
		}
		setHasOptionsMenu(true);
		registerForContextMenu(treeView);
		return v;
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		outState.putSerializable("treeManager", manager);
		outState.putBoolean("collapsible", this.collapsible);
		super.onSaveInstanceState(outState);
	}

	protected final void setCollapsible(final boolean newCollapsible) {
		this.collapsible = newCollapsible;
		treeView.setCollapsible(this.collapsible);
	}

	@Override
	public void onResume() {
		activity.logDebug("resume playlist");
		super.onResume();
		load_playlist();
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
			activity.showPlaylistsDialog(v);
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
				activity.runCommand_noreturn("display", String.valueOf(itemid),
						"");
			} else if (item.getItemId() == 1) {
				activity.logDebug("remove item:" + itemid);
			}
			return true;
		} else {
			return false;
		}

	}

	void load_playlist() {
		activity.logDebug("load_playlist");
		final PlaylistFragment fragment = this;
		new Thread(new Runnable() {
			public void run() {
				v.post(new Runnable() {
					public void run() {

						manager = new InMemoryTreeStateManager<Long>();
						final TreeBuilder<Long> treeBuilder = new TreeBuilder<Long>(
								manager);
						if (activity.playlistid > 0) {
							add_playlist(treeBuilder, activity.playlistid, 0);
						} else {
							treeBuilder.sequentiallyAddNextNode((long) 0, 0);
							playlistmap
									.put((long) 0,
											"No playlist loaded\nLoad one from the menu");
						}
						adapter = new PlaylistAdapter(activity, fragment, selected,
								manager, LEVEL_NUMBER);
						treeView.setAdapter(adapter);
						manager.collapseChildren(null);
						setCollapsible(true);
					}
				});
			}
		}).start();

	}

	void add_playlist(TreeBuilder<Long> treeBuilder, int playlistid, int level) {
		activity.logDebug("add_playlist:" + playlistid);

		if (playlistid <= 0) {
			return;
		}
		String Query = "SELECT * FROM playlist" + " WHERE playlist="
				+ playlistid + " ORDER BY playorder";
		JSONArray jArray = activity.runQuery("lyricDb", Query);
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
					JSONArray pArray = activity.runQuery("lyricDb", Query2);
					if (pArray != null) {
						playlistmap.put(results.getLong("playorder"), pArray
								.getJSONObject(0).getString("title"));
						add_playlist(treeBuilder, results.getInt("data"),
								newlevel);
					}
				} else if (results.getString("type").equals("song")) {
					String Query2 = "SELECT pagetitle, lyrics FROM page WHERE pageid="
							+ results.getString("data");
					JSONArray pArray = activity.runQuery("lyricDb", Query2);
					if (pArray != null) {
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
				} else {
					playlistmap.put(results.getLong("playorder"),
							"Unknown item type");
				}
			} catch (JSONException e) {
				activity.logError("Error parsing data " + e.toString());
				return;
			}

		}
	}
}
