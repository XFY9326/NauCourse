package tool.xfy9326.naucourses.tools

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer

// 用于只消费一次的通知
class NotifyLivaData : EventLiveData<Unit>() {
    fun notifyEvent() = postEventValue(Unit)

    fun observeNotification(owner: LifecycleOwner, observer: () -> Unit, tag: String? = null) {
        super.observeEvent(owner, Observer {
            observer.invoke()
        }, tag)
    }

    fun observeNotificationForever(observer: () -> Unit, tag: String? = null) =
        super.observeEventForever(Observer {
            observer.invoke()
        }, tag)
}