package tool.xfy9326.naucourses.providers.store

import tool.xfy9326.naucourses.beans.CourseCellStyle
import tool.xfy9326.naucourses.io.gson.GsonStoreType
import tool.xfy9326.naucourses.providers.beans.jwc.CourseSet
import tool.xfy9326.naucourses.providers.store.base.BaseGsonStore

object CourseCellStyleStore : BaseGsonStore<Array<CourseCellStyle>>() {
    override val useCache: Boolean = true
    override val useEncrypt: Boolean = false
    override val storeType: GsonStoreType = GsonStoreType.COURSE_STYLE

    fun loadCellStyles(courseSet: CourseSet): Array<CourseCellStyle> {
        val storedStyles = loadStore()
        return CourseCellStyle.asyncCellStyle(courseSet, storedStyles)
    }
}