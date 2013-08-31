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

	public PlaylistAdapter(final Activity activity,	final PlaylistFragment fragment, final int textViewResourceId) {
		super(activity, textViewResourceId);
		Log.i(TAG, "new playlist adapter");
		this.fragment = fragment;
		this.activity = (Lyricue) activity;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) activity
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.playlist_item, parent, false);
		final TextView descriptionView = (TextView) rowView
				.findViewById(R.id.playlist_item_description);
		final ImageView imageView = (ImageView) rowView
				.findViewById(R.id.playlist_item_image);
		descriptionView.setText(this.getItem(position).title);
		Log.i(TAG,"Getview:"+position+"="+this.getItem(position).title);
		if (activity.imageplaylist && (getImage(getItem(position).id) != null)) {
			imageView.setImageBitmap(getImage(getItem(position).id));
			imageView.setScaleType(ImageView.ScaleType.FIT_START);
			imageView.setVisibility(View.VISIBLE);
		} else {
			imageView.setVisibility(View.GONE);
		}
		return rowView;
	}

	private Bitmap getImage(final long id) {
		Bitmap b = fragment.imagemap.get(id);
		if (b != null) {
			int height = (b.getHeight() * activity.thumbnail_width)
					/ b.getWidth();
			return Bitmap.createScaledBitmap(fragment.imagemap.get(id),
					activity.thumbnail_width, height, false);
		}
		return null;
	}
	
	public void add(Long itemId, String text, String type, Long data) {
		PlaylistItem item = new PlaylistItem(itemId, text, type, data);
		super.add(item);
	}
}