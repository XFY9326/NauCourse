package tool.xfy9326.naucourse.Widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.NextClassMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Receivers.UpdateReceiver;
import tool.xfy9326.naucourse.Utils.NextCourse;

/**
 * Created by 10696 on 2018/2/27.
 * 显示下一节课的桌面小部件
 */

public class NextClassWidget extends AppWidgetProvider {
    public static final String ACTION_ON_CLICK = "tool.xfy9326.naucourse.Widget.NextClassWidget.OnClick";
    private static final int REQUEST_ON_CLICK = 1;
    @Nullable
    private static NextCourse nextCourse = null;

    @NonNull
    synchronized private static RemoteViews ViewGet(Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.app_widget_next_class);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        remoteViews.setTextViewText(R.id.textView_app_widget_dateNow, simpleDateFormat.format(new Date()));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_ON_CLICK, new Intent(context, NextClassWidget.class).setAction(ACTION_ON_CLICK), PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.layout_app_widget, pendingIntent);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            NextCourse next = nextCourse;
            if (next == null) {
                next = NextClassMethod.getNextClassArray(context);
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
                remoteViews.setViewVisibility(R.id.textView_app_widget_noNextClass, View.VISIBLE);
                remoteViews.setViewVisibility(R.id.layout_app_widget_nextClass, View.GONE);
            }
        } else {
            remoteViews.setViewVisibility(R.id.textView_app_widget_noNextClass, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.layout_app_widget_nextClass, View.GONE);
        }
        return remoteViews;
    }

    @Override
    public void onEnabled(@NonNull Context context) {
        //初始化自动更新
        context.sendBroadcast(new Intent(context, UpdateReceiver.class).setAction(UpdateReceiver.UPDATE_ACTION).setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES).putExtra(Config.INTENT_IS_ONLY_INIT, true));
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(@NonNull Context context, @NonNull AppWidgetManager appWidgetManager, @NonNull int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, ViewGet(context));
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(ACTION_ON_CLICK)) {
                ComponentName componentName = new ComponentName(context, NextClassWidget.class);
                AppWidgetManager.getInstance(context).updateAppWidget(componentName, ViewGet(context));
                nextCourse = (NextCourse) intent.getSerializableExtra(Config.INTENT_NEXT_CLASS_DATA);
            }
        }
        super.onReceive(context, intent);
    }
}
