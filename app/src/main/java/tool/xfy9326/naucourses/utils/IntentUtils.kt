package tool.xfy9326.naucourses.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.utils.views.ActivityUtils

object IntentUtils {
    fun launchUrlInBrowser(context: Context, url: String) {
        try {
            context.startActivity(Intent().apply {
                action = Intent.ACTION_VIEW
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                data = Uri.parse(url)
            })
        } catch (e: ActivityNotFoundException) {
            ActivityUtils.showToast(context, R.string.application_launch_failed)
        }
    }
}