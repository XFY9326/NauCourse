package tool.xfy9326.naucourses.tools

import java.util.*

class Event<out T>(private val content: T) {
    private var hasNullBeenHandled = false

    // 同一tag只会被消费一次
    private var hasBeenHandled = Hashtable<String, Boolean>()

    @Synchronized
    fun getContentIfNotHandled(tag: String?): Container<out T>? {
        return if (tag == null) {
            if (hasNullBeenHandled) {
                null
            } else {
                hasNullBeenHandled = true
                Container(content)
            }
        } else {
            if (hasBeenHandled[tag] == true) {
                null
            } else {
                hasBeenHandled[tag] = true
                Container(content)
            }
        }
    }

    // 防止需要返回null时被当作已经处理
    data class Container<T>(val data: T)
}