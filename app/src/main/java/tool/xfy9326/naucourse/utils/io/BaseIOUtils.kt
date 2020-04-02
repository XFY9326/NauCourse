package tool.xfy9326.naucourse.utils.io

import java.io.File

object BaseIOUtils {
    fun deleteFile(path: String) = File(path).deleteRecursively()

    fun prepareFile(file: File): Boolean {
        if (file.exists() || file.mkdirs()) {
            if (file.exists() && !file.delete()) {
                return false
            }
            return true
        }
        return false
    }
}