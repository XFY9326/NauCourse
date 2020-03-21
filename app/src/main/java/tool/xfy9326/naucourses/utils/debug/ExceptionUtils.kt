package tool.xfy9326.naucourses.utils.debug

import tool.xfy9326.naucourses.App
import tool.xfy9326.naucourses.BuildConfig
import tool.xfy9326.naucourses.io.prefs.AppPref
import tool.xfy9326.naucourses.utils.BaseUtils
import java.io.PrintWriter
import java.io.StringWriter
import java.net.UnknownHostException

object ExceptionUtils : Thread.UncaughtExceptionHandler {
    private const val CRASH_RESTART_PERIOD_MILLS = 3000L
    private val THROWS_ON = BuildConfig.DEBUG
    private val THROWS_SAVE_ON = BuildConfig.DEBUG
    private const val GLOBAL_THROWS_SAVE_ON = true

    private var exceptionHandler: Thread.UncaughtExceptionHandler? = null

    inline fun <reified T> printStackTrace(throwable: Throwable) {
        print(T::class.java.simpleName, throwable)
    }

    inline fun <reified T : Any> printStackTrace(clazz: T, throwable: Throwable) {
        print(clazz::class.java.simpleName, throwable)
    }

    fun print(tag: String, throwable: Throwable) {
        if (THROWS_ON) throwable.printStackTrace()
        if (THROWS_SAVE_ON) saveThrowable(tag, getStackTraceString(throwable))
    }

    fun initCrashHandler() {
        exceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    @Suppress("ConstantConditionIf")
    override fun uncaughtException(t: Thread, e: Throwable) {
        try {
            if (GLOBAL_THROWS_SAVE_ON) {
                saveError(getStackTraceString(e))
                crashRestart(t, e)
            } else {
                crashRestart(t, e)
            }
        } catch (e: Exception) {
            exceptionHandler?.uncaughtException(t, e)
        }
    }

    private fun saveError(text: String) = DebugIOUtils.appendAsync(DebugIOUtils.DebugSaveType.ERROR, text)

    private fun saveThrowable(tag: String, text: String) = DebugIOUtils.append(DebugIOUtils.DebugSaveType.EXCEPTION, "$tag:\n$text")

    private fun crashRestart(t: Thread, e: Throwable) {
        val lastCrashMills = AppPref.LastCrashTimeMills
        AppPref.LastCrashTimeMills = System.currentTimeMillis()
        if (System.currentTimeMillis() - lastCrashMills > CRASH_RESTART_PERIOD_MILLS) {
            BaseUtils.restartApplication(App.instance, true)
        } else {
            exceptionHandler?.uncaughtException(t, e)
        }
    }

    private fun getStackTraceString(throwable: Throwable): String {
        var th: Throwable? = throwable
        while (th != null) {
            if (th is UnknownHostException) return "UnknownHostException: Network Unavailable! ${th.message}"
            th = th.cause
        }
        val writer = StringWriter()
        val printer = PrintWriter(writer)
        throwable.printStackTrace(printer)
        printer.flush()
        return writer.toString()
    }
}