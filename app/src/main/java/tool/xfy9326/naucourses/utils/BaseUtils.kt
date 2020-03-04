package tool.xfy9326.naucourses.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.util.TypedValue
import kotlin.system.exitProcess


object BaseUtils {
    fun Float.dpToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)

    fun Int.dpToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics).toInt()

    fun Float.spToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics)

    fun Int.spToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.toFloat(), Resources.getSystem().displayMetrics).toInt()

    fun restartApplication(context: Context) {
        val packageManager: PackageManager = context.applicationContext.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)!!
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.applicationContext.startActivity(intent)
        exitProcess(0)
    }
}