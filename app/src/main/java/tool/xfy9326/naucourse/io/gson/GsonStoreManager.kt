package tool.xfy9326.naucourse.io.gson

import com.google.gson.Gson
import tool.xfy9326.naucourse.App
import tool.xfy9326.naucourse.io.prefs.GsonStoreVersionPref
import tool.xfy9326.naucourse.utils.io.BaseIOUtils
import tool.xfy9326.naucourse.utils.io.TextIOUtils
import java.io.File

object GsonStoreManager {
    private const val GSON_FILE_DIR_NAME = "GsonStore"
    private const val GSON_FILE_PREFIX = ".gsf"

    private var jsonFilePath: String = App.instance.filesDir.absolutePath + File.separator + GSON_FILE_DIR_NAME + File.separator

    private fun getStoredPath(storeType: GsonStoreType) = jsonFilePath + storeType.name + GSON_FILE_PREFIX

    fun clearData(storeType: GsonStoreType) = synchronized(storeType) {
        BaseIOUtils.deleteFile(getStoredPath(storeType))
    }

    fun writeData(storeType: GsonStoreType, data: Any, encrypt: Boolean = false): Boolean = synchronized(storeType) {
        GsonStoreVersionPref.saveStoredVersion(storeType.name, storeType.versionCode)
        TextIOUtils.saveTextToFile(Gson().toJson(data), getStoredPath(storeType), encrypt, true)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> readData(storeType: GsonStoreType, encrypt: Boolean = false): T? = synchronized(storeType) {
        if (GsonStoreVersionPref.loadStoredVersion(storeType.name) != storeType.versionCode) {
            clearData(storeType)
            null
        } else {
            val text = TextIOUtils.readTextFromFile(getStoredPath(storeType), encrypt, true)
            return if (text == null || text.isEmpty()) {
                null
            } else {
                Gson().fromJson(text, storeType.storeClass) as T
            }
        }
    }
}