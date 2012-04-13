package org.lyricue.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class AvailableSongsAdapter extends ArrayAdapter<String> implements
		SectionIndexer {
	private final Context context;
	private String[] main;
	private String[] small;
	private int[] ids;
	ArrayList<String> myElements;
	HashMap<String, Integer> alphaIndexer;
	String[] sections;

	public AvailableSongsAdapter(Context context, String[] main,
			String[] small, int[] ids) {
		super(context, R.layout.available_song_row, main);
		this.context = context;
		this.main = main;
		this.small = small;
		this.ids = ids;

		alphaIndexer = new HashMap<String, Integer>();
		int size = main.length;
		for (int i = size - 1; i >= 0; i--) {
			// We store the first letter of the word, and its index.
			// The Hashmap will replace the value for identical keys are putted
			// in
			alphaIndexer.put(main[i].substring(0, 1), i);
		}

		// now we have an hashmap containing for each first-letter
		// sections(key), the index(value) in where this sections begins

		// we have now to build the sections(letters to be displayed)
		// array .it must contains the keys, and must (I do so...) be
		// ordered alphabetically

		Set<String> keys = alphaIndexer.keySet(); // set of letters ...sets
		// cannot be sorted...

		Iterator<String> it = keys.iterator();
		ArrayList<String> keyList = new ArrayList<String>(); // list can be
																// sorted

		while (it.hasNext()) {
			String key = it.next();
			keyList.add(key);
		}

		Collections.sort(keyList);

		sections = new String[keyList.size()]; // simple conversion to an
		// array of object
		keyList.toArray(sections);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.available_song_row, parent,
				false);
		TextView mainView = (TextView) rowView.findViewById(R.id.textMain);
		TextView smallView = (TextView) rowView.findViewById(R.id.textSmall);
		mainView.setText(main[position]);
		smallView.setText(small[position]);
		return rowView;
	}

	@Override
	public String getItem(int position) {
		if (position < main.length) {
			return main[position];
		}
		return "";
	}

	@Override
	public long getItemId(int position) {
		if (position < ids.length) {
			return ids[position];
		}
		return 0;
	}

	@Override
	public int getPositionForSection(int section) {
		String letter = sections[section];

		return alphaIndexer.get(letter);
	}

	@Override
	public int getSectionForPosition(int position) {
		return 0;
	}

	@Override
	public Object[] getSections() {
		return sections; // to string will be called each object, to display the
							// letter
	}

}
