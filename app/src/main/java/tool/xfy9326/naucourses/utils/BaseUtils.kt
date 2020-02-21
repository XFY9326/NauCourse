package tool.xfy9326.naucourses.utils

object BaseUtils {
    fun <T> HashSet<T>.getItemByHashCode(hashCode: Int): T? {
        forEach { if (it.hashCode() == hashCode) return it }
        return null
    }
}