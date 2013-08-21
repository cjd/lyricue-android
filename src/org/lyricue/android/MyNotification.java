package org.lyricue.android;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

public class MyNotification extends Notification {
	private String hostip = "";
	private Context context = null;
	private RemoteViews contentView = null;
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public MyNotification(Context ctx, String hostip) {
		super();
		this.context = ctx;
		this.hostip = hostip;
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		contentView = new RemoteViews(context.getPackageName(),
				R.layout.notification);
		setListeners(contentView, ctx);
		Notification noti = new Notification.Builder(context)
				.setContent(contentView).setSmallIcon(R.drawable.ic_launcher)
				.build();
		noti.flags |= Notification.FLAG_ONGOING_EVENT;

		notificationManager.notify(0, noti);
	}
	
	private void setListeners(RemoteViews view, Context ctx) {
		Intent intentPrev = new Intent(ctx, NotificationHandler.class);
		intentPrev.putExtra("command", "prev_page");
		intentPrev.putExtra("hostip", hostip);
		intentPrev.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pIntentPrev = PendingIntent.getService(ctx, 0,
				intentPrev, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.imageButtonNotifyPrev, pIntentPrev);

		Intent intentNext = new Intent(ctx, NotificationHandler.class);
		intentNext.putExtra("command", "next_page");
		intentNext.putExtra("hostip", hostip);
		intentNext.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pIntentNext = PendingIntent.getService(ctx, 1,
				intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.imageButtonNotifyNext, pIntentNext);

		Intent intentBlank = new Intent(ctx, NotificationHandler.class);
		intentBlank.putExtra("command", "blank");
		intentBlank.putExtra("hostip", hostip);
		intentBlank.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pIntentBlank = PendingIntent.getService(ctx, 2,
				intentBlank, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.imageButtonNotifyBlank, pIntentBlank);

		Intent intentReshow = new Intent(ctx, NotificationHandler.class);
		intentReshow.putExtra("command", "reshow");
		intentReshow.putExtra("hostip", hostip);
		intentReshow.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pIntentReshow = PendingIntent.getService(ctx, 3,
				intentBlank, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.imageButtonNotifyReshow,
				pIntentReshow);
	}

}
