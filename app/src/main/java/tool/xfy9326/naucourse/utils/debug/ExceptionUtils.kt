package tool.xfy9326.naucourse.utils.debug

import tool.xfy9326.naucourse.BuildConfig
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.utils.BaseUtils
import java.io.PrintWriter
import java.io.StringWriter
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@Suppress("unused")
object ExceptionUtils : Thread.UncaughtExceptionHandler {
    private const val CRASH_RESTART_PERIOD_MILLS = 5000L

    @Suppress("MayBeConstant")
    private val THROWS_ON = BuildConfig.DEBUG
    private val THROWS_SAVE_ON get() = SettingsPref.DebugMode && SettingsPref.DebugExceptionCatch
    private val GLOBAL_THROWS_SAVE_ON get() = SettingsPref.CrashCatch

    private var exceptionHandler: Thread.UncaughtExceptionHandler? = null

    inline fun <reified T> printStackTrace(throwable: Throwable) {
        print(T::class.java.simpleName, throwable)
    }

    inline fun <reified T : Any> printStackTrace(clazz: T, throwable: Throwable) {
        print(clazz::class.java.simpleName, throwable)
    }

    fun print(tag: String, throwable: Throwable) {
        if (DebugIOUtils.FORCE_DEBUG_ON || THROWS_ON) throwable.printStackTrace()
        if (DebugIOUtils.FORCE_DEBUG_ON || THROWS_SAVE_ON) saveThrowable(tag, getStackTraceString(throwable))
    }

    fun initCrashHandler() {
        exceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    @Suppress("ConstantConditionIf")
    override fun uncaughtException(t: Thread, e: Throwable) {
        try {
            if (recordThisCrash()) {
                if (DebugIOUtils.FORCE_DEBUG_ON || GLOBAL_THROWS_SAVE_ON) {
                    saveError(getStackTraceString(e))
                    BaseUtils.restartApplication(true)
                } else {
                    BaseUtils.restartApplication(true)
                }
            } else {
                exceptionHandler?.uncaughtException(t, e)
            }

        } catch (e: Exception) {
            exceptionHandler?.uncaughtException(t, e)
        }
    }

    private fun saveError(text: String) = DebugIOUtils.appendAsync(DebugIOUtils.DebugSaveType.ERROR, text)

    private fun saveThrowable(tag: String, text: String) = DebugIOUtils.append(DebugIOUtils.DebugSaveType.EXCEPTION, "$tag:\n$text")

    private fun recordThisCrash(): Boolean {
        val lastCrashMills = AppPref.LastCrashTimeMills
        AppPref.LastCrashTimeMills = System.currentTimeMillis()
        return System.currentTimeMillis() - lastCrashMills > CRASH_RESTART_PERIOD_MILLS
    }

    private fun getStackTraceString(throwable: Throwable): String {
        var th: Throwable? = throwable
        while (th != null) {
            if (th is UnknownHostException) return "UnknownHostException: Network Unavailable! ${th.message}"
            if (th is SocketTimeoutException) return "SocketTimeoutException: Network Connection Timeout! ${th.message}"
            th = th.cause
        }
        val writer = StringWriter()
        val printer = PrintWriter(writer)
        throwable.printStackTrace(printer)
        printer.flush()
        return writer.toString()
    }
}