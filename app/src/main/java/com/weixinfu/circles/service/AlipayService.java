package com.weixinfu.circles.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.weixinfu.circles.R;

/**
 * Created by jezhee on 7/19/15.
 */
public class AlipayService extends Service {

    private static boolean start = false;

    private static final int NOTIFICATION_ID = 1017;

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundCompat();
    }

    @Override
    public void onDestroy() {
        // 退出前台服务，同时清掉状态栏通知。
        // 在api 18的版本上，这时候状态栏通知没了，但InnerService还在，且仍旧是前台服务状态，目的达到。
        stopForeground(true);

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static boolean trigger(Context context) {
        context = context.getApplicationContext();
        if (!start) {
            context.startService(new Intent(context, AlipayService.class));
        } else {
            if (Build.VERSION.SDK_INT < 18) {
                context.stopService(new Intent(context, AlipayService.class));
            } else {
                context.stopService(new Intent(context, InnerService.class));
            }
        }
        start = !start;
        return start;
    }

    private void startForegroundCompat() {
        if (Build.VERSION.SDK_INT < 18) {
            // api 18（4.3）以下，随便玩
            startForeground(NOTIFICATION_ID, new Notification());
        } else {
            // api 18的时候，google管严了，得绕着玩
            // 先把自己做成一个前台服务，提供合法的参数
            startForeground(NOTIFICATION_ID, fadeNotification(this));

            // 再起一个服务，也是前台的
            startService(new Intent(this, InnerService.class));
        }
    }

    public static class InnerService extends Service {

        @Override
        public void onCreate() {
            super.onCreate();
            // 先把自己也搞成前台的，提供合法参数
            startForeground(NOTIFICATION_ID, fadeNotification(this));

            // 关键步骤来了：自行推掉，或者把AlipayService退掉。
            // duang！系统sb了，说好的人与人的信任呢？
            stopSelf();
        }

        @Override
        public void onDestroy() {
            stopForeground(true);
            super.onDestroy();
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }

    private static Notification fadeNotification(Context context) {
        Notification notification = new Notification();
        // 随便给一个icon，反正不会显示，只是假装自己是合法的Notification而已
        notification.icon = R.drawable.abc_ab_share_pack_mtrl_alpha;
        notification.contentView = new RemoteViews(context.getPackageName(), R.layout.notification_view);
        return notification;
    }
}
