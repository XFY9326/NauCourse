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
import java.util.Date;
import java.util.Locale;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.NextClassMethod;
import tool.xfy9326.naucourse.R;

/**
 * Created by 10696 on 2018/2/27.
 * 显示下一节课的桌面小部件
 */

public class NextClassWidget extends AppWidgetProvider {
    public static final String ACTION_ON_CLICK = "tool.xfy9326.naucourse.Views.NextClassWidget.OnClick";
    private static final int REQUEST_ON_CLICK = 1;
    private static String[] nextData;

    synchronized private static RemoteViews ViewGet(Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.app_widget_next_class);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        remoteViews.setTextViewText(R.id.textView_app_widget_dateNow, simpleDateFormat.format(new Date()));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_ON_CLICK, new Intent(context, NextClassWidget.class).setAction(ACTION_ON_CLICK), PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.layout_app_widget, pendingIntent);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPreferences.getBoolean(Config.PREFERENCE_HAS_LOGIN, Config.DEFAULT_PREFERENCE_HAS_LOGIN)) {
            String[] result;
            if (nextData != null) {
                result = nextData;
            } else {
                result = NextClassMethod.getNextClassArray(context);
            }
            if (result != null) {
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
            }
        } else {
            remoteViews.setViewVisibility(R.id.textView_app_widget_noNextClass, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.layout_app_widget_nextClass, View.GONE);
        }
        return remoteViews;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, ViewGet(context));
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(ACTION_ON_CLICK)) {
                ComponentName componentName = new ComponentName(context, NextClassWidget.class);
                AppWidgetManager.getInstance(context).updateAppWidget(componentName, ViewGet(context));
                nextData = intent.getStringArrayExtra(Config.INTENT_NEXT_CLASS_DATA);
            }
        }
        super.onReceive(context, intent);
    }
}
