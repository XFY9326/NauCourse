package tool.xfy9326.naucourse.methods.io;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.activities.MainActivity;
import tool.xfy9326.naucourse.beans.course.NextCourse;

/**
 * Created by 10696 on 2018/3/9.
 * 显示上课信息的通知
 */

public class NotificationMethod {
    private static final int ACTIVITY_REQUEST_CODE = 0;
    private static final int NOTIFICATION_CODE_NEXT_COURSE = 100;
    private static final String CHANNEL_ID = "channel_nau_notify";

    /**
     * 显示下一节课的通知
     *
     * @param context    Context
     * @param nextCourse NextCourse对象
     */
    public static void showNextClassNotification(@NonNull Context context, NextCourse nextCourse) {
        if (nextCourse.getCourseId() != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String lastNotifyId = sharedPreferences.getString(Config.PREFERENCE_LAST_NOTIFY_ID, null);
            if (lastNotifyId == null || !lastNotifyId.equals(nextCourse.getCourseId())) {
                showNotification(context, NOTIFICATION_CODE_NEXT_COURSE, nextCourse.getCourseName(), String.format("%s %s %s", nextCourse.getCourseTeacher(), nextCourse.getCourseLocation(), nextCourse.getCourseTime()));
                sharedPreferences.edit().putString(Config.PREFERENCE_LAST_NOTIFY_ID, nextCourse.getCourseId()).apply();
            }
        }
    }

    /**
     * 默认模板显示通知
     *
     * @param context Context
     * @param code    通知Code
     * @param title   通知标题
     * @param text    通知内容
     */
    @SuppressWarnings("SameParameterValue")
    private static void showNotification(Context context, int code, String title, String text) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);

            createNotificationChannel(context, notificationManager);

            builder.setSmallIcon(R.mipmap.ic_launcher_foreground);
            builder.setContentTitle(title);
            builder.setContentText(text);
            builder.setAutoCancel(true);
            builder.setDefaults(NotificationCompat.DEFAULT_ALL);
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
            builder.setCategory(NotificationCompat.CATEGORY_MESSAGE);
            builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                    R.mipmap.ic_launcher));

            PendingIntent pendingIntent = PendingIntent.getActivity(context, ACTIVITY_REQUEST_CODE, new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra(Config.INTENT_VIEW_PAGER_POSITION, Config.VIEWPAGER_TABLE_PAGE), PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);

            notificationManager.notify(code, builder.build());
        }
    }

    /**
     * 设置通知渠道
     *
     * @param context             Context
     * @param notificationManager NotificationManager
     */
    private static void createNotificationChannel(@NonNull Context context, @NonNull NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(CHANNEL_ID);
            if (notificationChannel == null) {
                notificationChannel = new NotificationChannel(CHANNEL_ID, context.getString(R.string.notification_channel), NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.setDescription(context.getString(R.string.notification_channel_des));
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                notificationChannel.setShowBadge(true);
                notificationChannel.enableLights(true);
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }
}
