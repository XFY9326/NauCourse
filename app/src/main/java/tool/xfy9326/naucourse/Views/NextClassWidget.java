package tool.xfy9326.naucourse.Views;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.CourseMethod;
import tool.xfy9326.naucourse.Methods.TimeMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.Course;
import tool.xfy9326.naucourse.Utils.SchoolTime;

/**
 * Created by 10696 on 2018/2/27.
 * 显示下一节课的桌面小部件
 */

public class NextClassWidget extends AppWidgetProvider {
    public static final String ACTION_ON_CLICK = "tool.xfy9326.naucourse.Views.NextClassWidget.OnClick";
    private static final int REQUEST_ON_CLICK = 1;

    synchronized private static RemoteViews ViewGet(Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.app_widget_next_class);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        remoteViews.setTextViewText(R.id.textView_app_widget_dateNow, simpleDateFormat.format(new Date()));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_ON_CLICK, new Intent(ACTION_ON_CLICK), PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.layout_app_widget, pendingIntent);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {

            SchoolTime schoolTime = (SchoolTime) BaseMethod.getOfflineData(context, SchoolTime.class, TimeMethod.FILE_NAME);
            ArrayList<Course> courses = BaseMethod.getOfflineTableData(context);

            int weekNum = BaseMethod.getNowWeekNum(schoolTime);

            if (schoolTime != null && courses != null && weekNum != 0) {
                schoolTime.setWeekNum(weekNum);
                CourseMethod courseMethod = new CourseMethod(context, courses, schoolTime);
                String[] result = courseMethod.getNextClass(weekNum);

                if (result[0] != null) {
                    remoteViews.setTextViewText(R.id.textView_app_widget_nextClass, result[0]);
                    remoteViews.setTextViewText(R.id.textView_app_widget_nextLocation, result[1]);
                    remoteViews.setTextViewText(R.id.textView_app_widget_nextTeacher, result[2]);
                    remoteViews.setTextViewText(R.id.textView_app_widget_nextTime, result[3]);

                    remoteViews.setViewVisibility(R.id.textView_app_widget_noNextClass, View.GONE);
                    remoteViews.setViewVisibility(R.id.layout_app_widget_nextClass, View.VISIBLE);
                } else {
                    remoteViews.setViewVisibility(R.id.textView_app_widget_noNextClass, View.VISIBLE);
                    remoteViews.setViewVisibility(R.id.layout_app_widget_nextClass, View.GONE);
                }
            } else {
                remoteViews.setViewVisibility(R.id.textView_app_widget_noNextClass, View.VISIBLE);
                remoteViews.setViewVisibility(R.id.layout_app_widget_nextClass, View.GONE);
            }
        }
        return remoteViews;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int appWidgetId : appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, ViewGet(context));
        }
        System.gc();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(ACTION_ON_CLICK)) {
                ComponentName componentName = new ComponentName(context, NextClassWidget.class);
                AppWidgetManager.getInstance(context).updateAppWidget(componentName, ViewGet(context));
            }
        }
        System.gc();
    }
}
