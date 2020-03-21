package tool.xfy9326.naucourses.tools

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

// 针对只消费一次的事件，主要用于防止View重建时二次传递脏数据
class EventLiveData<T> : MutableLiveData<EventLiveData.Event<T>> {

    constructor() : super()

    constructor(value: T) : super(Event(value))

    fun postEventValue(value: T) {
        super.postValue(Event(value))
    }

    fun setEventValue(value: T) {
        super.setValue(Event(value))
    }

    fun observeEvent(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, Observer {
            it.getContentIfNotHandled()?.let { value ->
                observer.onChanged(value.data)
            }
        })
    }

    fun observeEventForever(observer: Observer<in T>) {
        super.observeForever {
            it.getContentIfNotHandled()?.let { value ->
                observer.onChanged(value.data)
            }
        }
    }

    class Event<out T>(private val content: T) {
        private var hasBeenHandled = false

        fun getContentIfNotHandled(): Container<out T>? {
            return if (hasBeenHandled) {
                null
            } else {
                hasBeenHandled = true
                Container(content)
            }
        }

        // 防止需要返回null时被当作已经处理
        data class Container<T>(val data: T)
    }
}