package tool.xfy9326.naucourse.utils.debug

import android.util.Log
import tool.xfy9326.naucourse.BuildConfig
import tool.xfy9326.naucourse.constants.TimeConst
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import java.text.SimpleDateFormat
import java.util.*

@Suppress("unused")
object LogUtils {
    @Suppress("MayBeConstant")
    private val LOG_ON = BuildConfig.DEBUG
    private val LOG_SAVE_ON get() = SettingsPref.DebugMode && SettingsPref.DebugLogCatch
    private val DATE_FORMAT_YMD_HM_S = SimpleDateFormat(TimeConst.FORMAT_YMD_HM_S, Locale.CHINA)

    inline fun <reified T> d(msg: String) {
        print(Log.DEBUG, T::class.java.simpleName, msg)
    }

    inline fun <reified T> e(msg: String) {
        print(Log.ERROR, T::class.java.simpleName, msg)
    }

    inline fun <reified T> w(msg: String) {
        print(Log.WARN, T::class.java.simpleName, msg)
    }

    inline fun <reified T> i(msg: String) {
        print(Log.INFO, T::class.java.simpleName, msg)
    }

    inline fun <reified T : Any> d(clazz: T, msg: String) {
        print(Log.DEBUG, clazz::class.java.simpleName, msg)
    }

    inline fun <reified T : Any> e(clazz: T, msg: String) {
        print(Log.ERROR, clazz::class.java.simpleName, msg)
    }

    inline fun <reified T : Any> w(clazz: T, msg: String) {
        print(Log.WARN, clazz::class.java.simpleName, msg)
    }

    inline fun <reified T : Any> i(clazz: T, msg: String) {
        print(Log.INFO, clazz::class.java.simpleName, msg)
    }

    @Suppress("ConstantConditionIf")
    fun print(type: Int, tag: String, msg: String) {
        if (DebugIOUtils.FORCE_DEBUG_ON || LOG_ON) Log.println(type, "LogUtils-$tag", msg)
        if (DebugIOUtils.FORCE_DEBUG_ON || LOG_SAVE_ON) DebugIOUtils.append(DebugIOUtils.DebugSaveType.LOG, getOutputLog(type, tag, msg))
    }

    private fun getOutputLog(type: Int, tag: String, msg: String) =
        "${DATE_FORMAT_YMD_HM_S.format(Date())} ${
        when (type) {
            Log.DEBUG -> "D"
            Log.ERROR -> "E"
            Log.WARN -> "W"
            Log.INFO -> "I"
            else -> "U"
        }
        }/$tag: $msg"
}