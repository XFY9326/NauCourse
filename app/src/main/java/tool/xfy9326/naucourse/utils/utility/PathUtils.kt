package tool.xfy9326.naucourse.utils.utility

import android.os.Environment
import androidx.core.content.ContextCompat
import tool.xfy9326.naucourse.App
import tool.xfy9326.naucourse.constants.NetworkConst
import java.io.File

object PathUtils {
    fun getUrlFileName(source: String): String? =
        if (NetworkConst.DIR in source) {
            var name = source.substring(source.lastIndexOf(NetworkConst.DIR) + 1)
            if (name.contains(NetworkConst.URL_QUERY_DIVIDE_SYMBOL)) {
                name = name.substring(0, name.lastIndexOf(NetworkConst.URL_QUERY_DIVIDE_SYMBOL))
            }
            if (name.isNotBlank() && name.isNotEmpty()) {
                name
            } else {
                null
            }
        } else {
            null
        }

    fun getImageLocalSavePath(dirName: String? = null): String? {
        val picturePath = ContextCompat.getExternalFilesDirs(App.instance, Environment.DIRECTORY_PICTURES)[0]?.absolutePath
        return if (picturePath != null) {
            picturePath + if (dirName == null) {
                File.separator
            } else {
                File.separator + dirName + File.separator
            }
        } else {
            null
        }
    }
}