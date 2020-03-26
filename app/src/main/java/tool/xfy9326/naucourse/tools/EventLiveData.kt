package tool.xfy9326.naucourse.tools

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

// 用于只消费一次的事件，主要用于防止View重建时二次传递脏数据
@Suppress("unused")
open class EventLiveData<T> : MutableLiveData<Event<T>> {
    constructor() : super()

    constructor(value: T) : super(Event(value))

    fun postEventValue(value: T) {
        super.postValue(Event(value))
    }

    fun setEventValue(value: T) {
        super.setValue(Event(value))
    }

    fun observeEvent(owner: LifecycleOwner, observer: Observer<in T>, tag: String? = null) {
        super.observe(owner, Observer {
            it.getContentIfNotHandled(tag)?.let { value ->
                observer.onChanged(value.data)
            }
        })
    }

    fun observeEventForever(observer: Observer<in T>, tag: String? = null) =
        Observer<Event<T>> {
            it.getContentIfNotHandled(tag)?.let { value ->
                observer.onChanged(value.data)
            }
        }.also {
            super.observeForever(it)
        }
}