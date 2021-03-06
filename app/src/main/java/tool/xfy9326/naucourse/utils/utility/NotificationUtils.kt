package tool.xfy9326.naucourse.utils.utility

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import tool.xfy9326.naucourse.BuildConfig
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.beans.NextCourseNotification
import tool.xfy9326.naucourse.constants.TimeConst
import tool.xfy9326.naucourse.utils.views.ViewUtils
import java.text.SimpleDateFormat
import java.util.*


object NotificationUtils {
    private const val CHANNEL_ID_NEXT_COURSE = "${BuildConfig.APPLICATION_ID}.notification.channel.NEXT_COURSE"
    private const val NOTIFY_ID_NEXT_COURSE = 1
    private const val ACTIVITY_REQUEST_CODE = 1

    private val DATE_FORMAT_HM = SimpleDateFormat(TimeConst.FORMAT_HM, Locale.CHINA)

    fun publishNextCourseNotification(context: Context, nextCourseNotification: NextCourseNotification) {
        NotificationManagerCompat.from(context).apply {
            createNextCourseNotificationChannel(context, this)

            NotificationCompat.Builder(context, CHANNEL_ID_NEXT_COURSE).apply {
                setSmallIcon(R.mipmap.ic_launcher)
                setAutoCancel(true)
                setDefaults(NotificationCompat.DEFAULT_ALL)
                setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                priority = NotificationCompat.PRIORITY_DEFAULT
                setCategory(NotificationCompat.CATEGORY_MESSAGE)
                setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))

                val pendingIntent = IntentUtils.getLaunchMainPendingIntent(context, ACTIVITY_REQUEST_CODE)
                setContentIntent(pendingIntent)

                setContentTitle(nextCourseNotification.courseName)
                nextCourseNotification.apply {
                    setContentText(
                        "${ViewUtils.getCourseDataShowText("${courseTeacher}${ViewUtils.COURSE_DATA_JOIN_SYMBOL}${courseLocation}")}  " +
                                "${DATE_FORMAT_HM.format(Date(courseStartDateTime))}~${DATE_FORMAT_HM.format(Date(courseEndDateTime))}"
                    )
                }

                notify(NOTIFY_ID_NEXT_COURSE, build())
            }
        }
    }

    private fun createNextCourseNotificationChannel(context: Context, notificationManager: NotificationManagerCompat) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(CHANNEL_ID_NEXT_COURSE) == null) {
                NotificationChannel(
                    CHANNEL_ID_NEXT_COURSE, context.getString(R.string.next_course_notification_channel), NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = context.getString(R.string.next_course_notification_channel_des)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    setShowBadge(true)
                    enableLights(true)
                    enableVibration(true)
                    notificationManager.createNotificationChannel(this)
                }
            }
        }
    }
}