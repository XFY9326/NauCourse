package tool.xfy9326.naucourse.utils.utility

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.utils.views.ActivityUtils.showToast


object IntentUtils {
    const val NEW_VERSION_FLAG = "NEW_VERSION_FLAG"
    const val UPDATE_FROM_OLD_DATA_FLAG = "UPDATE_FROM_OLD_DATA_FLAG"

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
}