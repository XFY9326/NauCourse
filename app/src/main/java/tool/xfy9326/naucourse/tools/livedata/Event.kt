package tool.xfy9326.naucourse.tools.livedata

import java.util.*

// manuallyCheckHandled 仅针对空tag有效
class Event<out T>(content: T, private val manuallyCheckHandled: Boolean = false) {
    private var container: Container<T>? = Container(content)
    private var hasNullTagBeenHandled = false

    // 同一tag只会被消费一次
    private val hasBeenHandled = Hashtable<String, Boolean>()

    @Synchronized
    fun getContentIfNotHandled(tag: String?): Container<out T>? {
        return if (container != null) {
            if (tag == null) {
                if (hasNullTagBeenHandled) {
                    null
                } else {
                    if (!manuallyCheckHandled) {
                        hasNullTagBeenHandled = true
                    }
                    container
                }
            } else {
                if (hasBeenHandled[tag] == true) {
                    null
                } else {
                    hasBeenHandled[tag] = true
                    container
                }
            }
        } else {
            null
        }
    }

    @Synchronized
    fun setNullTagHandled() {
        if (manuallyCheckHandled) {
            hasNullTagBeenHandled = true
            container = null
            hasBeenHandled.clear()
        }
    }

    fun peekContent(): T? = container?.data

    // 防止需要返回null时被当作已经处理
    data class Container<T>(val data: T)
}