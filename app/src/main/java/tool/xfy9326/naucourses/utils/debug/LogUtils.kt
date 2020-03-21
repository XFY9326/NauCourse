package tool.xfy9326.naucourses.utils.debug

import android.util.Log
import tool.xfy9326.naucourses.BuildConfig
import java.util.*

object LogUtils {
    private val LOG_ON = BuildConfig.DEBUG
    private const val LOG_SAVE_ON = false

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
        if (LOG_ON) Log.println(type, tag, msg)
        if (LOG_SAVE_ON) DebugIOUtils.append(DebugIOUtils.DebugSaveType.LOG, getOutputLog(type, tag, msg))
    }

    private fun getOutputLog(type: Int, tag: String, msg: String) =
        "${Date()} ${
        when (type) {
            Log.DEBUG -> "D"
            Log.ERROR -> "E"
            Log.WARN -> "W"
            Log.INFO -> "I"
            else -> "U"
        }
        }/$tag: $msg"
}