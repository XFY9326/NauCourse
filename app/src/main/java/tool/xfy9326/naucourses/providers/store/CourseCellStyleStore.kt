package tool.xfy9326.naucourses.providers.store

import tool.xfy9326.naucourses.beans.CourseCellStyle
import tool.xfy9326.naucourses.io.json.GsonStoreManager
import tool.xfy9326.naucourses.providers.store.base.BaseGsonStore

object CourseCellStyleStore : BaseGsonStore<Array<CourseCellStyle>>() {
    override val useCache: Boolean = false
    override val storeType: GsonStoreManager.StoreType = GsonStoreManager.StoreType.COURSE_STYLE
}