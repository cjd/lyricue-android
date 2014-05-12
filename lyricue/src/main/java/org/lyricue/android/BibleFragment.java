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

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class BibleFragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();
    private Lyricue activity = null;
    private View v = null;
    @SuppressWarnings("CanBeFinal")
    private ArrayList<String> prevArray = new ArrayList<String>();

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = (Lyricue) getActivity();
        v = inflater.inflate(R.layout.bible, container, false);
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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
        // Setup previous verses spinner
        spin = (Spinner) v.findViewById(R.id.spinBiblePrevious);
        ArrayAdapter<String> prevAdapter = new ArrayAdapter<String>(activity,
                android.R.layout.simple_spinner_item, prevArray);
        spin.setAdapter(prevAdapter);
        spin.setOnItemSelectedListener(new BookOnItemSelectedListener());

        new LoadBibleTask().execute(activity);
    }

    void show_verse(String verse, String command) {
        Log.i(TAG, "Showing " + verse);
        new ShowVerseTask().execute(command + "#" + verse);
    }

    void select_verse(String verse) {
        Log.i(TAG, "Select Verse " + verse);
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
        new SelectBibleTask().execute(bible);
    }

    void select_book() {
        Log.i(TAG, "select_book");
        Spinner spin = (Spinner) v.findViewById(R.id.spinBibleBook);
        String bookname = spin.getSelectedItem().toString();
        new SelectBookTask().execute(bookname);
    }

    void select_chapter() {
        Log.i(TAG, "select_chapter");
        Spinner spin = (Spinner) v.findViewById(R.id.spinBibleChapter);
        int chapter = Integer.parseInt(spin.getSelectedItem().toString());
        spin = (Spinner) v.findViewById(R.id.spinBibleBook);
        String bookname = spin.getSelectedItem().toString();
        new SelectChapterTask().execute(bookname + ";" + chapter);
    }

    private class LoadBibleTask extends
            AsyncTask<Context, Void, ArrayList<String>> {
        private Exception exception;
        private int selected;

        @Override
        protected ArrayList<String> doInBackground(Context... arg0) {
            try {
                String status = activity.ld.runCommand(0, "status", "", "");
                ArrayList<String> spinArray = new ArrayList<String>();
                if (!status.equals("")) {

                    String biblename = status
                            .substring(status.indexOf(",T:") + 3);
                    biblename = biblename.substring(0,
                            biblename.lastIndexOf(","));

                    // Find Bibles
                    String bibles = activity.ld.runCommand(0, "bible",
                            "available", "");
                    JSONObject json = new JSONObject(bibles);
                    JSONArray jArray = json.getJSONArray("results");
                    activity.bibles_text = new String[jArray.length()];
                    activity.bibles_id = new String[jArray.length()];
                    activity.bibles_type = new String[jArray.length()];
                    selected = 0;

                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject results = jArray.getJSONObject(i);
                        activity.bibles_id[i] = results.getString("name");
                        activity.bibles_type[i] = results.getString("type");
                        if (activity.bibles_type[i].equals("db")) {
                            activity.bibles_id[i] += "@bibleDb";
                        }
                        activity.bibles_text[i] = results
                                .getString("description");
                        if (biblename.equals(results.getString("name"))) {
                            selected = i;
                        }
                        spinArray.add(activity.bibles_text[i]);
                    }
                    return spinArray;
                } else {
                    activity.bibles_text = new String[1];
                    activity.bibles_id = new String[1];
                    activity.bibles_type = new String[1];
                    activity.bibles_text[0] = "Empty Bible";
                    activity.bibles_id[0] = "";
                    activity.bibles_type[0] = "";
                    spinArray.add("Empty Bible");
                    return spinArray;
                }
            } catch (JSONException e) {
                this.exception = e;

                return null;
            }
        }

        protected void onPostExecute(ArrayList<String> spinArray) {

            if (this.exception != null) {
                activity.logError("Error parsing data "
                        + this.exception.toString());

            } else {
                if (spinArray != null) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            activity, android.R.layout.simple_spinner_item,
                            spinArray);
                    Spinner spinBible = (Spinner) v
                            .findViewById(R.id.spinBibleVersion);
                    spinBible.setAdapter(adapter);
                    spinBible.setSelection(selected);
                    spinBible
                            .setOnItemSelectedListener(new BookOnItemSelectedListener());
                    Spinner book = (Spinner) v.findViewById(R.id.spinBibleBook);
                    book.setClickable(true);
                    book.setSelection(0);
                    select_book();
                }
            }
        }
    }

    private class BibleOnClickListener implements OnClickListener {
        @Override
        public void onClick(View vi) {
            Log.i(TAG, "onClickBible");
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
                    if (activity.hosts != null) {
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
                    if (activity.hosts != null) {
                        show_verse(verse, "verse_start");
                    }
                    break;
                case R.id.buttonBibleAdd:
                    Log.i(TAG, "Adding " + bookname + " " + chapter + ":"
                            + startverse + "-" + endverse);
                    if (activity.hosts != null) {
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
                    if (activity.hosts != null) {
                        show_verse(verse, "verse_start");
                    }
                    break;
            }

        }
    }

    private class BookOnItemSelectedListener implements OnItemSelectedListener {
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

    private class ShowVerseTask extends AsyncTask<String, Void, String> {
        private String verse_passed;

        protected String doInBackground(String... verse) {
            String[] tokens = verse[0].split("#", 2);
            verse_passed = verse[0];
            return activity.ld.runCommand(0, "bible", tokens[0],
                    tokens[1]).trim();
        }

        protected void onPostExecute(String shown) {
            if (!shown.equals("")) {
                Log.i(TAG, "Ret " + shown);
                if (!shown.equals(verse_passed)) {
                    String[] tokens = shown.split("[-:]");
                    String startverse = tokens[2];
                    String endverse = tokens[4];
                    Spinner spin = (Spinner) v
                            .findViewById(R.id.spinBibleVerseStart);
                    spin.setSelection(Integer.parseInt(startverse) - 1);
                    spin = (Spinner) v.findViewById(R.id.spinBibleVerseEnd);
                    spin.setSelection(Integer.parseInt(endverse) - 1);
                }
            }
        }
    }

    private class SelectBibleTask extends AsyncTask<Integer, Void, Integer> {
        protected Integer doInBackground(Integer... bible) {
            activity.ld.runCommand(0, "change_to_db",
                    activity.bibles_id[bible[0]],
                    activity.bibles_type[bible[0]]);
            return 1;
        }

        protected void onPostExecute(Integer ret) {
            Spinner book = (Spinner) v.findViewById(R.id.spinBibleBook);
            book.setClickable(true);
            book.setSelection(0);
            select_book();
        }
    }

    private class SelectBookTask extends AsyncTask<String, Void, Integer> {
        protected Integer doInBackground(String... books) {
            String ret = activity.ld.runCommand(0, "bible", "maxchapter",
                    books[0]).trim();
            if (ret.length() == 0) {
                return 1;
            } else {
                return Integer.parseInt(ret);
            }
        }

        protected void onPostExecute(Integer maxchap) {
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
    }

    private class SelectChapterTask extends AsyncTask<String, Void, Integer> {
        protected Integer doInBackground(String... lookup) {
            String ret = activity.ld.runCommand(0, "bible", "maxverse",
                    lookup[0]).trim();
            if (ret.length() == 0) {
                return 1;
            } else {
                return Integer.parseInt(ret);
            }
        }

        protected void onPostExecute(Integer maxverse) {
            int start_verse = 1;
            int end_verse = 1;
            Spinner spinS = (Spinner) v.findViewById(R.id.spinBibleVerseStart);
            if (spinS.getSelectedItem() != null) {
                start_verse = Integer.parseInt(spinS.getSelectedItem()
                        .toString());
                if (start_verse >= maxverse) {
                    start_verse = maxverse;
                }
            }
            Spinner spinE = (Spinner) v.findViewById(R.id.spinBibleVerseEnd);
            if (spinE.getSelectedItem() != null) {
                end_verse = Integer
                        .parseInt(spinE.getSelectedItem().toString());
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
            String result = activity.ld.runCommand(0, "bible", "verse_start",
                    verse).trim();
            Log.i(TAG, result + "=" + verse);
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
                result = activity.ld.runCommand(0, "bible", "verse_start",
                        verse).trim();
                Log.i(TAG, result + "=" + verse);
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

            PlaylistFragment frag = (PlaylistFragment) activity.fragments.get(PlaylistFragment.class.getName());
            frag.refresh();
            return null;
        }
    }

}