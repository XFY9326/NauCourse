package tool.xfy9326.naucourse.utils.io

import androidx.core.content.ContextCompat
import tool.xfy9326.naucourse.App
import java.io.File

object BaseIOUtils {
    fun deleteFile(path: String) = deleteFile(File(path))

    fun deleteExternalFiles(dirName: String) = deleteFile(ContextCompat.getExternalFilesDirs(App.instance, dirName)[0])

    fun deleteFile(file: File) = if (file.exists()) file.deleteRecursively() else true

    fun prepareFile(file: File, replaceFile: Boolean = true): Boolean {
        if (file.exists() || file.mkdirs()) {
            return if (replaceFile) {
                !file.exists() || file.delete()
            } else {
                true
            }
        }
        return false
    }
}