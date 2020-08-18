package tool.xfy9326.naucourse.tools.livedata

import androidx.lifecycle.LifecycleOwner

// 用于只消费一次的通知
class NotifyLivaData : EventLiveData<Unit>() {
    fun notifyEvent() = postEventValue(Unit)

    fun observeNotification(owner: LifecycleOwner, observer: () -> Unit, tag: String? = null) {
        super.observeEvent(owner, {
            observer.invoke()
        }, tag)
    }

    fun observeNotificationForever(observer: () -> Unit, tag: String? = null) =
        super.observeEventForever({
            observer.invoke()
        }, tag)
}