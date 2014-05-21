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

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

public class NotificationHandler extends Service {
    @SuppressWarnings("FieldCanBeLocal")
    private final String TAG = "Lyricue";
    private LyricueDisplay ld = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            Log.d(TAG, "command:" + extras.getString("command"));
            Parcelable[] hosts_in = extras.getParcelableArray("hosts");
            HostItem hosts[]= new HostItem[hosts_in.length];
            for (int i=0;i<hosts_in.length;i++){
                hosts[i]=(HostItem) hosts_in[i];
            }
            //noinspection ConstantConditions
            if ((hosts != null) && (!hosts[0].equals(new HostItem("",0)))) {
                if (ld == null) {
                    ld = new LyricueDisplay(hosts);
                }
                ld.runCommand_noreturn("display", extras.getString("command"), "");
            }


        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
