package tool.xfy9326.naucourse.receivers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.AlarmManagerCompat
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import tool.xfy9326.naucourse.BuildConfig
import tool.xfy9326.naucourse.beans.CourseItem
import tool.xfy9326.naucourse.beans.NextCourseBundle
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.services.NextCourseNotifyService
import tool.xfy9326.naucourse.utils.courses.CourseUtils
import tool.xfy9326.naucourse.utils.courses.ExtraCourseUtils
import tool.xfy9326.naucourse.utils.courses.TimeUtils
import tool.xfy9326.naucourse.utils.debug.LogUtils
import tool.xfy9326.naucourse.utils.utility.AppWidgetUtils
import tool.xfy9326.naucourse.utils.utility.AppWidgetUtils.goAsync
import tool.xfy9326.naucourse.widget.NextCourseWidget
import java.util.*

class NextCourseAlarmReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_NEXT_COURSE_ALARM = "${BuildConfig.APPLICATION_ID}.action.NEXT_COURSE_ALARM"
        const val EXTRA_DATA_UPDATE = "EXTRA_DATA_UPDATE"
        const val EXTRA_JUST_INIT = "EXTRA_JUST_INIT"

        private const val REQUEST_NEXT_COURSE_ALARM = 1
        private const val REQUEST_NEXT_COURSE_NOTIFY = 2

        private fun needAlarm(context: Context) = NextCourseWidget.hasWidget(context) || SettingsPref.NotifyNextCourse

        private fun cancelAlarm(context: Context, intent: PendingIntent) =
            (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(intent)

        private suspend fun setNextUpdateAlarm(context: Context, nextCourseBundle: NextCourseBundle? = null) {
            (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).let {
                val updateTime = getNextUpdateTime(nextCourseBundle)
                if (updateTime != null) {
                    val intent = getAlarmPendingIntent(context)
                    if (it.nextAlarmClock?.showIntent == intent && it.nextAlarmClock?.triggerTime != updateTime) {
                        cancelAlarm(context, intent)
                        LogUtils.d<NextCourseAlarmReceiver>("Cancel Old Next Course Alarm")
                    }
                    AlarmManagerCompat.setExactAndAllowWhileIdle(it, AlarmManager.RTC_WAKEUP, updateTime, intent)
                    LogUtils.d<NextCourseAlarmReceiver>("Set Next Course Alarm. Time: $updateTime")
                } else {
                    LogUtils.d<NextCourseAlarmReceiver>("Null Next Update Time!")
                }
            }
        }

        private fun setNextUpdateNotifyAlarm(context: Context, courseItem: CourseItem) {
            (context.getSystemService(Context.ALARM_SERVICE) as AlarmManager).let {
                val updateTime = getNextCourseNotifyTime(courseItem)
                if (updateTime != null) {
                    if (updateTime == 0L) {
                        context.startService(getNotifyIntent(context, courseItem))
                        LogUtils.d<NextCourseAlarmReceiver>("Notify Next Course Now!")
                    } else {
                        val intent = getNotifyPendingIntent(context, courseItem)
                        if (it.nextAlarmClock?.showIntent == intent && it.nextAlarmClock?.triggerTime != updateTime) {
                            cancelAlarm(context, intent)
                            LogUtils.d<NextCourseAlarmReceiver>("Cancel Old Next Course Notify Alarm")
                        }
                        AlarmManagerCompat.setExactAndAllowWhileIdle(it, AlarmManager.RTC_WAKEUP, updateTime, intent)
                        LogUtils.d<NextCourseAlarmReceiver>("Set Next Course Notify Alarm. Time: $updateTime")
                    }
                } else {
                    LogUtils.d<NextCourseAlarmReceiver>("Null Next Update Notify Time!")
                }
            }
        }

        private fun getNotifyPendingIntent(context: Context, courseItem: CourseItem) =
            PendingIntent.getService(context, REQUEST_NEXT_COURSE_NOTIFY, getNotifyIntent(context, courseItem), PendingIntent.FLAG_UPDATE_CURRENT)

        private fun getNotifyIntent(context: Context, courseItem: CourseItem) =
            Intent(context, NextCourseNotifyService::class.java).apply {
                action = NextCourseNotifyService.ACTION_NOTIFY_NEXT_COURSE
                putExtra(NextCourseNotifyService.EXTRA_NEXT_COURSE_NAME, courseItem.course.name)
                putExtra(NextCourseNotifyService.EXTRA_NEXT_COURSE_TEACHER, courseItem.course.teacher)
                putExtra(NextCourseNotifyService.EXTRA_NEXT_COURSE_LOCATION, courseItem.courseTime.location)
                putExtra(NextCourseNotifyService.EXTRA_NEXT_COURSE_START_TIME, courseItem.detail!!.dateTimePeriod.startDateTime.time)
                putExtra(NextCourseNotifyService.EXTRA_NEXT_COURSE_END_TIME, courseItem.detail.dateTimePeriod.endDateTime.time)
            }

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
                    timeInMillis > currentTime -> timeInMillis
                    passStartTime -> null
                    else -> 0L
                }
            }
        }

        private suspend fun getNextUpdateTime(nextCourseBundle: NextCourseBundle?): Long? {
            val currentTime = System.currentTimeMillis()
            val calendar = Calendar.getInstance(Locale.CHINA).apply {
                timeInMillis = currentTime
                set(Calendar.MINUTE, 0)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (nextCourseBundle == null) {
                val courseBundle = ExtraCourseUtils.getLocalSimpleCourseData()
                if (courseBundle != null) {
                    if (courseBundle.first.hasCourse) {
                        if (courseBundle.second.inVacation) {
                            calendar.add(Calendar.DATE, 1)
                        } else {
                            calendar.apply {
                                set(Calendar.HOUR_OF_DAY, TimeUtils.CLASS_TIME_ARR[0].startHour)
                                set(Calendar.MINUTE, TimeUtils.CLASS_TIME_ARR[0].startMinute)
                                add(Calendar.MINUTE, -CourseUtils.NEXT_COURSE_BEFORE_COURSE_START_BASED_MINUTE)
                            }
                            if (calendar.timeInMillis >= currentTime) {
                                return calendar.timeInMillis
                            }

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
                if (!nextCourseBundle.inVacation) {
                    if (nextCourseBundle.hasNextCourse) {
                        nextCourseBundle.courseBundle?.courseItem?.detail?.dateTimePeriod?.endDateTime?.let {
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
                } else {
                    calendar.add(Calendar.DATE, 1)
                    return calendar.timeInMillis
                }
            }
            return null
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            if (intent?.action == Intent.ACTION_BOOT_COMPLETED || intent?.action == ACTION_NEXT_COURSE_ALARM) {
                if (needAlarm(context)) {
                    goAsync {
                        ExtraCourseUtils.getLocalCourseData()?.let {
                            val nextCourseBundle = ExtraCourseUtils.getNextCourseInfo(it.first, it.second, it.third)
                            if (intent.getBooleanExtra(EXTRA_JUST_INIT, false)) {
                                if (intent.getBooleanExtra(EXTRA_DATA_UPDATE, false)) {
                                    LogUtils.d<NextCourseAlarmReceiver>("Next Course Alarm Data Update")
                                    onAlarm(context, nextCourseBundle)
                                    setNextUpdateAlarm(context, nextCourseBundle)
                                } else {
                                    LogUtils.d<NextCourseAlarmReceiver>("Init Next Course Alarm")
                                    setNextUpdateAlarm(context)
                                }
                            } else {
                                onAlarm(context, nextCourseBundle)
                                setNextUpdateAlarm(context, nextCourseBundle)
                            }
                        }
                    }
                } else {
                    cancelAlarm(context, getAlarmPendingIntent(context))
                    LogUtils.d<NextCourseAlarmReceiver>("Not Necessary For Next Course Alarm!")
                }
            }
        }
    }

    private suspend fun onAlarm(context: Context, nextCourseBundle: NextCourseBundle) = coroutineScope {
        launch {
            if (NextCourseWidget.hasWidget(context)) {
                AppWidgetUtils.updateNextCourseWidget(context, nextCourseBundle)
            }
        }

        launch {
            if (SettingsPref.NotifyNextCourse) {
                if (nextCourseBundle.hasNextCourse) {
                    nextCourseBundle.courseBundle?.courseItem?.let {
                        if (it.detail != null) setNextUpdateNotifyAlarm(context, it)
                    }
                }
            }
        }
    }
}