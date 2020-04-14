package tool.xfy9326.naucourse.tools.livedata

import java.util.*

class Event<out T>(private val content: T, private val manuallyCheckHandled: Boolean = false) {
    private var hasNullTagBeenHandled = false

    // 同一tag只会被消费一次
    private var hasBeenHandled = Hashtable<String, Boolean>()

    @Synchronized
    fun getContentIfNotHandled(tag: String?): Container<out T>? {
        return if (tag == null) {
            if (hasNullTagBeenHandled) {
                null
            } else {
                setNullTagHandled()
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

    @Synchronized
    fun setNullTagHandled() {
        if (!manuallyCheckHandled) hasNullTagBeenHandled = true
    }

    // 防止需要返回null时被当作已经处理
    data class Container<T>(val data: T)
}