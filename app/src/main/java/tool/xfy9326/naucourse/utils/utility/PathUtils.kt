package tool.xfy9326.naucourse.utils.utility

import android.os.Environment
import androidx.core.content.ContextCompat
import tool.xfy9326.naucourse.App
import tool.xfy9326.naucourse.Constants
import java.io.File

object PathUtils {
    fun getUrlFileName(source: String): String? =
        if (Constants.Network.DIR in source) {
            val name = source.substring(source.lastIndexOf(Constants.Network.DIR) + 1).trim()
            if (name.isNotBlank() && name.isNotEmpty()) {
                name
            } else {
                null
            }
        } else {
            null
        }

    fun getImageLocalSavePath(dirName: String? = null) =
        ContextCompat.getExternalFilesDirs(App.instance, Environment.DIRECTORY_PICTURES)[0].absolutePath +
                if (dirName == null) {
                    File.separator
                } else {
                    File.separator + dirName + File.separator
                }
}