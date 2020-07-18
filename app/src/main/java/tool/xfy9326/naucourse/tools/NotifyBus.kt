package tool.xfy9326.naucourse.tools

import tool.xfy9326.naucourse.tools.livedata.NotifyLivaData
import java.util.*

object NotifyBus {
    private val liveDataMap = Hashtable<NotifyType, NotifyLivaData>(NotifyType.values().size)

    init {
        for (value in NotifyType.values()) {
            liveDataMap[value] = NotifyLivaData()
        }
    }

    operator fun get(notifyType: NotifyType) = liveDataMap[notifyType]!!
}