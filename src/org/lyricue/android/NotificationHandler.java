package org.lyricue.android;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

public class NotificationHandler extends Service {

	private LyricueDisplay ld=null;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Bundle extras = intent.getExtras();
		if (extras != null) {
			if (ld == null) {
				ld = new LyricueDisplay(extras.getStringArray("hosts"));
			}
			
			ld.runCommand_noreturn("display", extras.getString("command"), "");
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
