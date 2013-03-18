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
import android.widget.ImageButton;
import android.widget.Spinner;

public class BibleFragment extends Fragment {
	Lyricue activity = null;
	View v = null;
	String biblename = "";
	ArrayList<String> prevArray = new ArrayList<String>();

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
		ImageButton ib = (ImageButton) v.findViewById(R.id.imageBibleNext);
		ib.setOnClickListener(new BibleOnClickListener());
		ib = (ImageButton) v.findViewById(R.id.imageBiblePrev);
		ib.setOnClickListener(new BibleOnClickListener());

		String status = activity.ld.runCommand("status", "", "");
		if (status.equals(""))
			return;
		String biblename = status.substring(status.indexOf(",T:") + 3);
		biblename = biblename.substring(0, biblename.lastIndexOf(","));

		// Setup previous verses spinner
		spin = (Spinner) v.findViewById(R.id.spinBiblePrevious);
		ArrayAdapter<String> prevAdapter = new ArrayAdapter<String>(activity,
				android.R.layout.simple_spinner_item, prevArray);
		spin.setAdapter(prevAdapter);
		spin.setOnItemSelectedListener(new BookOnItemSelectedListener());

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
			int selected = 0;

			for (int i = 0; i < jArray.length(); i++) {
				JSONObject results = jArray.getJSONObject(i);
				activity.bibles_id[i] = results.getString("name");
				activity.bibles_type[i] = results.getString("type");
				if (activity.bibles_type[i].equals("db")) {
					activity.bibles_id[i] += "@bibleDb";
				}
				activity.bibles_text[i] = results.getString("description");
				if (biblename.equals(results.getString("name"))) {
					selected = i;
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
			select_book();
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
			String verse = bookname + ":" + chapter + ":" + startverse + "-"
					+ chapter + ":" + endverse;

			switch (vi.getId()) {
			case R.id.imageBibleNext:
				spin = (Spinner) v.findViewById(R.id.spinBibleVerseEnd);
				if (spin.getItemAtPosition(Integer.parseInt(endverse)) != null) {
					spin.setSelection(Integer.parseInt(endverse));
				}
				verse = bookname + ":" + chapter + ":" + startverse + "-"
						+ chapter + ":" + spin.getSelectedItem().toString();
				if (!activity.hostip.equals("#demo")) {
					show_verse(verse, "verse");
				}
				break;
			case R.id.imageBiblePrev:
				spin = (Spinner) v.findViewById(R.id.spinBibleVerseStart);
				if (spin.getItemAtPosition(Integer.parseInt(startverse) - 1) != null) {
					spin.setSelection(Integer.parseInt(startverse) - 2);
				}
				verse = bookname + ":" + chapter + ":"
						+ spin.getSelectedItem().toString() + "-" + chapter
						+ ":" + endverse;
				if (!activity.hostip.equals("#demo")) {
					show_verse(verse, "verse_start");
				}
				break;
			case R.id.buttonBibleAdd:
				activity.logDebug("Adding " + bookname + " " + chapter + ":"
						+ startverse + "-" + endverse);
				if (!activity.hostip.equals("#demo")) {
					if (activity.playlistid != -1) {
						new AddVerseTask().execute(bookname, chapter,
								startverse, endverse);
					}
				}
				break;
			case R.id.buttonBibleShow:
				spin = (Spinner) v.findViewById(R.id.spinBiblePrevious);
				if (!prevArray.contains(verse)) {
					prevArray.add(verse);
					ArrayAdapter<String> prevAdapter = new ArrayAdapter<String>(
							activity, android.R.layout.simple_spinner_item,
							prevArray);
					spin.setAdapter(prevAdapter);
					spin.setSelection(prevArray.size() - 1);
				}
				if (!activity.hostip.equals("#demo")) {
					show_verse(verse, "verse_start");
				}
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
				select_book();
				break;
			case R.id.spinBibleChapter:
				select_chapter();
				break;
			case R.id.spinBibleVerseStart:
				break;
			case R.id.spinBibleVerseEnd:
				break;
			case R.id.spinBiblePrevious:
				select_verse(parent.getItemAtPosition(pos).toString());
				break;
			}
		}

		public void onNothingSelected(AdapterView<?> parent) {

		}
	}

	String show_verse(String verse, String command) {
		activity.logDebug("Showing " + verse);
		String shown = activity.ld.runCommand("bible", command, verse).trim();
		if (shown != "") {
			activity.logDebug("Ret " + shown);
			if (!shown.equals(verse)) {
				String[] tokens = shown.split("[-:]");
				String startverse = tokens[2];
				String endverse = tokens[4];
				Spinner spin = (Spinner) v
						.findViewById(R.id.spinBibleVerseStart);
				spin.setSelection(Integer.parseInt(startverse) - 1);
				spin = (Spinner) v.findViewById(R.id.spinBibleVerseEnd);
				spin.setSelection(Integer.parseInt(endverse) - 1);
			}
			return shown;
		}
		return null;
	}

	void select_verse(String verse) {
		activity.logDebug("Select Verse " + verse);
		Spinner spin = (Spinner) v.findViewById(R.id.spinBibleBook);
		String[] tokens = verse.split("[-:]");
		String bible = tokens[0];
		int chapter = Integer.parseInt(tokens[1]);
		int start_verse = Integer.parseInt(tokens[2]);
		int end_verse = Integer.parseInt(tokens[4]);
		for (int i = 0; i < spin.getCount(); i++) {
			if (spin.getItemAtPosition(i).toString().equals(bible)) {
				spin.setSelection(i);
			}
		}
		spin = (Spinner) v.findViewById(R.id.spinBibleChapter);
		spin.setSelection(chapter - 1);
		spin = (Spinner) v.findViewById(R.id.spinBibleVerseStart);
		spin.setSelection(start_verse - 1);
		spin = (Spinner) v.findViewById(R.id.spinBibleVerseEnd);
		spin.setSelection(end_verse - 1);
	}

