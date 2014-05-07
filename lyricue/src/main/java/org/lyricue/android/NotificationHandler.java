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

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.content.Context;
import android.util.Log;

public class NotificationHandler extends Service {
    private static final String TAG = Lyricue.class.getSimpleName();
    private LyricueDisplay ld = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            Log.d(TAG, "command:" + extras.getString("command"));
            if (extras.getString("command").equals("android_exit")) {
                NotificationManager notificationManager = (NotificationManager) this
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancelAll();
                return super.onStartCommand(intent, flags, startId);
            }
            String[] hosts = extras.getStringArray("hosts");
            if (!hosts[0].equals("")) {
                if (ld == null) {
                    HostItem hostitems[] = new HostItem[hosts.length];
                    for (int i = 0; i < hosts.length; i++) {
                        hostitems[i] = new HostItem(hosts[i]);
                    }
                    ld = new LyricueDisplay(hostitems);
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
