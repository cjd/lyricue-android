package org.lyricue.android;

import android.util.Log;

public class HostItem {
    private static final String TAG = Lyricue.class.getSimpleName();
    public String hostname;
    public int port;


    public HostItem(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public HostItem(String input) {
        Log.d(TAG, "Parsing " + input);
        String values[] = input.split("#", 2);
        if (values.length < 2) {
            values = input.split(":");
            if (values.length > 2) { // Must be IPV6
                values = input.split("]", 2);
                this.hostname = values[0].substring(1);
                this.port = Integer.parseInt(values[1].substring(1));
            } else {
                this.hostname = values[0];
                this.port = Integer.parseInt(values[1]);
            }
        } else {
            this.hostname = values[0];
            this.port = Integer.parseInt(values[1]);
        }
    }

    @Override
    public String toString() {
        return hostname + "#" + port;
    }
}
