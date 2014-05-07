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
