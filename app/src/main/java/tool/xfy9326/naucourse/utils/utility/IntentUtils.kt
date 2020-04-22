package tool.xfy9326.naucourse.utils.utility

import android.app.Activity
import android.app.DownloadManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.network.LoginNetworkManager
import tool.xfy9326.naucourse.receivers.NextCourseAlarmReceiver
import tool.xfy9326.naucourse.ui.activities.ImageShowActivity
import tool.xfy9326.naucourse.ui.activities.MainIndexActivity
import tool.xfy9326.naucourse.utils.views.ActivityUtils.showToast


object IntentUtils {
    const val NEW_VERSION_FLAG = "NEW_VERSION_FLAG"
    const val UPDATE_FROM_OLD_DATA_FLAG = "UPDATE_FROM_OLD_DATA_FLAG"

    fun getLaunchMainPendingIntent(context: Context, requestCode: Int): PendingIntent = PendingIntent.getActivity(
        context, requestCode,
        Intent(context, MainIndexActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    fun launchUrlInBrowser(context: Context, url: String) {
        try {
            context.startActivity(Intent().apply {
                action = Intent.ACTION_VIEW
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                data = Uri.parse(url)
            })
        } catch (e: ActivityNotFoundException) {
            showToast(context, R.string.application_launch_failed)
        }
    }

    fun selectPicture(fragment: Fragment, requestCode: Int) {
        try {
            fragment.startActivityForResult(
                Intent(Intent.ACTION_GET_CONTENT)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .setType(Constants.MIME.IMAGE),
                requestCode
            )
        } catch (e: ActivityNotFoundException) {
            fragment.showToast(R.string.application_launch_failed)
        }
    }

    fun viewLargePhotoByUrl(activity: Activity, url: String, loginClientType: LoginNetworkManager.ClientType? = null) {
        activity.startActivity(Intent(activity, ImageShowActivity::class.java).apply {
            putExtra(ImageShowActivity.EXTRA_IMAGE_URL, url)
            if (loginClientType != null) putExtra(ImageShowActivity.EXTRA_LOGIN_CLIENT_TYPE, loginClientType)
        })
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    fun startNextCourseAlarm(context: Context) =
        context.sendBroadcast(Intent(context, NextCourseAlarmReceiver::class.java).apply {
            action = NextCourseAlarmReceiver.ACTION_NEXT_COURSE_ALARM
            putExtra(NextCourseAlarmReceiver.EXTRA_JUST_INIT, true)
        })

    fun refreshNextCourseAlarmData(context: Context) =
        context.sendBroadcast(Intent(context, NextCourseAlarmReceiver::class.java).apply {
            action = NextCourseAlarmReceiver.ACTION_NEXT_COURSE_ALARM
            putExtra(NextCourseAlarmReceiver.EXTRA_JUST_INIT, true)
            putExtra(NextCourseAlarmReceiver.EXTRA_DATA_UPDATE, true)
        })

    fun installApk(context: Context, uri: Uri) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                setDataAndType(uri, Constants.MIME.APK)
            })
        } catch (e: Exception) {
            showToast(context, R.string.application_launch_failed)
        }
    }

    fun requestDownloadUpdate(context: Context, url: String, updateVersionCode: Int, updateVersionName: String) {
        try {
            context.getSystemService<DownloadManager>()?.let {
                AppPref.UpdateDownloadId = it.enqueue(DownloadManager.Request(Uri.parse(url)).apply {
                    val fileName = PathUtils.getUrlFileName(url)
                    setTitle(context.getString(R.string.app_name))
                    setDescription(context.getString(R.string.downloading_update, updateVersionName, updateVersionCode))
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                    setMimeType(Constants.MIME.APK)
                })
            }
        } catch (e: Exception) {
            showToast(context, R.string.application_launch_failed)
        }
    }
}