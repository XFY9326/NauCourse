package tool.xfy9326.naucourse.providers.store

import tool.xfy9326.naucourse.beans.CourseTable
import tool.xfy9326.naucourse.io.gson.GsonStoreType
import tool.xfy9326.naucourse.providers.store.base.BaseGsonStore

object CourseTableStore : BaseGsonStore<Array<CourseTable>>() {
    override val useCache: Boolean = true
    override val useEncrypt: Boolean = false
    override val storeType: GsonStoreType = GsonStoreType.COURSE_TABLE
}