	void select_bible(int bible) {
		activity.ld.runCommand("change_to_db", activity.bibles_id[bible],
				activity.bibles_type[bible]);
		Spinner book = (Spinner) v.findViewById(R.id.spinBibleBook);
		book.setClickable(true);
		book.setSelection(0);
		select_book();
	}

	void select_book() {
		activity.logDebug("select_book");
		Spinner spin = (Spinner) v.findViewById(R.id.spinBibleBook);
		String bookname = spin.getSelectedItem().toString();
		Integer maxchap = Integer.parseInt(activity.ld.runCommand("bible",
				"maxchapter", bookname).trim());
		Spinner spinChap = (Spinner) v.findViewById(R.id.spinBibleChapter);
		ArrayList<String> spinArray = new ArrayList<String>();
		int current_chapter = 1;
		if (spinChap.getSelectedItem() != null) {
			current_chapter = Integer.parseInt(spinChap.getSelectedItem()
					.toString());
			if (current_chapter > maxchap) {
				current_chapter = maxchap;
			}
		}
		for (int i = 1; i <= maxchap; i++) {
			spinArray.add(String.valueOf(i));
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
				android.R.layout.simple_spinner_item, spinArray);
		spinChap.setAdapter(adapter);
		spinChap.setSelection(current_chapter - 1);
		v.findViewById(R.id.spinBibleChapter).setClickable(true);
	}

	void select_chapter() {
		activity.logDebug("select_chapter");
		Spinner spin = (Spinner) v.findViewById(R.id.spinBibleChapter);
		int chapter = Integer.parseInt(spin.getSelectedItem().toString());
		spin = (Spinner) v.findViewById(R.id.spinBibleBook);
		String bookname = spin.getSelectedItem().toString();
		Integer maxverse = Integer.parseInt(activity.ld.runCommand("bible",
				"maxverse", bookname + ";" + chapter).trim());
		int start_verse = 1;
		int end_verse = 1;
		Spinner spinS = (Spinner) v.findViewById(R.id.spinBibleVerseStart);
		if (spinS.getSelectedItem() != null) {
			start_verse = Integer.parseInt(spinS.getSelectedItem().toString());
			if (start_verse >= maxverse) {
				start_verse = maxverse;
			}
		}
		Spinner spinE = (Spinner) v.findViewById(R.id.spinBibleVerseEnd);
		if (spinE.getSelectedItem() != null) {
			end_verse = Integer.parseInt(spinE.getSelectedItem().toString());
			if (end_verse >= maxverse) {
				end_verse = maxverse;
			}
		}

		ArrayList<String> spinArrayS = new ArrayList<String>();
		ArrayList<String> spinArrayE = new ArrayList<String>();
		for (int i = 1; i <= maxverse; i++) {
			spinArrayS.add(String.valueOf(i));
			spinArrayE.add(String.valueOf(i));
		}

		ArrayAdapter<String> adapterS = new ArrayAdapter<String>(activity,
				android.R.layout.simple_spinner_item, spinArrayS);
		spinS.setAdapter(adapterS);
		spinS.setSelection(start_verse - 1);
		spinS.setOnItemSelectedListener(new BookOnItemSelectedListener());

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
				android.R.layout.simple_spinner_item, spinArrayE);
		spinE.setAdapter(adapter);
		spinE.setSelection(end_verse - 1);
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

			// Create playlist for verse
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

			String title = book + ":" + chapter + ":" + startverse + "-"
					+ endverse;
			Query = "INSERT INTO playlists (id,title) VALUES (" + playlist
					+ ",\"" + title + "\")";
			activity.ld.runQuery("lyricDb", Query);

			// Add actual verses
			String verse = book + ":" + chapter + ":" + startverse + "-"
					+ chapter + ":" + endverse;
			String result = activity.ld.runCommand("bible", "verse_start",
					verse).trim();
			activity.logDebug(result + "=" + verse);
			while (!result.equals(verse)) {
				String tokens[] = result.split("[-:]");
				playorder++;
				Query = "INSERT INTO playlist (playlist,playorder,type,data) VALUES ("
						+ playlist
						+ ", "
						+ playorder
						+ ", \"vers\", \""
						+ tokens[2] + "-" + tokens[4] + "\")";
				String tokens2[] = verse.split("[-:]");
				activity.ld.runQuery("lyricDb", Query);
				verse = tokens2[0] + ":" + tokens2[1] + ":"
						+ (Integer.parseInt(tokens[4]) + 1) + "-" + tokens2[3]
						+ ":" + tokens2[4];
				result = activity.ld.runCommand("bible", "verse_start", verse)
						.trim();
				activity.logDebug(result + "=" + verse);
			}
			String tokens[] = result.split("[-:]");
			playorder++;
			Query = "INSERT INTO playlist (playlist,playorder,type,data) VALUES ("
					+ playlist
					+ ", "
					+ playorder
					+ ", \"vers\", \""
					+ tokens[2] + "-" + tokens[4] + "\")";
			activity.ld.runQuery("lyricDb", Query);
		
			PlaylistFragment frag = (PlaylistFragment) activity.fragments.get("playlist");
			frag.load_playlist();
			return null;
		}
	}

}