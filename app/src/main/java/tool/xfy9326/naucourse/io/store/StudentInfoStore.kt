package tool.xfy9326.naucourse.io.store

import tool.xfy9326.naucourse.io.store.base.BaseJsonStore
import tool.xfy9326.naucourse.providers.beans.jwc.StudentInfo

object StudentInfoStore : BaseJsonStore<StudentInfo>() {
    override val fileName: String = "StudentInfo"
    override val versionCode: Int = 1
    override val storeClass: Class<StudentInfo> = StudentInfo::class.java

    override val useCache: Boolean = true
    override val useEncrypt: Boolean = true
}