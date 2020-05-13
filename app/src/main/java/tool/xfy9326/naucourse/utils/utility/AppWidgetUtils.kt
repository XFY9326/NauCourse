package tool.xfy9326.naucourse.utils.utility

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import tool.xfy9326.naucourse.App
import tool.xfy9326.naucourse.BuildConfig
import tool.xfy9326.naucourse.beans.NextCourseBundle
import tool.xfy9326.naucourse.widget.NextCourseWidget

object AppWidgetUtils {
    const val ACTION_COURSE_WIDGET_CLEAR = "${BuildConfig.APPLICATION_ID}.action.COURSE_WIDGET_CLEAR"

    fun refreshNextCourseWidget(context: Context) =
        context.sendBroadcast(Intent(context, NextCourseWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        })

    fun clearWidget() = App.instance.sendBroadcast(Intent(App.instance, NextCourseWidget::class.java).apply {
        action = ACTION_COURSE_WIDGET_CLEAR
    })

    fun updateNextCourseWidget(context: Context, nextCourseBundle: NextCourseBundle) =
        context.sendBroadcast(Intent(context, NextCourseWidget::class.java).apply {
            action = NextCourseWidget.ACTION_NEXT_COURSE_WIDGET_UPDATE
            putExtra(NextCourseWidget.EXTRA_NEXT_COURSE_WIDGET_DATA, nextCourseBundle)
        })

    fun hasWidget(context: Context, widgetClass: Class<out AppWidgetProvider>): Boolean {
        return AppWidgetManager.getInstance(context).getAppWidgetIds(ComponentName(context, widgetClass)).isNotEmpty()
    }
}