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

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;

import java.lang.ref.WeakReference;

final class PlaylistAdapter extends ArrayAdapter<PlaylistItem> {
    private final String TAG = this.getClass().getSimpleName();
    private final Lyricue activity;

    @SuppressWarnings("SameParameterValue")
    public PlaylistAdapter(final Activity activity, final PlaylistFragment fragment, final int textViewResourceId) {
        super(activity, textViewResourceId);
        Log.i(TAG, "new playlist adapter");
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
            new ImageDownloaderTask(viewHolder.image).execute(getItem(position).id);
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

    private class ImageDownloaderTask extends AsyncTask<Long, Void, Bitmap> {
        private final WeakReference imageViewReference;

        public ImageDownloaderTask(ImageView imageView) {
            imageViewReference = new WeakReference(imageView);
        }


        @Override
        protected Bitmap doInBackground(Long... params) {
            // params comes from the execute() call: params[0] is the url.
            return downloadBitmap(params[0]);
        }

        @Override
        // Once the image is downloaded, associates it to the imageView
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (imageViewReference != null) {
                ImageView imageView = (ImageView) imageViewReference.get();
                if (imageView != null) {

                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    } else {
                        imageView.setVisibility(View.GONE);
                    }
                }

            }
        }

    }

    static Bitmap downloadBitmap(Long playorder) {

        String Query2 = "SELECT HEX(snapshot) FROM playlist WHERE playorder="
                + playorder;
        JSONArray pArray = activity.ld.runQuery("lyricDb",
                Query2);
        if (pArray != null
                && pArray.length() > 0
                && pArray.getJSONObject(0).getString(
                "HEX(snapshot)") != null) {
            byte[] imageBytes = hexStringToByteArray(pArray
                    .getJSONObject(0)
                    .getString("HEX(snapshot)"));
            thumbnail = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            if (thumbnail != null) {
                int height = (thumbnail.getHeight() * activity.thumbnail_width)
                        / thumbnail.getWidth();
                thumbnail = Bitmap.createScaledBitmap(thumbnail, activity.thumbnail_width, height, false);
            }
            return thumbnail;
        }
        return null;
    }
}