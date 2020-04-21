package tool.xfy9326.naucourse.utils.io

import java.io.File

object BaseIOUtils {
    fun deleteFile(path: String) = deleteFile(File(path))

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