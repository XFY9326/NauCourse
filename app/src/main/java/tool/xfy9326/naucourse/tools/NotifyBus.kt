package tool.xfy9326.naucourse.tools

import tool.xfy9326.naucourse.tools.livedata.NotifyLivaData
import java.util.*

object NotifyBus {
    private val liveDataMap = Hashtable<Type, NotifyLivaData>(Type.values().size)

    init {
        for (value in Type.values()) {
            liveDataMap[value] = NotifyLivaData()
        }
    }

    enum class Type {
        NIGHT_MODE_CHANGED,
        COURSE_TERM_UPDATE,
        COURSE_STYLE_TERM_UPDATE,
        REBUILD_COURSE_TABLE,
        REBUILD_COURSE_TABLE_BACKGROUND
    }

    operator fun get(type: Type) = liveDataMap[type]!!
}