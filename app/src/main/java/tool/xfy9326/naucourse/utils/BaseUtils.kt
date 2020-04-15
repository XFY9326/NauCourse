package tool.xfy9326.naucourse.utils

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.IBinder
import android.util.TypedValue
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.sync.Mutex
import okhttp3.internal.toHexString
import tool.xfy9326.naucourse.App
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import java.util.*
import kotlin.system.exitProcess

@Suppress("unused")
object BaseUtils {
    private const val FILL_ZERO = "0"
    const val CRASH_RESTART_FLAG = "CRASH_RESTART"

    fun Float.dpToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)

    fun Int.dpToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics).toInt()

    fun Float.spToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics)

    fun Int.spToPx() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.toFloat(), Resources.getSystem().displayMetrics).toInt()

    fun ByteArray.toHex(): String {
        var tmp: String
        val textBuilder = StringBuilder()
        for (byte in this) {
            tmp = (byte.toInt() and 0xFF).toHexString()
            if (tmp.length == 1) {
                textBuilder.append(FILL_ZERO).append(tmp)
            } else {
                textBuilder.append(tmp)
            }
        }
        return textBuilder.toString()
    }

    fun String.hexToByteArray(): ByteArray {
        if (this.length < 2) {
            return ByteArray(0)
        }
        val lowerString = this.toLowerCase(Locale.CHINA)
        val result = ByteArray(lowerString.length / 2)
        for (i in result.indices) {
            val tmp = lowerString.substring(2 * i, 2 * i + 2)
            result[i] = (tmp.toInt(16) and 0xFF).toByte()
        }
        return result
    }

    inline fun Mutex.tryWithLock(owner: Any? = null, action: () -> Unit) {
        if (tryLock(owner)) {
            try {
                action()
            } finally {
                unlock(owner)
            }
        }
    }

    fun getNightModeInt(type: SettingsPref.NightModeType) =
        when (type) {
            SettingsPref.NightModeType.AUTO -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            SettingsPref.NightModeType.ENABLED -> AppCompatDelegate.MODE_NIGHT_YES
            SettingsPref.NightModeType.DISABLED -> AppCompatDelegate.MODE_NIGHT_NO
        }

    fun restartApplication(crashRestart: Boolean = false) {
        val intent = App.instance.packageManager.getLaunchIntentForPackage(App.instance.packageName)!!.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (crashRestart) putExtra(CRASH_RESTART_FLAG, true)
        }
        App.instance.startActivity(intent)
        if (crashRestart) exitProcess(0)
    }

    fun hideKeyboard(context: Context, windowToken: IBinder) =
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
            if (isActive) hideSoftInputFromWindow(windowToken, 0)
        }
}