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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class AvailableSongsAdapter extends ArrayAdapter<AvailableSongItem>
        implements SectionIndexer, Filterable {
    private final Context context;
    private final Object mLock = new Object();
    private ArrayList<AvailableSongItem> items;
    private ArrayList<AvailableSongItem> originalitems;
    private ArrayFilter mFilter;
    private HashMap<String, Integer> alphaIndexer;
    private String[] sections;

    public AvailableSongsAdapter(Context context,
                                 ArrayList<AvailableSongItem> items) {
        super(context, R.layout.available_song_row, items);
        this.context = context;
        this.items = items;
        refresh_songlist_index();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.available_song_row, parent, false);
            viewHolder = new ViewHolderItem();
            viewHolder.mainText = (TextView) convertView.findViewById(R.id.textMain);
            viewHolder.smallText = (TextView) convertView.findViewById(R.id.textSmall);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }
        if (position < items.size()) {
            viewHolder.mainText.setText(items.get(position).main);
            viewHolder.smallText.setText(items.get(position).small);
        }
        return convertView;
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

    static class ViewHolderItem {
        TextView mainText;
        TextView smallText;
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

                final ArrayList<AvailableSongItem> newValues = new ArrayList<AvailableSongItem>();

                for (final AvailableSongItem value : values) {
                    final String valueText = value.toString().toLowerCase();

                    // First match against the whole, non-split value
                    if (valueText.startsWith(prefixString)) {
                        newValues.add(value);
                    } else {
                        final String[] words = valueText.split(" ");

                        // Start at index 0, in case valueText starts with
                        // space(s)
                        for (String word : words) {
                            if (word.startsWith(prefixString)) {
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
                for (AvailableSongItem item : items) {
                    add(item);
                }
                refresh_songlist_index();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
