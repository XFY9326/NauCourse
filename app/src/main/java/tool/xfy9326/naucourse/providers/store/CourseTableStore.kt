package tool.xfy9326.naucourse.providers.store

import tool.xfy9326.naucourse.beans.CourseTable
import tool.xfy9326.naucourse.providers.store.base.BaseJsonStore

object CourseTableStore : BaseJsonStore<Array<CourseTable>>() {
    override val fileName: String = "CourseTable"
    override val versionCode: Int = 1
    override val storeClass: Class<Array<CourseTable>> = Array<CourseTable>::class.java

    override val useCache: Boolean = true
    override val useEncrypt: Boolean = false
}