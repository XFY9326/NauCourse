package tool.xfy9326.naucourse.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.getSystemService
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import tool.xfy9326.naucourse.BuildConfig
import tool.xfy9326.naucourse.beans.CourseItem
import tool.xfy9326.naucourse.beans.NextCourseBundle
import tool.xfy9326.naucourse.beans.NextCourseNotification
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.kt.goAsync
import tool.xfy9326.naucourse.utils.courses.CourseUtils
import tool.xfy9326.naucourse.utils.courses.ExtraCourseUtils
import tool.xfy9326.naucourse.utils.courses.TimeUtils
import tool.xfy9326.naucourse.utils.utility.AppWidgetUtils
import tool.xfy9326.naucourse.utils.utility.NotificationUtils
import tool.xfy9326.naucourse.widget.NextCourseWidget
import java.util.*

class NextCourseAlarmReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_NEXT_COURSE_ALARM = "${BuildConfig.APPLICATION_ID}.action.NEXT_COURSE_ALARM"
        const val ACTION_NEXT_COURSE_NOTIFY = "${BuildConfig.APPLICATION_ID}.action.NEXT_COURSE_NOTIFY"
        const val EXTRA_DATA_UPDATE = "EXTRA_DATA_UPDATE"
        const val EXTRA_JUST_INIT = "EXTRA_JUST_INIT"

        private const val REQUEST_NEXT_COURSE_ALARM = 1
        private const val REQUEST_NEXT_COURSE_NOTIFY = 2

        private fun needAlarm(context: Context) = AppWidgetUtils.hasWidget(context, NextCourseWidget::class.java) || SettingsPref.NotifyNextCourse

        private fun cancelAlarm(context: Context, intent: PendingIntent) =
            context.getSystemService<AlarmManager>()?.cancel(intent)

        private suspend fun setNextUpdateAlarm(context: Context, nextCourseBundle: NextCourseBundle? = null, init: Boolean = false) {
            context.getSystemService<AlarmManager>()?.let {
                getNextUpdateTime(nextCourseBundle, init)?.let { time ->
                    AlarmManagerCompat.setExactAndAllowWhileIdle(it, AlarmManager.RTC_WAKEUP, time, getAlarmPendingIntent(context))
                }
            }
        }

        private fun setNextUpdateNotifyAlarm(context: Context, courseItem: CourseItem) {
            context.getSystemService<AlarmManager>()?.let {
                getNextCourseNotifyTime(courseItem)?.let { time ->
                    courseItem.toNotifyData().let { data ->
                        if (time == 0L) {
                            if (data.hashCode() != AppPref.LastNotifyCourseHash) {
                                NotificationUtils.publishNextCourseNotification(context, data)
                            }
                        } else {
                            AlarmManagerCompat.setExactAndAllowWhileIdle(
                                it,
                                AlarmManager.RTC_WAKEUP,
                                time,
                                getNotifyPendingIntent(context, data)
                            )
                        }
                        AppPref.LastNotifyCourseHash = data.hashCode()
                    }
                }
            }
        }

        private fun getNotifyPendingIntent(context: Context, notifyData: NextCourseNotification) =
            PendingIntent.getBroadcast(context, REQUEST_NEXT_COURSE_NOTIFY, Intent(context, NextCourseAlarmReceiver::class.java).apply {
                action = ACTION_NEXT_COURSE_NOTIFY
                putExtras(notifyData.toBundle())
            }, PendingIntent.FLAG_UPDATE_CURRENT)

        private fun getAlarmPendingIntent(context: Context) = PendingIntent.getBroadcast(
            context, REQUEST_NEXT_COURSE_ALARM, Intent(context, NextCourseAlarmReceiver::class.java).apply {
                action = ACTION_NEXT_COURSE_ALARM
                flags = Intent.FLAG_INCLUDE_STOPPED_PACKAGES
            }, PendingIntent.FLAG_UPDATE_CURRENT
        )

        private fun getNextCourseNotifyTime(courseItem: CourseItem): Long? {
            Calendar.getInstance(Locale.CHINA).apply {
                val currentTime = System.currentTimeMillis()

                time = courseItem.detail!!.dateTimePeriod.startDateTime
                val passStartTime = currentTime > timeInMillis

                add(Calendar.MINUTE, -CourseUtils.NEXT_COURSE_BEFORE_COURSE_START_BASED_MINUTE)
                return when {
                    // 得出上课通知时间
                    timeInMillis > currentTime -> timeInMillis
                    // 已经上课
                    passStartTime -> null
                    // 立即通知（未上课但是已经过了通知时间）
                    else -> 0
                }
            }
        }

        private suspend fun getNextUpdateTime(nextCourseBundle: NextCourseBundle?, init: Boolean): Long? {
            val currentTime = System.currentTimeMillis()
            val calendar = Calendar.getInstance(Locale.CHINA).apply {
                timeInMillis = currentTime
                set(Calendar.MINUTE, 0)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (nextCourseBundle == null) {
                // 直接通过课程时间算出下次通知的时间
                val courseBundle = ExtraCourseUtils.getLocalSimpleCourseData()
                if (courseBundle != null) {
                    if (courseBundle.first.hasCourse) {
                        if (courseBundle.second.inVacation) {
                            calendar.add(Calendar.DATE, 1)
                        } else {
                            // 第一节课上课
                            calendar.apply {
                                set(Calendar.HOUR_OF_DAY, TimeUtils.CLASS_TIME_ARR[0].startHour)
                                set(Calendar.MINUTE, TimeUtils.CLASS_TIME_ARR[0].startMinute)
                                add(Calendar.MINUTE, -CourseUtils.NEXT_COURSE_BEFORE_COURSE_START_BASED_MINUTE)
                            }
                            if (calendar.timeInMillis >= currentTime) {
                                return calendar.timeInMillis
                            }

                            // 所有课下课
                            var found = false
                            for (classTime in TimeUtils.CLASS_TIME_ARR) {
                                calendar.apply {
                                    set(Calendar.HOUR_OF_DAY, classTime.endHour)
                                    set(Calendar.MINUTE, classTime.endMinute)
                                    add(Calendar.MINUTE, -CourseUtils.NEXT_COURSE_BEFORE_COURSE_END_BASED_MINUTE)
                                }
                                if (calendar.timeInMillis >= currentTime) {
                                    found = true
                                    break
                                }
                            }
                            if (!found) {
                                calendar.apply {
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    add(Calendar.DATE, 1)
                                }
                            }
                        }
                        return calendar.timeInMillis
                    }
                }
            } else if (!nextCourseBundle.courseDataEmpty) {
                // 通过下一节课的数据算出下次通知的时间
                if (nextCourseBundle.hasNextCourse) {
                    nextCourseBundle.courseBundle!!
                    if (init) {
                        nextCourseBundle.courseBundle.courseItem.detail?.dateTimePeriod?.startDateTime
                    } else {
                        nextCourseBundle.courseBundle.courseItem.detail?.dateTimePeriod?.endDateTime
                    }?.let {
                        calendar.apply {
                            calendar.time = it
                            calendar.add(Calendar.MINUTE, -CourseUtils.NEXT_COURSE_BEFORE_COURSE_END_BASED_MINUTE)
                            return calendar.timeInMillis
                        }
                    }
                } else {
                    calendar.add(Calendar.DATE, 1)
                    return calendar.timeInMillis
                }
            }
            return null
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            if (intent?.action == ACTION_NEXT_COURSE_NOTIFY) {
                intent.extras?.let { data ->
                    goAsync {
                        NotificationUtils.publishNextCourseNotification(context, NextCourseNotification.fromBundle(data))
                    }
                }
            } else if (intent?.action == Intent.ACTION_BOOT_COMPLETED || intent?.action == ACTION_NEXT_COURSE_ALARM) {
                if (needAlarm(context)) {
                    goAsync {
                        ExtraCourseUtils.getLocalCourseData()?.let { data ->
                            val nextCourseBundle = ExtraCourseUtils.getNextCourseInfo(data.first, data.second, data.third)
                            if (intent.getBooleanExtra(EXTRA_JUST_INIT, false)) {
                                if (intent.getBooleanExtra(EXTRA_DATA_UPDATE, false)) {
                                    // 数据刷新
                                    onAlarm(context, nextCourseBundle)
                                    setNextUpdateAlarm(context, nextCourseBundle)
                                } else {
                                    // 定时器初始化
                                    setNextUpdateAlarm(context, nextCourseBundle, true)
                                }
                            } else {
                                // 定时器通知
                                onAlarm(context, nextCourseBundle)
                                setNextUpdateAlarm(context, nextCourseBundle)
                            }
                        }
                    }
                } else {
                    // 不再需要定时器
                    cancelAlarm(context, getAlarmPendingIntent(context))
                }
            }
        }
    }

    private suspend fun onAlarm(context: Context, nextCourseBundle: NextCourseBundle) = coroutineScope {
        launch {
            if (AppWidgetUtils.hasWidget(context, NextCourseWidget::class.java)) {
                AppWidgetUtils.updateNextCourseWidget(context, nextCourseBundle)
            }
        }

        launch {
            if (SettingsPref.NotifyNextCourse && nextCourseBundle.hasNextCourse) {
                nextCourseBundle.courseBundle?.courseItem?.let {
                    if (it.detail != null) setNextUpdateNotifyAlarm(context, it)
                }
            }
        }
    }
}