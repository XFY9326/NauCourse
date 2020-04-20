package tool.xfy9326.naucourse.io.store

import tool.xfy9326.naucourse.io.store.base.BaseJsonStore
import tool.xfy9326.naucourse.providers.beans.jwc.SuspendCourse

object SuspendCourseStore : BaseJsonStore<Array<SuspendCourse>>() {
    override val fileName: String = "SuspendCourse"
    override val versionCode: Int = 1
    override val storeClass: Class<Array<SuspendCourse>> = Array<SuspendCourse>::class.java

    override val useCache: Boolean = true
    override val useEncrypt: Boolean = true
}