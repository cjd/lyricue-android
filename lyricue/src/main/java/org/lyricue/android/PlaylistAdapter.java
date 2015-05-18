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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

final class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> implements View.OnClickListener{
    private final String TAG = "Lyricue";
    private Context mContext;
    private ArrayList<PlaylistItem> items;
    private Lyricue mActivity;
    private PlaylistFragment mFragment;

    @SuppressWarnings("SameParameterValue")
    public PlaylistAdapter(final Lyricue activity, final PlaylistFragment fragment, Context context) {
        Log.i(TAG, "new playlist adapter");
        this.mContext = context;
        this.mActivity = activity;
        this.mFragment = fragment;
        this.items = new ArrayList<PlaylistItem>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.playlist_item, viewGroup, false);
        ViewHolder holder = new ViewHolder(v);
        v.setOnClickListener(PlaylistAdapter.this);
        v.setTag(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        PlaylistItem item = items.get(i);
        viewHolder.lyrics.setText(item.title);
        Log.i(TAG, "Get view:" + i + "=" + item.title);
        if (item.thumbnail != null) {
            Log.d(TAG,"Download Item");
            viewHolder.image.setImageBitmap(item.thumbnail);
            viewHolder.image.setScaleType(ImageView.ScaleType.FIT_START);
            viewHolder.image.setVisibility(View.VISIBLE);
        } else {
            if (mActivity.ld != null) {
                new ImageDownloaderTask(viewHolder.image).execute(item);
            }
            viewHolder.image.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        int itemPosition = holder.getPosition();
        Log.i(TAG, "Click playlist:" + holder.lyrics.getText()+ "=" + itemPosition);
        PlaylistItem item = items.get(itemPosition);
        if (item.type.equals("unloaded")) {
            mFragment.load_playlists();
        } else {
            if (item.type.equals("play") || item.type.equals("sub")) {
                Log.i(TAG,
                        "Load playlist:"
                                + String.valueOf(item.data)
                );
                mFragment.load_playlist(item.data, item.title);
            } else {
                mActivity.ld.runCommand_noreturn("display",
                        String.valueOf(item.id), "");
            }
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView lyrics;
        public ImageView image;

        public ViewHolder(View itemView){
            super(itemView);
            lyrics=(TextView)itemView.findViewById(R.id.playlist_item_description);
            image=(ImageView)itemView.findViewById(R.id.playlist_item_image);
        }
    }

    public void add(Long itemId, String text, String type, Long data) {
        PlaylistItem item = new PlaylistItem(itemId, text, type, data);
        items.add(item);
    }

    private class ImageDownloaderTask extends AsyncTask<PlaylistItem, Void, Bitmap> {
        private final WeakReference imageViewReference;
        private PlaylistItem item;

        public ImageDownloaderTask(ImageView imageView) {
            imageViewReference = new WeakReference(imageView);
        }


        @Override
        protected Bitmap doInBackground(PlaylistItem... params) {
            // params comes from the execute() call: params[0] is the url.
            item=params[0];
            Log.d(TAG,mActivity.ld.toString());
            return downloadBitmap(mActivity.ld, mActivity.thumbnail_width, item.id);
        }

        @Override
        // Once the image is downloaded, associates it to the imageView
        protected void onPostExecute(Bitmap bitmap) {
            Log.d(TAG,"Download Item done");
            if (isCancelled()) {
                bitmap = null;
            }

            //noinspection ConstantConditions
            if (imageViewReference != null) {
                ImageView imageView = (ImageView) imageViewReference.get();
                Log.d(TAG,"Valid view");
                if (imageView != null) {
                    if (bitmap != null) {
                        Log.d(TAG,"Valid Image");
                        imageView.setImageBitmap(bitmap);
                        item.thumbnail = bitmap;
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