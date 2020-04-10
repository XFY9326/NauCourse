package tool.xfy9326.naucourse.services

import android.app.IntentService
import android.content.Intent
import tool.xfy9326.naucourse.BuildConfig
import tool.xfy9326.naucourse.utils.debug.LogUtils
import tool.xfy9326.naucourse.utils.utility.NotificationUtils

class NextCourseNotifyService : IntentService(NextCourseNotifyService::class.java.simpleName) {
    companion object {
        const val ACTION_NOTIFY_NEXT_COURSE = "${BuildConfig.APPLICATION_ID}.action.NOTIFY_NEXT_COURSE"

        const val EXTRA_NEXT_COURSE_NAME = "EXTRA_NEXT_COURSE_NAME"
        const val EXTRA_NEXT_COURSE_TEACHER = "EXTRA_NEXT_COURSE_TEACHER"
        const val EXTRA_NEXT_COURSE_LOCATION = "EXTRA_NEXT_COURSE_LOCATION"
        const val EXTRA_NEXT_COURSE_START_TIME = "EXTRA_NEXT_COURSE_START_TIME"
        const val EXTRA_NEXT_COURSE_END_TIME = "EXTRA_NEXT_COURSE_END_TIME"
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent?.action == ACTION_NOTIFY_NEXT_COURSE) {
            if (intent.hasExtra(EXTRA_NEXT_COURSE_NAME) && intent.hasExtra(EXTRA_NEXT_COURSE_TEACHER) &&
                intent.hasExtra(EXTRA_NEXT_COURSE_LOCATION) && intent.hasExtra(EXTRA_NEXT_COURSE_START_TIME) &&
                intent.hasExtra(EXTRA_NEXT_COURSE_END_TIME)
            ) {
                NotificationUtils.publishNextCourseNotification(
                    this,
                    NotificationUtils.NextCourseNotification(
                        intent.getStringExtra(EXTRA_NEXT_COURSE_NAME)!!,
                        intent.getStringExtra(EXTRA_NEXT_COURSE_TEACHER)!!,
                        intent.getStringExtra(EXTRA_NEXT_COURSE_LOCATION)!!,
                        intent.getLongExtra(EXTRA_NEXT_COURSE_START_TIME, 0L),
                        intent.getLongExtra(EXTRA_NEXT_COURSE_END_TIME, 0L)
                    )
                )
            } else {
                LogUtils.d(this, "Incomplete Notify Extra!")
            }
        }
    }
}