package tool.xfy9326.naucourse.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.widget.RemoteViews
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tool.xfy9326.naucourse.BuildConfig
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.beans.NextCourseBundle
import tool.xfy9326.naucourse.utils.BaseUtils
import tool.xfy9326.naucourse.utils.BaseUtils.goAsync
import tool.xfy9326.naucourse.utils.courses.ExtraCourseUtils
import tool.xfy9326.naucourse.utils.debug.ExceptionUtils
import tool.xfy9326.naucourse.utils.utility.AppWidgetUtils
import tool.xfy9326.naucourse.utils.utility.IntentUtils
import tool.xfy9326.naucourse.utils.views.ViewUtils
import java.text.SimpleDateFormat
import java.util.*


class NextCourseWidget : AppWidgetProvider() {
    companion object {
        const val ACTION_NEXT_COURSE_WIDGET_UPDATE = "${BuildConfig.APPLICATION_ID}.action.NEXT_COURSE_WIDGET_UPDATE"
        const val EXTRA_NEXT_COURSE_WIDGET_DATA = "EXTRA_NEXT_COURSE_WIDGET_DATA"
        private const val REQUEST_ON_CLICK_WIDGET_CONTENT = 1
        private val updateLock = Mutex()

        private val DATE_FORMAT_HM = SimpleDateFormat(Constants.Time.FORMAT_HM, Locale.CHINA)

        private fun generateView(context: Context, nextCourseBundle: NextCourseBundle?): RemoteViews = when {
            nextCourseBundle == null || nextCourseBundle.courseDataEmpty ->
                getNextCourseMsgRemoteViews(context, R.string.widget_empty_course_or_term_data, R.drawable.ic_data)
            nextCourseBundle.inVacation ->
                getNextCourseMsgRemoteViews(context, R.string.widget_in_vacation, R.drawable.ic_break)
            nextCourseBundle.hasNextCourse -> {
                val courseBundle = nextCourseBundle.courseBundle!!
                RemoteViews(context.packageName, getNextCourseLayoutId(context)).apply {
                    val colorDrawable = context.getDrawable(R.drawable.shape_today_course_color)!!
                    colorDrawable.colorFilter = PorterDuffColorFilter(courseBundle.courseCellStyle.color, PorterDuff.Mode.SRC_ATOP)
                    setImageViewBitmap(R.id.iv_widgetCourseColor, colorDrawable.toBitmap())

                    setTextViewText(R.id.tv_widgetCourseName, courseBundle.courseItem.course.name)
                    setTextViewText(
                        R.id.tv_widgetCourseDetail,
                        ViewUtils.getCourseDataShowText("${courseBundle.courseItem.course.teacher}${ViewUtils.COURSE_DATA_JOIN_SYMBOL}${courseBundle.courseItem.courseTime.location}")
                    )
                    if (courseBundle.courseItem.detail != null) {
                        setTextViewText(
                            R.id.tv_widgetCourseStartTime,
                            DATE_FORMAT_HM.format(courseBundle.courseItem.detail.dateTimePeriod.startDateTime)
                        )
                    }
                    setContentClickIntent(context, this)
                }
            }
            else -> getNextCourseMsgRemoteViews(context, R.string.no_next_course, R.drawable.ic_break)
        }

        private fun getNextCourseMsgRemoteViews(context: Context, @StringRes strResId: Int, @DrawableRes iconResId: Int) =
            RemoteViews(context.packageName, getNextCourseMsgLayoutId(context)).apply {
                setImageViewResource(R.id.iv_widgetIcon, iconResId)
                setTextViewText(R.id.tv_widgetMsg, context.getString(strResId))
                setContentClickIntent(context, this)
            }

        private fun getNextCourseLayoutId(context: Context) =
            if (BaseUtils.isNightModeUsing(context)) {
                R.layout.widget_next_course_dark
            } else {
                R.layout.widget_next_course
            }

        private fun getNextCourseMsgLayoutId(context: Context) =
            if (BaseUtils.isNightModeUsing(context)) {
                R.layout.widget_next_course_msg_dark
            } else {
                R.layout.widget_next_course_msg
            }

        private fun setContentClickIntent(context: Context, remoteViews: RemoteViews) =
            remoteViews.setOnClickPendingIntent(
                R.id.layout_widgetNextCourse,
                IntentUtils.getLaunchMainPendingIntent(context, REQUEST_ON_CLICK_WIDGET_CONTENT)
            )
    }

    override fun onEnabled(context: Context?) {
        context?.let {
            // 初始化定时器
            IntentUtils.startNextCourseAlarm(it)
            val componentName = ComponentName(it, NextCourseWidget::class.java)
            val appWidgetManager = AppWidgetManager.getInstance(it)
            onUpdate(it, appWidgetManager, appWidgetManager.getAppWidgetIds(componentName))
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        goAsync {
            updateLock.withLock {
                try {
                    // 数据刷新（无传入数据）
                    ExtraCourseUtils.getLocalCourseData().let {
                        val nextCourseBundle = ExtraCourseUtils.getNextCourseInfo(it?.first, it?.second, it?.third)
                        appWidgetManager.updateAppWidget(appWidgetIds, generateView(context, nextCourseBundle))
                    }
                } catch (e: Exception) {
                    ExceptionUtils.printStackTrace(this, e)
                }
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val componentName = ComponentName(it, NextCourseWidget::class.java)
            if (intent?.action == ACTION_NEXT_COURSE_WIDGET_UPDATE) {
                goAsync {
                    updateLock.withLock {
                        // 数据刷新（有传入数据）
                        try {
                            val nextCourseBundle = intent.getSerializableExtra(EXTRA_NEXT_COURSE_WIDGET_DATA) as NextCourseBundle
                            AppWidgetManager.getInstance(it).updateAppWidget(componentName, generateView(it, nextCourseBundle))
                        } catch (e: Exception) {
                            ExceptionUtils.printStackTrace(this, e)
                        }
                    }
                }
            } else if (intent?.action == AppWidgetUtils.ACTION_COURSE_WIDGET_CLEAR) {
                goAsync {
                    updateLock.withLock {
                        try {
                            AppWidgetManager.getInstance(it).updateAppWidget(componentName, generateView(it, null))
                        } catch (e: Exception) {
                            ExceptionUtils.printStackTrace(this, e)
                        }
                    }
                }
            }
        }
        super.onReceive(context, intent)
    }
}