package tool.xfy9326.naucourse.utils.debug

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tool.xfy9326.naucourse.App
import tool.xfy9326.naucourse.Constants
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object DebugIOUtils {
    private val DATE_FORMAT_YMD = SimpleDateFormat(Constants.Time.FORMAT_YMD, Locale.CHINA)
    private val DATE_FORMAT_YMD_HM_CH = SimpleDateFormat(Constants.Time.FORMAT_YMD_HM_CH, Locale.CHINA)

    private const val DEBUG_DIR = "Debug_Log"
    private const val FORCE_LOG_ON_FLAG = "debug_on"
    private const val DEBUG_LOG_FILE_PREFIX = ".log"

    private const val DEBUG_EXCEPTION_PREFIX = "Exception_"
    private const val DEBUG_ERROR_PREFIX = "Error_"
    private const val DEBUG_LOG_PREFIX = "Log_"

    private val DEBUG_LOG_SAVE_DIR = App.instance.getExternalFilesDir(DEBUG_DIR)

    val FORCE_DEBUG_ON = File(DEBUG_LOG_SAVE_DIR, FORCE_LOG_ON_FLAG).exists()

    enum class DebugSaveType {
        EXCEPTION {
            override val frontPrefix = DEBUG_EXCEPTION_PREFIX
        },
        ERROR {
            override val frontPrefix = DEBUG_ERROR_PREFIX
        },
        LOG {
            override val frontPrefix = DEBUG_LOG_PREFIX
        };

        abstract val frontPrefix: String
    }

    private fun getLogDate() = DATE_FORMAT_YMD.format(Date())

    private fun getDivider() = "\n\n=================== ${DATE_FORMAT_YMD_HM_CH.format(Date())} ======================\n\n"

    private fun getSaveFile(type: DebugSaveType) = File(DEBUG_LOG_SAVE_DIR, type.frontPrefix + getLogDate() + DEBUG_LOG_FILE_PREFIX)

    @Synchronized
    fun clearLogs() = if (DEBUG_LOG_SAVE_DIR?.exists() == true) DEBUG_LOG_SAVE_DIR.deleteRecursively() else true

    fun append(type: DebugSaveType, msg: String) =
        GlobalScope.launch(Dispatchers.IO) {
            writeFile(type, msg)
        }

    fun appendAsync(type: DebugSaveType, msg: String) = writeFile(type, msg)

    private fun writeFile(type: DebugSaveType, msg: String) =
        synchronized(this) {
            try {
                FileWriter(getSaveFile(type), true).use {
                    if (type != DebugSaveType.LOG) {
                        it.write(getDivider())
                    }
                    it.write(msg)
                    if (type == DebugSaveType.LOG) {
                        it.write(Constants.CHANGE_LINE)
                    }
                    it.flush()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
}