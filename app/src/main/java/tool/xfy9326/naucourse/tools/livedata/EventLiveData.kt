package tool.xfy9326.naucourse.tools.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

// 用于只消费一次的事件，主要用于防止View重建时二次传递脏数据
@Suppress("unused")
class EventLiveData<T> {
    private val liveData: MutableLiveData<Event<T>>
    val value: T?
        get() = liveData.value?.peekContent()

    constructor() {
        liveData = MutableLiveData<Event<T>>()
    }

    constructor(value: T) {
        liveData = MutableLiveData(Event(value))
    }

    fun postEventValue(value: T) = liveData.postValue(Event(value))

    fun postCheckEventValue(value: T) = liveData.postValue(Event(value, true))

    fun setEventValue(value: T) {
        liveData.value = Event(value)
    }

    fun removeObserver(observer: Observer<Event<T>>) = liveData.removeObserver(observer)

    fun removeObservers(owner: LifecycleOwner) = liveData.removeObservers(owner)

    fun observeEvent(owner: LifecycleOwner, tag: String? = null, observer: (T) -> Unit) =
        Observer<Event<T>> {
            it.getContentIfNotHandled(tag)?.let { value ->
                observer.invoke(value.data)
            }
        }.also {
            liveData.observe(owner, it)
        }

    fun observeEventWithCheck(owner: LifecycleOwner, observer: (T) -> Boolean) =
        Observer<Event<T>> {
            it.getContentIfNotHandled(null)?.let { value ->
                if (observer.invoke(value.data)) it.setNullTagHandled()
            }
        }.also {
            liveData.observe(owner, it)
        }

    fun observeEventForever(tag: String? = null, observer: (T) -> Unit) =
        Observer<Event<T>> {
            it.getContentIfNotHandled(tag)?.let { value ->
                observer.invoke(value.data)
            }
        }.also {
            liveData.observeForever(it)
        }
}