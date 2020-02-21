package tool.xfy9326.naucourses.providers.cache

import tool.xfy9326.naucourses.beans.CourseTable
import tool.xfy9326.naucourses.io.json.GsonStoreManager

object CourseTableCache : BaseCache<Array<CourseTable>>() {
    override val cacheType: GsonStoreManager.StoreType = GsonStoreManager.StoreType.COURSE_TABLE
}