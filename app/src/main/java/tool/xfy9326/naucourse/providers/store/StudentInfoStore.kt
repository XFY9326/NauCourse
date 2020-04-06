package tool.xfy9326.naucourse.providers.store

import tool.xfy9326.naucourse.providers.beans.jwc.StudentInfo
import tool.xfy9326.naucourse.providers.store.base.BaseJsonStore

object StudentInfoStore : BaseJsonStore<StudentInfo>() {
    override val fileName: String = "StudentInfo"
    override val versionCode: Int = 1
    override val storeClass: Class<StudentInfo> = StudentInfo::class.java

    override val useCache: Boolean = false
    override val useEncrypt: Boolean = true
}