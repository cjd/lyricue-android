package org.lyricue.android;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Set;

import pl.polidea.treeview.AbstractTreeViewAdapter;
import pl.polidea.treeview.TreeNodeInfo;
import pl.polidea.treeview.TreeStateManager;

final class PlaylistAdapter extends AbstractTreeViewAdapter<Long> {
	private final Lyricue activity;
	private final PlaylistFragment fragment;

	public PlaylistAdapter(final Activity activity,
			final PlaylistFragment fragment, final Set<Long> selected,
			final TreeStateManager<Long> treeStateManager,
			final int numberOfLevels) {
		super(activity, treeStateManager, numberOfLevels);
		this.fragment = fragment;
		this.activity = (Lyricue) activity;
	}

	@Override
	public LinearLayout updateView(final View view,
			final TreeNodeInfo<Long> treeNodeInfo) {
		final LinearLayout viewLayout = (LinearLayout) view;
		final TextView descriptionView = (TextView) viewLayout
				.findViewById(R.id.playlist_item_description);
		final ImageView imageView = (ImageView) viewLayout
				.findViewById(R.id.playlist_item_image);
		descriptionView.setText(getDescription(treeNodeInfo.getId()));
		descriptionView.setTextSize(20 - 2 * treeNodeInfo.getLevel());
		
		if (activity.imageplaylist && (getImage(treeNodeInfo.getId()) != null)) {
			imageView.setImageBitmap(getImage(treeNodeInfo.getId()));
			imageView.setScaleType(ImageView.ScaleType.FIT_START);
			imageView.setVisibility(View.VISIBLE);
		} else {
			imageView.setVisibility(View.GONE);
		}
		return viewLayout;
	}

	private String getDescription(final long id) {
		return fragment.playlistmap.get(id);
	}

	private Bitmap getImage(final long id) {
		Bitmap b = fragment.imagemap.get(id);
		if (b != null) {
			int height = (b.getHeight()*activity.thumbnail_width)/b.getWidth();
			return Bitmap.createScaledBitmap(fragment.imagemap.get(id), activity.thumbnail_width,height, false);
		}
		return null;
	}

	@Override
	public View getNewChildView(final TreeNodeInfo<Long> treeNodeInfo) {
		final LinearLayout viewLayout = (LinearLayout) getActivity()
				.getLayoutInflater().inflate(R.layout.playlist_item, null);
		return updateView(viewLayout, treeNodeInfo);
	}

	@Override
	public void handleItemClick(final View view, final Object id) {
		activity.logDebug(id.toString());
		activity.ld.runCommand_noreturn("display", id.toString(), "");
	}

	@Override
	public long getItemId(final int position) {
		return getTreeId(position);
	}
}