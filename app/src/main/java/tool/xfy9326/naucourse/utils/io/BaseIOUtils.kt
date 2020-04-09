package tool.xfy9326.naucourse.utils.io

import java.io.File

object BaseIOUtils {
    fun deleteFile(path: String) = File(path).deleteRecursively()

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