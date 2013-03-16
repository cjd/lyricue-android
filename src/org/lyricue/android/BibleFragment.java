package org.lyricue.android;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class BibleFragment extends Fragment {
	Lyricue activity = null;
	View v = null;
	String biblename = "";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		activity = (Lyricue) getActivity();
		v = (View) inflater.inflate(R.layout.bible, null);
		return v;
	}

	public void load_bible() {
		Spinner book = (Spinner) v.findViewById(R.id.spinBibleBook);
		book.setOnItemSelectedListener(new BookOnItemSelectedListener());
		Spinner spin = (Spinner) v.findViewById(R.id.spinBibleChapter);
		spin.setOnItemSelectedListener(new BookOnItemSelectedListener());
		spin = (Spinner) v.findViewById(R.id.spinBibleVerseStart);
		spin.setOnItemSelectedListener(new BookOnItemSelectedListener());
		spin = (Spinner) v.findViewById(R.id.spinBibleVerseEnd);
		spin.setOnItemSelectedListener(new BookOnItemSelectedListener());
		Button b = (Button) v.findViewById(R.id.buttonBibleAdd);
		b.setOnClickListener(new BibleOnClickListener());
		b = (Button) v.findViewById(R.id.buttonBibleShow);
		b.setOnClickListener(new BibleOnClickListener());

		String status = activity.ld.runCommand("status", "", "");
		if (status.equals(""))
			return;
		String biblename = status.substring(status.indexOf(",T:") + 3);
		biblename = biblename.substring(0, biblename.lastIndexOf(","));

		// Find Bibles
		String bibles = activity.ld.runCommand("bible", "available", "");
		try {
			JSONObject json = new JSONObject(bibles);
			JSONArray jArray = json.getJSONArray("results");
			Spinner spinBible = (Spinner) v.findViewById(R.id.spinBibleVersion);
			ArrayList<String> spinArray = new ArrayList<String>();
			activity.bibles_text = new String[jArray.length()];
			activity.bibles_id = new String[jArray.length()];
			activity.bibles_type = new String[jArray.length()];
			int selected=0;
				
			for (int i = 0; i < jArray.length(); i++) {
				JSONObject results = jArray.getJSONObject(i);
				activity.bibles_id[i]=results.getString("name");
				activity.bibles_type[i]=results.getString("type");
				if (activity.bibles_type[i].equals("db")) {
					activity.bibles_id[i]+="@bibleDb";
				}
				activity.bibles_text[i]=results.getString("description");
				//activity.logDebug(activity.bibles_id[i]+":"+activity.bibles_type[i] + ":" + activity.bibles_text[i] + "="+ biblename);
				if (biblename.equals(results.getString("name"))) {
					selected=i;
				}
				spinArray.add(activity.bibles_text[i]);
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
					android.R.layout.simple_spinner_item, spinArray);
			spinBible.setAdapter(adapter);
			spinBible.setSelection(selected);
			spinBible
					.setOnItemSelectedListener(new BookOnItemSelectedListener());
			book.setClickable(true);
			book.setSelection(0);
			select_book(book.getItemAtPosition(0).toString());
		} catch (JSONException e) {
			activity.logError("Error parsing data " + e.toString());
		}
	}

	public class BibleOnClickListener implements OnClickListener {
		@Override
		public void onClick(View vi) {
			activity.logDebug("onClickBible");
			Spinner spin = (Spinner) v.findViewById(R.id.spinBibleBook);
			String bookname = spin.getSelectedItem().toString();
			spin = (Spinner) v.findViewById(R.id.spinBibleChapter);
			String chapter = spin.getSelectedItem().toString();
			spin = (Spinner) v.findViewById(R.id.spinBibleVerseStart);
			String startverse = spin.getSelectedItem().toString();
			spin = (Spinner) v.findViewById(R.id.spinBibleVerseEnd);
			String endverse = spin.getSelectedItem().toString();

			switch (vi.getId()) {
			case R.id.buttonBibleAdd:
				if (!activity.hostip.equals("#demo")) {
					activity.logDebug("Adding " + bookname + " " + chapter
							+ ":" + startverse + "-" + endverse);
					new AddVerseTask().execute(bookname, chapter, startverse,
							endverse);
				}
				break;
			case R.id.buttonBibleShow:
				activity.logDebug("Showing " + bookname + " " + chapter + ":"
						+ startverse + "-" + endverse);
				activity.ld.runCommand_noreturn("bible", "verse", bookname + ":" + chapter + ":"
						+ startverse + "-" + chapter + ":" + endverse);
				break;
			}
		}
	}

	public class BookOnItemSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			switch (parent.getId()) {
			case R.id.spinBibleVersion:
				select_bible(pos);
				break;
			case R.id.spinBibleBook:
				select_book(parent.getItemAtPosition(pos).toString());
				break;
			case R.id.spinBibleChapter:
				select_chapter(Integer.parseInt(parent.getItemAtPosition(pos)
						.toString()));
				break;
			case R.id.spinBibleVerseStart:
				break;
			case R.id.spinBibleVerseEnd:
				break;
			}
		}

		public void onNothingSelected(AdapterView<?> parent) {

		}
	}

	void select_bible(int bible) {
		activity.ld.runCommand("change_to_db", activity.bibles_id[bible],activity.bibles_type[bible]);
		Spinner book=(Spinner) v.findViewById(R.id.spinBibleBook);
		book.setClickable(true);
		book.setSelection(0);
		select_book(book.getItemAtPosition(0).toString());
	}

	void select_book(String book) {
		Spinner spin = (Spinner) v.findViewById(R.id.spinBibleBook);
		String bookname = spin.getSelectedItem().toString();
		String maxchap = activity.ld.runCommand("bible", "maxchapter", bookname).trim();
		Spinner spinChap = (Spinner) v.findViewById(R.id.spinBibleChapter);
		ArrayList<String> spinArray = new ArrayList<String>();
		activity.logDebug(maxchap+"maxchap");
		for (int i = 1; i <= Integer.parseInt(maxchap); i++) {
			spinArray.add(String.valueOf(i));
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
				android.R.layout.simple_spinner_item, spinArray);
		spinChap.setAdapter(adapter);
		v.findViewById(R.id.spinBibleChapter).setClickable(true);
	}

	void select_chapter(int chapter) {
		Spinner spin = (Spinner) v.findViewById(R.id.spinBibleBook);
		String bookname = spin.getSelectedItem().toString();
		String maxverse = activity.ld.runCommand("bible", "maxverse", bookname+";"+chapter).trim();
		
		ArrayList<String> spinArrayS = new ArrayList<String>();
		ArrayList<String> spinArrayE = new ArrayList<String>();
		for (int i = 1; i <= Integer.parseInt(maxverse); i++) {
			spinArrayS.add(String.valueOf(i));
			spinArrayE.add(String.valueOf(i));
		}

		Spinner spinS = (Spinner) v.findViewById(R.id.spinBibleVerseStart);
		ArrayAdapter<String> adapterS = new ArrayAdapter<String>(activity,
				android.R.layout.simple_spinner_item, spinArrayS);
		spinS.setAdapter(adapterS);
		spinS.setOnItemSelectedListener(new BookOnItemSelectedListener());

		Spinner spinE = (Spinner) v.findViewById(R.id.spinBibleVerseEnd);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
				android.R.layout.simple_spinner_item, spinArrayE);
		spinE.setAdapter(adapter);
		spinE.setOnItemSelectedListener(new BookOnItemSelectedListener());
		v.findViewById(R.id.spinBibleVerseStart).setClickable(true);
		v.findViewById(R.id.spinBibleVerseEnd).setClickable(true);
	}

	private class AddVerseTask extends AsyncTask<String, Void, Void> {
		protected Void doInBackground(String... args) {
			String book = args[0];
			int chapter = Integer.parseInt(args[1]);
			int startverse = Integer.parseInt(args[2]);
			int endverse = Integer.parseInt(args[3]);

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

			for (int i = startverse; i <= endverse; i++) {
				playorder++;
				Query = "INSERT INTO playlist (playlist,playorder,type,data) VALUES ("
						+ playlist
						+ ", "
						+ playorder
						+ ", \"vers\", \""
						+ i
						+ "-" + i + "\")";
				activity.ld.runQuery("lyricDb", Query);
			}

			String title = book + ":" + chapter + ":" + startverse + "-"
					+ endverse;
			Query = "INSERT INTO playlists (id,title) VALUES (" + playlist
					+ ",\"" + title + "\")";
			activity.ld.runQuery("lyricDb", Query);
			/*
			 * PlaylistFragment frag = (PlaylistFragment)
			 * activity.adapter.getItem(1); frag.load_playlist();
			 */
			return null;
		}
	}

}