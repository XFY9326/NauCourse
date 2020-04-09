package tool.xfy9326.naucourse.io.store

import tool.xfy9326.naucourse.beans.NextCourseBundle
import tool.xfy9326.naucourse.io.store.base.BaseJsonStore

object NextCourseBundleStore : BaseJsonStore<NextCourseBundle>() {
    override val fileName: String = "NextCourseBundle"
    override val versionCode: Int = 1
    override val storeClass: Class<NextCourseBundle> = NextCourseBundle::class.java

    override val useCache: Boolean = true
    override val useEncrypt: Boolean = true
}