package tool.xfy9326.naucourse.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import java.util.Date;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.activities.MainActivity;
import tool.xfy9326.naucourse.methods.NextClassMethod;
import tool.xfy9326.naucourse.methods.TimeMethod;
import tool.xfy9326.naucourse.receivers.CourseUpdateReceiver;
import tool.xfy9326.naucourse.utils.NextCourse;

/**
 * Created by 10696 on 2018/2/27.
 * 显示下一节课的桌面小部件
 */

public class NextClassWidget extends AppWidgetProvider {
    public static final String ACTION_ON_UPDATE = "tool.xfy9326.naucourse.Widget.NextClassWidget.OnUpdate";
    private static final int REQUEST_ON_UPDATE = 1;
    private static final int REQUEST_ON_CLICK_CONTENT = 1;
    @Nullable
    private static NextCourse nextCourse = null;

    @NonNull
    synchronized private static RemoteViews viewGet(Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.app_widget_next_class);

        remoteViews.setTextViewText(R.id.textView_app_widget_dateNow, TimeMethod.formatDateSDF(new Date()));

        PendingIntent updatePendingIntent = PendingIntent.getBroadcast(context, REQUEST_ON_UPDATE, new Intent(context, NextClassWidget.class).setAction(ACTION_ON_UPDATE), PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.button_app_widget_update, updatePendingIntent);

        PendingIntent clickContentPendingIntent = PendingIntent.getActivity(context, REQUEST_ON_CLICK_CONTENT, new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.layout_app_widget, clickContentPendingIntent);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            NextCourse next = nextCourse;
            if (next == null) {
                next = NextClassMethod.getNextClassArray(context);
            }
            if (next.isInVacation()) {
                remoteViews.setTextViewText(R.id.textView_app_widget_noNextClass, context.getString(R.string.in_vacation));
            } else {
                remoteViews.setTextViewText(R.id.textView_app_widget_noNextClass, context.getString(R.string.no_course));
            }
            if (next.getCourseId() != null) {
                remoteViews.setTextViewText(R.id.textView_app_widget_nextClass, next.getCourseName());
                remoteViews.setTextViewText(R.id.textView_app_widget_nextLocation, next.getCourseLocation());
                remoteViews.setTextViewText(R.id.textView_app_widget_nextTeacher, next.getCourseTeacher());
                String time = next.getCourseTime();
                if (time != null) {
                    remoteViews.setTextViewText(R.id.textView_app_widget_nextTime, time.replace("~", "\n~\n").trim());
                }

                remoteViews.setViewVisibility(R.id.textView_app_widget_noNextClass, View.GONE);
                remoteViews.setViewVisibility(R.id.layout_app_widget_nextClass, View.VISIBLE);
            } else {
                remoteViews.setTextViewText(R.id.textView_app_widget_noNextClass, context.getString(R.string.no_course));
                remoteViews.setViewVisibility(R.id.textView_app_widget_noNextClass, View.VISIBLE);
                remoteViews.setViewVisibility(R.id.layout_app_widget_nextClass, View.GONE);
            }
        } else {
            remoteViews.setTextViewText(R.id.textView_app_widget_noNextClass, context.getString(R.string.class_info_empty));
            remoteViews.setViewVisibility(R.id.textView_app_widget_noNextClass, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.layout_app_widget_nextClass, View.GONE);
        }
        return remoteViews;
    }

    @Override
    public void onEnabled(@NonNull Context context) {
        //初始化自动更新
        context.sendBroadcast(new Intent(context, CourseUpdateReceiver.class).setAction(CourseUpdateReceiver.UPDATE_ACTION).setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES).putExtra(Config.INTENT_IS_ONLY_INIT, true));
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(@NonNull Context context, @NonNull AppWidgetManager appWidgetManager, @NonNull int[] appWidgetIds) {
        try {
            for (int appWidgetId : appWidgetIds) {
                appWidgetManager.updateAppWidget(appWidgetId, viewGet(context));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(ACTION_ON_UPDATE)) {
                ComponentName componentName = new ComponentName(context, NextClassWidget.class);
                nextCourse = (NextCourse) intent.getSerializableExtra(Config.INTENT_NEXT_CLASS_DATA);
                AppWidgetManager.getInstance(context).updateAppWidget(componentName, viewGet(context));
            }
        }
        super.onReceive(context, intent);
    }
}
