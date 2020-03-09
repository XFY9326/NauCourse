package tool.xfy9326.naucourses.providers.store

import tool.xfy9326.naucourses.beans.CourseArrange
import tool.xfy9326.naucourses.io.json.GsonStoreType
import tool.xfy9326.naucourses.providers.store.base.BaseGsonStore

object CourseArrangeStore : BaseGsonStore<CourseArrange>() {
    override val useCache: Boolean = true
    override val useEncrypt: Boolean = true
    override val storeType: GsonStoreType = GsonStoreType.COURSE_ARRANGE
}