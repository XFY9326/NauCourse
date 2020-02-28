package tool.xfy9326.naucourses.io.json

import android.content.Context
import com.google.gson.Gson
import tool.xfy9326.naucourses.App
import tool.xfy9326.naucourses.beans.CourseCellStyle
import tool.xfy9326.naucourses.beans.CourseTable
import tool.xfy9326.naucourses.beans.UUIDContent
import tool.xfy9326.naucourses.io.prefs.GsonStoreVersionPref
import tool.xfy9326.naucourses.providers.beans.jwc.StudentInfo
import tool.xfy9326.naucourses.utils.IOUtils
import java.io.File

class GsonStoreManager private constructor(context: Context) {
    private var jsonFilePath: String = context.noBackupFilesDir.absolutePath + File.separator + GSON_FILE_DIR_NAME + File.separator

    enum class StoreType {
        STUDENT_INFO {
            override val versionCode: Int = 1
            override val storeClass: Class<*> = StudentInfo::class.java
        },
        COURSE_TABLE {
            override val versionCode: Int = 1
            override val storeClass: Class<*> = Array<CourseTable>::class.java
        },
        COURSE_STYLE {
            override val versionCode: Int = 1
            override val storeClass: Class<*> = Array<CourseCellStyle>::class.java
        },
        UUID {
            override val versionCode: Int = 1
            override val storeClass: Class<*> = UUIDContent::class.java
        };

        abstract val storeClass: Class<*>
        abstract val versionCode: Int
    }

    companion object {
        private const val GSON_FILE_DIR_NAME = "GsonStore"

        @Volatile
        private lateinit var instance: GsonStoreManager

        fun getInstance(): GsonStoreManager = synchronized(this) {
            if (!::instance.isInitialized) {
                instance = GsonStoreManager(App.instance)
            }
            instance
        }
    }

    private fun getStoredPath(storeType: StoreType) = jsonFilePath + storeType.name

    fun clearData(storeType: StoreType) = synchronized(storeType) {
        IOUtils.deleteFile(getStoredPath(storeType))
    }

    fun writeData(storeType: StoreType, data: Any, encrypt: Boolean = false): Boolean = synchronized(storeType) {
        GsonStoreVersionPref.saveStoredVersion(storeType.name, storeType.versionCode)
        IOUtils.saveTextToFile(Gson().toJson(data), getStoredPath(storeType), encrypt, true)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> readData(storeType: StoreType, encrypt: Boolean = false): T? = synchronized(storeType) {
        if (GsonStoreVersionPref.loadStoredVersion(storeType.name) != storeType.versionCode) {
            clearData(storeType)
            null
        } else {
            val text = IOUtils.readTextFromFile(getStoredPath(storeType), encrypt, true)
            return if (text == null || text.isEmpty()) {
                null
            } else {
                Gson().fromJson(text, storeType.storeClass) as T
            }
        }
    }
}