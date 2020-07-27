package tool.xfy9326.naucourse.utils.utility

import android.app.DownloadManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.constants.MIMEConst
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.kt.showShortToast
import tool.xfy9326.naucourse.receivers.NextCourseAlarmReceiver
import tool.xfy9326.naucourse.ui.activities.MainIndexActivity


object IntentUtils {
    private const val QQ_GROUP_KEY = "TCoF0ryy-exOFVeKAe1jTAxmgj-PS1t-"
    const val NEW_VERSION_FLAG = "NEW_VERSION_FLAG"
    const val UPDATE_FROM_OLD_DATA_FLAG = "UPDATE_FROM_OLD_DATA_FLAG"

    fun getLaunchMainPendingIntent(context: Context, requestCode: Int): PendingIntent = PendingIntent.getActivity(
        context, requestCode,
        Intent(context, MainIndexActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    fun joinFeedbackQQGroup(context: Context) {
        try {
            context.startActivity(Intent().apply {
                data =
                    Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D$QQ_GROUP_KEY")
            })
        } catch (e: Exception) {
            context.showShortToast(R.string.application_launch_failed)
        }
    }

    fun launchUrlInBrowser(context: Context, url: String) {
        try {
            context.startActivity(Intent().apply {
                action = Intent.ACTION_VIEW
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                data = Uri.parse(url)
            })
        } catch (e: ActivityNotFoundException) {
            context.showShortToast(R.string.application_launch_failed)
        }
    }

    fun selectPicture(fragment: Fragment, requestCode: Int) {
        try {
            fragment.startActivityForResult(
                Intent(Intent.ACTION_GET_CONTENT)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .setType(MIMEConst.IMAGE),
                requestCode
            )
        } catch (e: ActivityNotFoundException) {
            fragment.showShortToast(R.string.application_launch_failed)
        }
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
                setDataAndType(uri, MIMEConst.APK)
            })
        } catch (e: Exception) {
            context.showShortToast(R.string.application_launch_failed)
        }
    }

    fun requestDownloadUpdate(context: Context, url: String, updateVersionCode: Int, updateVersionName: String) {
        try {
            val downloadManager = context.getSystemService<DownloadManager>()
            if (downloadManager != null) {
                AppPref.UpdateDownloadId = downloadManager.enqueue(DownloadManager.Request(Uri.parse(url)).apply {
                    val fileName = PathUtils.getUrlFileName(url)
                    setTitle(context.getString(R.string.app_name))
                    setDescription(context.getString(R.string.downloading_update, updateVersionName, updateVersionCode))
                    setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
                    setMimeType(MIMEConst.APK)
                })
            } else {
                launchUrlInBrowser(context, url)
            }
        } catch (e: Exception) {
            launchUrlInBrowser(context, url)
        }
    }
}