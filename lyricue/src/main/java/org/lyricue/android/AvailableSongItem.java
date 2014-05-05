package org.lyricue.android;

public class AvailableSongItem implements Comparable<Object> {
    public String main = "";
    public String small = "";
    public int id = 0;

    @Override
    public String toString() {
        if (!small.equals("")) {
            return main + " " + small;
        } else {
            return main;
        }
    }

    @Override
    public int compareTo(Object another) {
        return this.toString().toUpperCase().compareTo(another.toString().toUpperCase());
    }
}
