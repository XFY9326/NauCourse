package tool.xfy9326.naucourse.utils.io

import java.io.File

object BaseIOUtils {
    fun deleteFile(path: String): Boolean {
        val file = File(path)
        return if (file.exists()) {
            if (file.isDirectory) {
                var result = true
                file.listFiles()?.forEach {
                    if (!deleteFile(it.absolutePath)) result = false
                }
                result
            } else {
                file.delete()
            }
        } else {
            false
        }
    }
}