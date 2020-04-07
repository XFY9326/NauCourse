package tool.xfy9326.naucourse.io.store.base

import com.google.gson.Gson
import tool.xfy9326.naucourse.App
import tool.xfy9326.naucourse.io.prefs.JsonStoreVersionPref
import tool.xfy9326.naucourse.utils.io.BaseIOUtils
import tool.xfy9326.naucourse.utils.io.TextIOUtils
import java.io.File

object JsonStoreManager {
    private const val JSON_FILE_DIR_NAME = "JsonStore"
    private const val JSON_FILE_PREFIX = ".sjf"

    private var jsonFilePath: String = App.instance.filesDir.absolutePath + File.separator + JSON_FILE_DIR_NAME + File.separator

    private fun getStoredPath(fileName: String) = jsonFilePath + fileName + JSON_FILE_PREFIX

    fun clearData(config: JsonStoreConfig<*>) = synchronized(config) {
        BaseIOUtils.deleteFile(getStoredPath(config.fileName))
    }

    fun <T : Any> writeData(config: JsonStoreConfig<T>, data: T, encrypt: Boolean = false): Boolean = synchronized(config) {
        JsonStoreVersionPref.saveStoredVersion(config.fileName, config.versionCode)
        TextIOUtils.saveTextToFile(
            convertToJson(data),
            getStoredPath(config.fileName), encrypt, true
        )
    }

    fun <T : Any> readData(config: JsonStoreConfig<T>, encrypt: Boolean = false): T? = synchronized(config) {
        if (JsonStoreVersionPref.loadStoredVersion(config.fileName) != config.versionCode) {
            clearData(config)
            null
        } else {
            val text = TextIOUtils.readTextFromFile(getStoredPath(config.fileName), encrypt, true)
            return if (text == null || text.isEmpty()) {
                null
            } else {
                convertFromJson(config.storeClass, text)
            }
        }
    }

    private fun <T : Any> convertToJson(data: T) = Gson().toJson(data)

    private fun <T : Any> convertFromJson(storeClass: Class<T>, text: String) = Gson().fromJson(text, storeClass)
}