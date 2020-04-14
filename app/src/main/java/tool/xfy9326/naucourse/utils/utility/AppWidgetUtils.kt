package tool.xfy9326.naucourse.utils.utility

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.*
import tool.xfy9326.naucourse.App
import tool.xfy9326.naucourse.beans.NextCourseBundle
import tool.xfy9326.naucourse.widget.NextCourseWidget

object AppWidgetUtils {
    fun BroadcastReceiver.goAsync(
        coroutineScope: CoroutineScope = GlobalScope,
        dispatcher: CoroutineDispatcher = Dispatchers.Default,
        block: suspend () -> Unit
    ) {
        val result = goAsync()
        coroutineScope.launch(dispatcher) {
            try {
                block()
            } finally {
                result.finish()
            }
        }
    }

    fun refreshNextCourseWidget(context: Context) =
        context.sendBroadcast(Intent(context, NextCourseWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        })

    fun clearWidget() {
        App.instance.sendBroadcast(Intent(App.instance, NextCourseWidget::class.java).apply {
            action = NextCourseWidget.ACTION_NEXT_COURSE_WIDGET_CLEAR
        })
    }

    fun updateNextCourseWidget(context: Context, nextCourseBundle: NextCourseBundle) =
        context.sendBroadcast(Intent(context, NextCourseWidget::class.java).apply {
            action = NextCourseWidget.ACTION_NEXT_COURSE_WIDGET_UPDATE
            putExtra(NextCourseWidget.EXTRA_NEXT_COURSE_WIDGET_DATA, nextCourseBundle)
        })
}