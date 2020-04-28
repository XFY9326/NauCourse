package tool.xfy9326.naucourse.utils.utility

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

@Suppress("SameParameterValue")
object PermissionUtils {
    private const val REQUEST_CODE_STORAGE_PERMISSION = 3

    fun prepareStoragePermission(activity: Activity) =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            checkAndRequestPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_CODE_STORAGE_PERMISSION)
        } else {
            true
        }

    private fun checkAndRequestPermission(activity: Activity, permission: String, requestCode: Int): Boolean {
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(permission), requestCode)
            return false
        }
        return true
    }
}