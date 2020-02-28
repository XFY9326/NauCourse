package tool.xfy9326.naucourses.providers.store

import tool.xfy9326.naucourses.beans.CourseTable
import tool.xfy9326.naucourses.io.json.GsonStoreType
import tool.xfy9326.naucourses.providers.store.base.BaseGsonStore

object CourseTableStore : BaseGsonStore<Array<CourseTable>>() {
    override val useCache: Boolean = true
    override val useEncrypt: Boolean = false
    override val storeType: GsonStoreType = GsonStoreType.COURSE_TABLE
}