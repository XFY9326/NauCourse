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
import tool.xfy9326.naucourse.BuildConfig
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.beans.NextCourseBundle
import tool.xfy9326.naucourse.io.store.NextCourseBundleStore
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

        private val DATE_FORMAT_HM = SimpleDateFormat(Constants.Time.FORMAT_MD_HM, Locale.CHINA)

        private fun generateView(context: Context, nextCourseBundle: NextCourseBundle?): RemoteViews = when {
            nextCourseBundle == null || nextCourseBundle.courseDataEmpty -> RemoteViews(context.packageName, R.layout.widget_next_course_msg).apply {
                setWidgetMsg(context, this, R.string.widget_empty_course_or_term_data, R.drawable.ic_data)
                setContentClickIntent(context, this)
            }
            nextCourseBundle.inVacation -> RemoteViews(context.packageName, R.layout.widget_next_course_msg).apply {
                setWidgetMsg(context, this, R.string.widget_in_vacation, R.drawable.ic_break)
                setContentClickIntent(context, this)
            }
            nextCourseBundle.hasNextCourse -> {
                val courseBundle = nextCourseBundle.courseBundle!!
                RemoteViews(context.packageName, R.layout.widget_next_course).apply {
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
            else -> RemoteViews(context.packageName, R.layout.widget_next_course_msg).apply {
                setWidgetMsg(context, this, R.string.no_next_course, R.drawable.ic_break)
                setContentClickIntent(context, this)
            }
        }

        private fun setContentClickIntent(context: Context, remoteViews: RemoteViews) =
            remoteViews.setOnClickPendingIntent(
                R.id.layout_widgetNextCourse,
                IntentUtils.getLaunchMainPendingIntent(context, REQUEST_ON_CLICK_WIDGET_CONTENT)
            )

        private fun setWidgetMsg(context: Context, remoteViews: RemoteViews, @StringRes strResId: Int, @DrawableRes iconResId: Int) {
            remoteViews.apply {
                setImageViewResource(R.id.iv_widgetIcon, iconResId)
                setTextViewText(R.id.tv_widgetMsg, context.getString(strResId))
            }
        }

        fun hasWidget(context: Context): Boolean {
            val componentName = ComponentName(context, NextCourseWidget::class.java)
            return AppWidgetManager.getInstance(context).getAppWidgetIds(componentName).isNotEmpty()
        }
    }

    override fun onEnabled(context: Context?) {
        context?.let {
            // 初始化定时器
            IntentUtils.startNextCourseAlarm(it)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        goAsync {
            try {
                // 数据刷新（无传入数据）
                ExtraCourseUtils.getLocalCourseData().let {
                    val nextCourseBundle = ExtraCourseUtils.getNextCourseInfo(it?.first, it?.second, it?.third)
                    appWidgetManager.updateAppWidget(appWidgetIds, generateView(context, nextCourseBundle))
                    NextCourseBundleStore.saveStore(nextCourseBundle)
                }
            } catch (e: Exception) {
                ExceptionUtils.printStackTrace(this, e)
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            val componentName = ComponentName(it, NextCourseWidget::class.java)
            if (intent?.action == ACTION_NEXT_COURSE_WIDGET_UPDATE) {
                goAsync {
                    // 数据刷新（有传入数据）
                    try {
                        val nextCourseBundle = intent.getSerializableExtra(EXTRA_NEXT_COURSE_WIDGET_DATA) as NextCourseBundle
                        AppWidgetManager.getInstance(it).updateAppWidget(componentName, generateView(it, nextCourseBundle))
                        NextCourseBundleStore.saveStore(nextCourseBundle)
                    } catch (e: Exception) {
                        ExceptionUtils.printStackTrace(this, e)
                    }
                }
            } else if (intent?.action == AppWidgetUtils.ACTION_COURSE_WIDGET_CLEAR) {
                goAsync {
                    try {
                        AppWidgetManager.getInstance(it).updateAppWidget(componentName, generateView(it, null))
                        NextCourseBundleStore.clearStore()
                    } catch (e: Exception) {
                        ExceptionUtils.printStackTrace(this, e)
                    }
                }
            }
        }
        super.onReceive(context, intent)
    }
}