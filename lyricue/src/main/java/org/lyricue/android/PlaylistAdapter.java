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
import org.json.JSONException;

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
        if (getItem(position).thumbnail != null) {
            Log.d(TAG,"Download Item");
            viewHolder.image.setImageBitmap(getItem(position).thumbnail);
            viewHolder.image.setScaleType(ImageView.ScaleType.FIT_START);
            viewHolder.image.setVisibility(View.VISIBLE);
        } else {
            if (activity.ld != null) {
                new ImageDownloaderTask(viewHolder.image).execute(position);
            }
            viewHolder.image.setVisibility(View.GONE);
        }
        return convertView;
    }

    public void add(Long itemId, String text, String type, Long data) {
        PlaylistItem item = new PlaylistItem(itemId, text, type, data);
        super.add(item);
    }

    static class ViewHolderItem {
        TextView description;
        ImageView image;
    }

    private class ImageDownloaderTask extends AsyncTask<Integer, Void, Bitmap> {
        private final WeakReference imageViewReference;
        private int position = 0;

        public ImageDownloaderTask(ImageView imageView) {
            imageViewReference = new WeakReference(imageView);
        }


        @Override
        protected Bitmap doInBackground(Integer... params) {
            // params comes from the execute() call: params[0] is the url.
            position=params[0];
            Log.d(TAG,activity.ld.toString());
            return downloadBitmap(activity.ld, activity.thumbnail_width, getItem(position).id);
        }

        @Override
        // Once the image is downloaded, associates it to the imageView
        protected void onPostExecute(Bitmap bitmap) {
            Log.d(TAG,"Download Item done");
            if (isCancelled()) {
                bitmap = null;
            }

            if (imageViewReference != null) {
                ImageView imageView = (ImageView) imageViewReference.get();
                Log.d(TAG,"Valid view");
                if (imageView != null) {
                    if (bitmap != null) {
                        Log.d(TAG,"Valid Image");
                        imageView.setImageBitmap(bitmap);
                        getItem(position).thumbnail = bitmap;
                        imageView.setVisibility(View.VISIBLE);
                    } else {
                        Log.d(TAG,"No Image");
                        imageView.setVisibility(View.GONE);
                    }
                }

            }
        }

    }

    static Bitmap downloadBitmap(LyricueDisplay ld, int thumbnail_width, Long playorder) {
        String Query2 = "SELECT HEX(snapshot) FROM playlist WHERE playorder="
                + playorder;
        try {

            JSONArray pArray = ld.runQuery("lyricDb",
                    Query2);
            if (pArray != null
                    && pArray.length() > 0
                    && pArray.getJSONObject(0).getString(
                    "HEX(snapshot)") != null) {
                byte[] imageBytes = hexStringToByteArray(pArray
                        .getJSONObject(0)
                        .getString("HEX(snapshot)"));
                if (imageBytes.length > 0) {
                    Bitmap thumbnail = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    if (thumbnail != null) {
                        int height = (thumbnail.getHeight() * thumbnail_width)
                                / thumbnail.getWidth();
                        thumbnail = Bitmap.createScaledBitmap(thumbnail, thumbnail_width, height, false);
                    }
                    return thumbnail;
                }
            }
        }catch (JSONException e) {
            return null;
        }
        return null;
    }

     private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}