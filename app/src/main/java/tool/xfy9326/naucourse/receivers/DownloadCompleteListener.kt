package tool.xfy9326.naucourse.receivers

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.utils.debug.ExceptionUtils
import tool.xfy9326.naucourse.utils.utility.IntentUtils

class DownloadCompleteListener : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            try {
                context?.getSystemService<DownloadManager>()?.getUriForDownloadedFile(AppPref.UpdateDownloadId)?.let {
                    IntentUtils.installApk(context.applicationContext, it)
                }
            } catch (e: Exception) {
                ExceptionUtils.printStackTrace(this, e)
            }
        }
    }
}