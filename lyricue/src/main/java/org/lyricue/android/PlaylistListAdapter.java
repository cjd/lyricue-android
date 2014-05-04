package org.lyricue.android;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

final class PlaylistAdapter extends ArrayAdapter<PlaylistItem> {
    private static final String TAG = Lyricue.class.getSimpleName();
    private final Lyricue activity;
    private final PlaylistFragment fragment;

    @SuppressWarnings("SameParameterValue")
    public PlaylistAdapter(final Activity activity, final PlaylistFragment fragment, final int textViewResourceId) {
        super(activity, textViewResourceId);
        Log.i(TAG, "new playlist adapter");
        this.fragment = fragment;
        this.activity = (Lyricue) activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.playlist_item, parent, false);
            viewHolder = new ViewHolderItem();
            viewHolder.description = (TextView) convertView.findViewById(R.id.playlist_item_description);
            viewHolder.image = (ImageView) convertView.findViewById(R.id.playlist_item_image);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

        viewHolder.description.setText(this.getItem(position).title);
        Log.i(TAG, "Get view:" + position + "=" + this.getItem(position).title);
        if (activity.imageplaylist && (getItem(position).thumbnail != null)) {
            viewHolder.image.setImageBitmap(getItem(position).thumbnail);
            viewHolder.image.setScaleType(ImageView.ScaleType.FIT_START);
            viewHolder.image.setVisibility(View.VISIBLE);
        } else {
            viewHolder.image.setVisibility(View.GONE);
        }
        return convertView;
    }

    public void add(Long itemId, String text, String type, Long data, Bitmap thumbnail) {
        PlaylistItem item = new PlaylistItem(itemId, text, type, data, thumbnail);
        super.add(item);
    }

    static class ViewHolderItem {
        TextView description;
        ImageView image;
    }
}