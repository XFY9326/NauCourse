package tool.xfy9326.naucourse.providers.store

import tool.xfy9326.naucourse.beans.CourseArrange
import tool.xfy9326.naucourse.io.gson.GsonStoreType
import tool.xfy9326.naucourse.providers.store.base.BaseGsonStore

object CourseArrangeStore : BaseGsonStore<CourseArrange>() {
    override val useCache: Boolean = true
    override val useEncrypt: Boolean = true
    override val storeType: GsonStoreType = GsonStoreType.COURSE_ARRANGE
}