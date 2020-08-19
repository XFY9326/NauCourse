package tool.xfy9326.naucourse.tools.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

// 用于只消费一次的通知
@Suppress("unused")
class NotifyLivaData {
    private val liveData = EventLiveData<Unit>()

    fun notifyEvent() = liveData.postEventValue(Unit)

    fun removeObserver(observer: Observer<Event<Unit>>) = liveData.removeObserver(observer)

    fun removeObservers(owner: LifecycleOwner) = liveData.removeObservers(owner)

    fun observeNotification(owner: LifecycleOwner, tag: String? = null, observer: () -> Unit) =
        liveData.observeEvent(owner, tag) {
            observer.invoke()
        }

    fun observeNotificationForever(tag: String? = null, observer: () -> Unit) =
        liveData.observeEventForever(tag) {
            observer.invoke()
        }
}