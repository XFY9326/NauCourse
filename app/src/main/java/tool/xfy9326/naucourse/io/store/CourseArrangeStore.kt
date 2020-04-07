package tool.xfy9326.naucourse.io.store

import tool.xfy9326.naucourse.beans.CourseArrange
import tool.xfy9326.naucourse.io.store.base.BaseJsonStore

object CourseArrangeStore : BaseJsonStore<CourseArrange>() {
    override val fileName: String = "CourseArrange"
    override val versionCode: Int = 1
    override val storeClass: Class<CourseArrange> = CourseArrange::class.java

    override val useCache: Boolean = true
    override val useEncrypt: Boolean = true
}