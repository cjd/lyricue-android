package org.lyricue.android;

import android.graphics.Bitmap;

class PlaylistItem {
    public String title = "";
    public String type = "";
    public Long id = (long) 0;
    public Long data = (long) 0;
    public Bitmap thumbnail = null;

    public PlaylistItem(Long id, String title, String type, Long data, Bitmap thumbnail) {
        this.title = title;
        this.id = id;
        this.type = type;
        this.data = data;
        this.thumbnail = thumbnail;
    }
}
