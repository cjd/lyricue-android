package org.lyricue.android;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class AvailableFragment extends Fragment {
	Lyricue activity = null;
	View v = null;
	ListView songlist = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		v = (View) inflater.inflate(R.layout.available, null);
		songlist = (ListView) v.findViewById(R.id.available_songlist);
		setHasOptionsMenu(true);
		registerForContextMenu(songlist);
		activity = (Lyricue) getActivity();
		return v;

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	  if (v.getId()==R.id.available_songlist) {
		  menu.setHeaderTitle("Item Actions");
		  menu.add(Menu.NONE,0,0,"Add to playlist");
	  }
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		
		long itemid = songlist.getAdapter().getItemId(info.position);

		if (item.getItemId() == 0) {
			activity.logDebug("add to playlist:"+itemid);
		}
		
	  return true;
	}

	@Override
	public void onResume() {
		System.err.println("resume");
		super.onResume();
		load_available();
	}

	public void load_available() {
		String Query = "SELECT id,title,songnum,book FROM lyricMain WHERE id > 0 ORDER BY title,book";
		JSONArray jArray = activity.runQuery("lyricDb", Query);

		if (jArray != null) {
			try {
				String[] main = new String[jArray.length()];
				String[] small = new String[jArray.length()];
				int[] id = new int[jArray.length()];

				for (int i = 0; i < jArray.length(); i++) {
					JSONObject results = jArray.getJSONObject(i);
					main[i] = results.getString("title");
					if (! results.getString("songnum").equals("0")) {
						small[i] = results.getString("songnum")+" - ";
					} else {
						small[i] = "";
					}
					small[i] = small[i] + results.getString("book");
					id[i] = results.getInt("id");
				}
				AvailableSongsAdapter adapter = new AvailableSongsAdapter(
						activity.getApplicationContext(), main, small, id);
				songlist.setAdapter(adapter);
				songlist.setTextFilterEnabled(true);
				songlist.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						System.err.println("pos:" + position + " pid:"
								+ activity.playlistid);
					}
				});
			} catch (JSONException e) {
				activity.logError("Error parsing data " + e.toString());
			}
		}
	}
}
