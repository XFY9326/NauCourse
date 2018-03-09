package tool.xfy9326.naucourse.Receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.AlarmManagerCompat;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.NextClassMethod;
import tool.xfy9326.naucourse.Methods.NotificationMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Views.NextClassWidget;

/**
 * Created by 10696 on 2018/3/8.
 */

public class UpdateReceiver extends BroadcastReceiver {
    public static final String UPDATE_ACTION = "tool.xfy9326.naucourse.Receivers.UpdateReceiver.Update";
    private static final int REQUEST_ON_UPDATE = 1;
    boolean isClassBeforeNotify = false;
    private String[] nextData;

    private static void setNextAlarm(Context context, long time) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getLong(Config.PREFERENCE_LAST_NOTIFY_TIME, 0) != time) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_ON_UPDATE, new Intent(context, UpdateReceiver.class).setAction(UPDATE_ACTION), PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager, AlarmManager.RTC_WAKEUP, time, pendingIntent);
                sharedPreferences.edit().putLong(Config.PREFERENCE_LAST_NOTIFY_TIME, time).apply();
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null) {
            if (intent.getAction().equals(UPDATE_ACTION)) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                if (!intent.getBooleanExtra(Config.INTENT_IS_ONLY_INIT, false)) {
                    nextData = NextClassMethod.getNextClassArray(context);

                    if (sharedPreferences.getBoolean(Config.PREFERENCE_CLASS_BEFORE_NOTIFY, false) && sharedPreferences.getBoolean(Config.PREFERENCE_NOTIFY_NEXT_CLASS, Config.DEFAULT_PREFERENCE_NOTIFY_NEXT_CLASS)) {
                        NotificationMethod.showNextClassNotification(context, nextData);
                    }

                    context.sendBroadcast(new Intent(context, NextClassWidget.class).setAction(NextClassWidget.ACTION_ON_CLICK).putExtra(Config.INTENT_NEXT_CLASS_DATA, nextData));
                }
                setNextAlarm(context, nextUpdateTimeCount(context));

                sharedPreferences.edit().putBoolean(Config.PREFERENCE_CLASS_BEFORE_NOTIFY, isClassBeforeNotify).apply();
            }
        }
    }

    private long nextUpdateTimeCount(Context context) {
        long time;
        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        calendar.setTime(new Date());
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        String[] startTime = context.getResources().getStringArray(R.array.course_start_time);
        long nowTime = calendar.getTimeInMillis();

        String[] time_end = startTime[startTime.length - 1].split(":");
        calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time_end[0]));
        calendar.set(Calendar.MINUTE, Integer.valueOf(time_end[1]));
        boolean todayCourseFinish = nowTime >= calendar.getTimeInMillis();

        if (todayCourseFinish || nextData[0] == null) {
            String[] time_temp = startTime[0].split(":");
            calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time_temp[0]));
            calendar.set(Calendar.MINUTE, Integer.valueOf(time_temp[1]));
            calendar.add(Calendar.DATE, 1);
            calendar.add(Calendar.MINUTE, -30);
            time = calendar.getTimeInMillis();
            isClassBeforeNotify = false;
        } else {
            String[] time_temp = nextData[3].split("~")[0].split(":");
            calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time_temp[0]));
            calendar.set(Calendar.MINUTE, Integer.valueOf(time_temp[1]));
            calendar.add(Calendar.MINUTE, -10);

            if (nowTime >= calendar.getTimeInMillis()) {
                time_temp = nextData[3].split("~")[1].split(":");
                calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(time_temp[0]));
                calendar.set(Calendar.MINUTE, Integer.valueOf(time_temp[1]) + 1);
                time = calendar.getTimeInMillis();
                isClassBeforeNotify = false;
            } else {
                time = calendar.getTimeInMillis();
                isClassBeforeNotify = true;
            }
        }
        return time;
    }

}
