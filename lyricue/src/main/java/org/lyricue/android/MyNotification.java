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


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

public class MyNotification extends Notification {
    private HostItem[] hosts = null;

    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public MyNotification(Context ctx, HostItem[] hosts_in) {
        super();
        hosts=hosts_in;
        NotificationManager notificationManager = (NotificationManager) ctx
                .getSystemService(Context.NOTIFICATION_SERVICE);

        RemoteViews contentView = new RemoteViews(ctx.getPackageName(),
                R.layout.notification);
        setListeners(contentView, ctx);

        Intent activityIntent = new Intent(ctx, Lyricue.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Notification.Builder build = new Notification.Builder(ctx);
        build.setContent(contentView);
        build.setSmallIcon(R.drawable.ic_stat_name);
        build.setContentIntent(PendingIntent.getActivity(ctx, 0, activityIntent, PendingIntent.FLAG_CANCEL_CURRENT));
        Notification noti = build.getNotification();
        notificationManager.notify(0, noti);
    }

    private void setListeners(RemoteViews view, Context ctx) {
        Intent intentPrev = new Intent(ctx, NotificationHandler.class);
        intentPrev.putExtra("command", "prev_page");
        intentPrev.putExtra("hosts", hosts);
        intentPrev.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntentPrev = PendingIntent.getService(ctx, 0,
                intentPrev, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.imageButtonNotifyPrev, pIntentPrev);

        Intent intentNext = new Intent(ctx, NotificationHandler.class);
        intentNext.putExtra("command", "next_page");
        intentNext.putExtra("hosts", hosts);
        intentNext.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntentNext = PendingIntent.getService(ctx, 1,
                intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.imageButtonNotifyNext, pIntentNext);

        Intent intentBlank = new Intent(ctx, NotificationHandler.class);
        intentBlank.putExtra("command", "blank");
        intentBlank.putExtra("hosts", hosts);
        intentBlank.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntentBlank = PendingIntent.getService(ctx, 2,
                intentBlank, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.imageButtonNotifyBlank, pIntentBlank);

        Intent intentReshow = new Intent(ctx, NotificationHandler.class);
        intentReshow.putExtra("command", "reshow");
        intentReshow.putExtra("hosts", hosts);
        intentReshow.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntentReshow = PendingIntent.getService(ctx, 3,
                intentReshow, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.imageButtonNotifyReshow,
                pIntentReshow);
    }

}
