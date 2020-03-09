package tool.xfy9326.naucourses.utils.views

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.utils.utility.IOUtils
import tool.xfy9326.naucourses.utils.utility.IntentUtils

object DialogUtils {
    fun createUsingLicenseDialog(context: Context): AlertDialog =
        MaterialAlertDialogBuilder(context).apply {
            setTitle(R.string.eula_license)
            setMessage(
                IOUtils.readAssetFileAsText(
                    context,
                    IOUtils.ASSETS_PATH_EULA_LICENSE
                )
            )
            setPositiveButton(android.R.string.yes, null)
            background = context.getDrawable(R.drawable.bg_dialog)
        }.create()

    fun createOpenSourceLicenseDialog(context: Context): AlertDialog =
        MaterialAlertDialogBuilder(context).apply {
            setTitle(R.string.open_source_license)
            setMessage(
                IOUtils.readAssetFileAsText(
                    context,
                    IOUtils.ASSETS_PATH_OPEN_SOURCE_LICENSE
                )
            )
            setPositiveButton(android.R.string.yes, null)
            background = context.getDrawable(R.drawable.bg_dialog)
        }.create()

    fun createForgetPasswordDialog(context: Context): AlertDialog =
        MaterialAlertDialogBuilder(context).apply {
            setTitle(R.string.forget_password)
            setMessage(R.string.forget_password_help)
            setNeutralButton(R.string.find_password) { _: DialogInterface, _: Int ->
                IntentUtils.launchUrlInBrowser(context, Constants.Others.FORGET_PASSWORD)
            }
            setPositiveButton(android.R.string.yes, null)
            background = context.getDrawable(R.drawable.bg_dialog)
        }.create()

    fun createLogoutAttentionDialog(context: Context, logoutListener: DialogInterface.OnClickListener): AlertDialog =
        MaterialAlertDialogBuilder(context).apply {
            setTitle(R.string.logout)
            setMessage(R.string.logout_msg)
            setNegativeButton(android.R.string.cancel, null)
            setPositiveButton(android.R.string.yes, logoutListener)
            background = context.getDrawable(R.drawable.bg_dialog)
        }.create()
}