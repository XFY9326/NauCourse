package tool.xfy9326.naucourse.Methods;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import tool.xfy9326.naucourse.Activities.MainActivity;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.NextCourse;

/**
 * Created by 10696 on 2018/3/9.
 * 显示上课信息的通知
 */

public class NotificationMethod {
    private static final int ACTIVITY_REQUEST_CODE = 0;
    private static final int NOTIFICATION_CODE = 1;
    private static final String CHANNEL_ID = "channel_next_course_notify";

    /**
     * 显示下一节课的通知
     *
     * @param context    Context
     * @param nextCourse NextCourse对象
     */
    public static void showNextClassNotification(Context context, NextCourse nextCourse) {
        if (nextCourse.getCourseId() != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String lastNotifyId = sharedPreferences.getString(Config.PREFERENCE_LAST_NOTIFY_ID, null);
            if (lastNotifyId == null || !lastNotifyId.equals(nextCourse.getCourseId())) {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);

                    CreateNotificationChannel(context, notificationManager);

                    builder.setSmallIcon(R.mipmap.ic_launcher_foreground);
                    builder.setContentTitle(nextCourse.getCourseName());
                    builder.setContentText(nextCourse.getCourseTeacher() + "  " + nextCourse.getCourseLocation() + "  " + nextCourse.getCourseTime());
                    builder.setAutoCancel(true);

                    PendingIntent pendingIntent = PendingIntent.getActivity(context, ACTIVITY_REQUEST_CODE, new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), PendingIntent.FLAG_UPDATE_CURRENT);
                    builder.setContentIntent(pendingIntent);

                    notificationManager.notify(NOTIFICATION_CODE, builder.build());
                    sharedPreferences.edit().putString(Config.PREFERENCE_LAST_NOTIFY_ID, nextCourse.getCourseId()).apply();
                }
            }
        }
    }

    /**
     * 设置通知渠道
     *
     * @param context             Context
     * @param notificationManager NotificationManager
     */
    private static void CreateNotificationChannel(Context context, NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(CHANNEL_ID);
            if (notificationChannel == null) {
                notificationChannel = new NotificationChannel(CHANNEL_ID, context.getString(R.string.notification_channel), NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.setDescription(context.getString(R.string.notification_channel_des));
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                notificationChannel.setShowBadge(true);
                notificationChannel.enableLights(true);
                notificationChannel.enableVibration(true);
                notificationChannel.setShowBadge(false);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }
}
