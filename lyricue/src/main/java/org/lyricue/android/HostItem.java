package org.lyricue.android;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class HostItem implements Parcelable {
    public static final Creator<HostItem> CREATOR =
            new Creator<HostItem>() {
                public HostItem createFromParcel(Parcel in) {
                    return new HostItem(in);
                }

                public HostItem[] newArray(int size) {
                    return new HostItem[size];
                }
            };
    private final String TAG = "Lyricue";
    public String hostname;
    public int port;

    public HostItem(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public HostItem(Parcel in) {
        readFromParcel(in);
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.hostname);
        parcel.writeInt(this.port);
    }

    private void readFromParcel(Parcel in) {
        hostname = in.readString();
        port = in.readInt();
    }
}
