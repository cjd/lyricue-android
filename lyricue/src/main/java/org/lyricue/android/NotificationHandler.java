package org.lyricue.android;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

public class NotificationHandler extends Service {
    private static final String TAG = Lyricue.class.getSimpleName();
    private LyricueDisplay ld = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
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
