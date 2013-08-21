package org.lyricue.android;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class NotificationHandler extends Service {

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("Notification","The_Extra:start" );

		Bundle extras = intent.getExtras();
		if (extras != null) {
			Log.i("Notification","The_Extra:"+extras.getString("command") );

			//if (ld == null) {
				//Log.i("Notification","new connection:"+extras.getString("hostip"));
				//ld = new LyricueDisplay(hostip);
			//}
			//ld.runCommand_noreturn("display", extras.getString("command"), "");
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i("Notification","onBind()");
		Bundle extras = intent.getExtras();
		if (extras != null) {
			Log.i("Notification","The_Extra2:"+extras.getString("command") );
		}
		return null;
	}

}
