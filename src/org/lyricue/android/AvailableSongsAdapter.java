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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class AvailableSongsAdapter extends ArrayAdapter<AvailableSongItem>
		implements SectionIndexer, Filterable {
	private final Context context;
	private ArrayList<AvailableSongItem> items;
	private ArrayList<AvailableSongItem> originalitems;
	private ArrayFilter mFilter;
	HashMap<String, Integer> alphaIndexer;
	private final Object mLock = new Object();
	String[] sections;

	public AvailableSongsAdapter(Context context,
			ArrayList<AvailableSongItem> items) {
		super(context, R.layout.available_song_row, items);
		this.context = context;
		this.items = items;
		refresh_songlist_index();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.available_song_row, parent,
				false);
		TextView mainView = (TextView) rowView.findViewById(R.id.textMain);
		TextView smallView = (TextView) rowView.findViewById(R.id.textSmall);
		if (position < items.size()) {
			mainView.setText(items.get(position).main);
			smallView.setText(items.get(position).small);
		}
		return rowView;
	}

	@Override
	public AvailableSongItem getItem(int position) {
		if (position < items.size()) {
			return items.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		if (position < items.size()) {
			return items.get(position).id;
		}
		return 0;
	}

	@Override
	public int getPositionForSection(int section) {
		if (section < sections.length) {
			String letter = sections[section];
			return alphaIndexer.get(letter);
		}
		return 0;
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

	@Override
	public Filter getFilter() {
		if (mFilter == null) {
			mFilter = new ArrayFilter();
		}
		return mFilter;
	}

	void refresh_songlist_index() {
		if (alphaIndexer == null) alphaIndexer = new HashMap<String, Integer>();
		alphaIndexer.clear();
		int size = items.size();
		for (int i = size - 1; i >= 0; i--) {
			// We store the first letter of the word, and its index.
			// The Hashmap will replace the value for identical keys are put in
			alphaIndexer
					.put(items.get(i).main.substring(0, 1).toUpperCase(), i);
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

	private class ArrayFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence prefix) {
			FilterResults results = new FilterResults();

			if (originalitems == null) {
				synchronized (mLock) {
					originalitems = new ArrayList<AvailableSongItem>(items);
				}
			}

			if (prefix == null || prefix.length() == 0) {
				ArrayList<AvailableSongItem> list;
				synchronized (mLock) {
					list = new ArrayList<AvailableSongItem>(originalitems);
				}
				results.values = list;
				results.count = list.size();
			} else {
				String prefixString = prefix.toString().toLowerCase();

				ArrayList<AvailableSongItem> values;
				synchronized (mLock) {
					values = new ArrayList<AvailableSongItem>(originalitems);
				}

				final int count = values.size();
				final ArrayList<AvailableSongItem> newValues = new ArrayList<AvailableSongItem>();

				for (int i = 0; i < count; i++) {
					final AvailableSongItem value = values.get(i);
					final String valueText = value.toString().toLowerCase();

					// First match against the whole, non-split value
					if (valueText.startsWith(prefixString)) {
						newValues.add(value);
					} else {
						final String[] words = valueText.split(" ");
						final int wordCount = words.length;

						// Start at index 0, in case valueText starts with
						// space(s)
						for (int k = 0; k < wordCount; k++) {
							if (words[k].startsWith(prefixString)) {
								newValues.add(value);
								break;
							}
						}
					}
				}

				results.values = newValues;
				results.count = newValues.size();
			}
			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			items = (ArrayList<AvailableSongItem>) results.values;
			if (results.count > 0) {
				notifyDataSetChanged();
				clear();
				for (int i = 0; i < items.size(); i++) {
					add(items.get(i));
				}
				refresh_songlist_index();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}
}
