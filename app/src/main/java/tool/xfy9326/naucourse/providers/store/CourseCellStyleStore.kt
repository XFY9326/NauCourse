package tool.xfy9326.naucourse.providers.store

import tool.xfy9326.naucourse.beans.CourseCellStyle
import tool.xfy9326.naucourse.providers.beans.jwc.CourseSet
import tool.xfy9326.naucourse.providers.store.base.BaseJsonStore
import tool.xfy9326.naucourse.utils.courses.CourseStyleUtils

object CourseCellStyleStore : BaseJsonStore<Array<CourseCellStyle>>() {
    override val fileName: String = "CourseCellStyle"
    override val versionCode: Int = 1
    override val storeClass: Class<Array<CourseCellStyle>> = Array<CourseCellStyle>::class.java

    override val useCache: Boolean = true
    override val useEncrypt: Boolean = false

    @Volatile
    private var styleTemp: Pair<CourseSet, Array<CourseCellStyle>>? = null

    @Synchronized
    fun loadCellStyles(courseSet: CourseSet): Array<CourseCellStyle> {
        if (styleTemp != null) {
            if (styleTemp?.first == courseSet) {
                return styleTemp?.second!!
            }
        }
        val storedStyles = loadStore()
        val styles = CourseStyleUtils.asyncCellStyle(courseSet, storedStyles)
        styleTemp = Pair(courseSet, styles)
        return styles
    }
}