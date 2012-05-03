package org.lyricue.android;

import java.util.ArrayList;


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
	String bookname = "";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		activity = (Lyricue) getActivity();
		v = (View) inflater.inflate(R.layout.bible, null);
		Spinner spin = (Spinner) v.findViewById(R.id.spinBibleBook);
		spin.setOnItemSelectedListener(new BookOnItemSelectedListener());
		spin = (Spinner) v.findViewById(R.id.spinBibleChapter);
		spin.setOnItemSelectedListener(new BookOnItemSelectedListener());
		spin = (Spinner) v.findViewById(R.id.spinBibleVerseStart);
		spin.setOnItemSelectedListener(new BookOnItemSelectedListener());
		spin = (Spinner) v.findViewById(R.id.spinBibleVerseEnd);
		spin.setOnItemSelectedListener(new BookOnItemSelectedListener());
		Button b = (Button) v.findViewById(R.id.buttonBibleAdd);
		b.setOnClickListener(new BibleOnClickListener());

		return v;
	}

	public class BibleOnClickListener implements OnClickListener {
		@Override
		public void onClick(View vi) {
			System.err.println("onClickBible");
			switch (vi.getId()) {
			case R.id.buttonBibleAdd:
				Spinner spin = (Spinner) v.findViewById(R.id.spinBibleChapter);
				String chapter = spin.getSelectedItem().toString();
				spin = (Spinner) v.findViewById(R.id.spinBibleVerseStart);
				String startverse = spin.getSelectedItem().toString();
				spin = (Spinner) v.findViewById(R.id.spinBibleVerseEnd);
				String endverse = spin.getSelectedItem().toString();
				System.err.println("Adding " + bookname + " " + chapter + ":" + startverse
							+ "-" + endverse);
				new AddVerseTask().execute(bookname, chapter, startverse, endverse);
				break;
			}
		}
	}

	public class BookOnItemSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			switch (parent.getId()) {
			case R.id.spinBibleBook:
				System.err.println("book");
				select_book(parent.getItemAtPosition(pos).toString());
				break;
			case R.id.spinBibleChapter:
				System.err.println("chapter");
				select_chapter(Integer.parseInt(parent.getItemAtPosition(pos)
						.toString()));
				break;
			case R.id.spinBibleVerseStart:
				System.err.println("start");
				break;
			case R.id.spinBibleVerseEnd:
				System.err.println("end");
				break;
			}
			System.err.println(parent.getItemAtPosition(pos).toString());
		}

		public void onNothingSelected(AdapterView<?> parent) {

		}
	}

	void select_book(String book) {
		bookname = book;
		String status = activity.ld.runCommand("status", "", "");
		String biblename = status.substring(status.indexOf(",T:") + 3);
		biblename = biblename.substring(0, biblename.lastIndexOf(","));
		String query = "SELECT MAX(chapternum) AS chapternum FROM " + biblename
				+ " WHERE book LIKE \"" + bookname + "\"";
		int maxchap = activity.ld.runQuery_int("bibledb", query, "chapternum");
		Spinner spinChap = (Spinner) v.findViewById(R.id.spinBibleChapter);
		ArrayList<String> spinArray = new ArrayList<String>();
		for (int i = 1; i <= maxchap; i++) {
			spinArray.add(String.valueOf(i));
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
				android.R.layout.simple_spinner_dropdown_item, spinArray);
		spinChap.setAdapter(adapter);
	}

	void select_chapter(int chapter) {
		String status = activity.ld.runCommand("status", "", "");
		String biblename = status.substring(status.indexOf(",T:") + 3);
		biblename = biblename.substring(0, biblename.lastIndexOf(","));
		String query = "SELECT MAX(versenum) AS versenum FROM " + biblename
				+ " WHERE book LIKE \"" + bookname + "\" AND chapternum=\""
				+ chapter + "\"";
		int maxverse = activity.ld.runQuery_int("bibledb", query, "versenum");

		ArrayList<String> spinArrayS = new ArrayList<String>();
		ArrayList<String> spinArrayE = new ArrayList<String>();
		for (int i = 1; i <= maxverse; i++) {
			spinArrayS.add(String.valueOf(i));
			spinArrayE.add(String.valueOf(i));
		}

		Spinner spinS = (Spinner) v.findViewById(R.id.spinBibleVerseStart);
		ArrayAdapter<String> adapterS = new ArrayAdapter<String>(activity,
				android.R.layout.simple_spinner_dropdown_item, spinArrayS);
		spinS.setAdapter(adapterS);
		spinS.setOnItemSelectedListener(new BookOnItemSelectedListener());

		Spinner spinE = (Spinner) v.findViewById(R.id.spinBibleVerseEnd);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
				android.R.layout.simple_spinner_dropdown_item, spinArrayE);
		spinE.setAdapter(adapter);
		spinE.setOnItemSelectedListener(new BookOnItemSelectedListener());

	}


	private class AddVerseTask extends AsyncTask<String, Void, Void> {
		protected Void doInBackground(String... args) {
			String book = args[0];
			int chapter=Integer.parseInt(args[1]);
			int startverse=Integer.parseInt(args[2]);
			int endverse=Integer.parseInt(args[3]);
			
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
				          + playlist + ", "
				          + playorder
				          + ", \"vers\", \""
				          + i + "-"
				          + i + "\")";
				activity.ld.runQuery("lyricDb", Query);
			}

			String title=book+":"+chapter+":"+startverse+"-"+endverse;
			Query = "INSERT INTO playlists (id,title) VALUES (" + playlist
					+ ",\"" + title + "\")";
			activity.ld.runQuery("lyricDb", Query);
			PlaylistFragment frag = (PlaylistFragment) activity.adapter.getItem(1);
			frag.load_playlist();
			return null;
		}
	}

}