package tool.xfy9326.naucourses.tools

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

// 针对只消费一次的事件，防止View重建时二次传递数值
class SingleLiveData<T> : MutableLiveData<SingleLiveData.Event<T>> {

    constructor() : super()

    constructor(value: T) : super(Event(value))

    fun postSingleValue(value: T) {
        postValue(Event(value))
    }

    fun observeSingle(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, Observer {
            it.getContentIfNotHandled()?.let { value ->
                observer.onChanged(value)
            }
        })
    }

    fun observeSingleForever(observer: Observer<in T>) {
        super.observeForever {
            it.getContentIfNotHandled()?.let { value ->
                observer.onChanged(value)
            }
        }
    }

    class Event<out T>(private val content: T) {

        var hasBeenHandled = false
            private set

        fun getContentIfNotHandled(): T? {
            return if (hasBeenHandled) {
                null
            } else {
                hasBeenHandled = true
                content
            }
        }

        fun peekContent(): T = content
    }
}