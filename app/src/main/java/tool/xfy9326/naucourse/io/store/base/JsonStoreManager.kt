package tool.xfy9326.naucourse.io.store.base

import com.google.gson.Gson
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tool.xfy9326.naucourse.App
import tool.xfy9326.naucourse.io.prefs.JsonStoreVersionPref
import tool.xfy9326.naucourse.utils.debug.ExceptionUtils
import tool.xfy9326.naucourse.utils.io.BaseIOUtils
import tool.xfy9326.naucourse.utils.io.TextIOUtils
import java.io.File

object JsonStoreManager {
    private const val JSON_FILE_DIR_NAME = "JsonStore"
    private const val JSON_FILE_PREFIX = ".sjf"
    private val jsonMutex = Mutex()

    private var jsonFilePath: String = App.instance.filesDir.absolutePath + File.separator + JSON_FILE_DIR_NAME + File.separator

    private fun getStoredPath(fileName: String) = jsonFilePath + fileName + JSON_FILE_PREFIX

    fun clearData(config: JsonStoreConfig<*>) = synchronized(config) {
        BaseIOUtils.deleteFile(getStoredPath(config.fileName))
    }

    suspend fun <T : Any> writeData(config: JsonStoreConfig<T>, data: T, encrypt: Boolean = false): Boolean = jsonMutex.withLock {
        JsonStoreVersionPref.saveStoredVersion(config.fileName, config.versionCode)
        try {
            TextIOUtils.saveTextToFile(
                convertToJson(data),
                getStoredPath(config.fileName), encrypt, true
            )
        } catch (e: Exception) {
            ExceptionUtils.printStackTrace<JsonStoreManager>(e)
            false
        }
    }

    suspend fun <T : Any> readData(config: JsonStoreConfig<T>, encrypt: Boolean = false): T? = jsonMutex.withLock {
        if (JsonStoreVersionPref.loadStoredVersion(config.fileName) != config.versionCode) {
            clearData(config)
            null
        } else {
            val text = TextIOUtils.readTextFromFile(getStoredPath(config.fileName), encrypt, true)
            return if (text == null || text.isEmpty()) {
                null
            } else {
                try {
                    convertFromJson(config.storeClass, text)
                } catch (e: Exception) {
                    ExceptionUtils.printStackTrace<JsonStoreManager>(e)
                    null
                }
            }
        }
    }

    private fun <T : Any> convertToJson(data: T) = Gson().toJson(data)

    private fun <T : Any> convertFromJson(storeClass: Class<T>, text: String) = Gson().fromJson(text, storeClass)
}