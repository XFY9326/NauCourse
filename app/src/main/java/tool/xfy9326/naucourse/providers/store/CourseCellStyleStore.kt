package tool.xfy9326.naucourse.providers.store

import tool.xfy9326.naucourse.beans.CourseCellStyle
import tool.xfy9326.naucourse.io.gson.GsonStoreType
import tool.xfy9326.naucourse.providers.beans.jwc.CourseSet
import tool.xfy9326.naucourse.providers.store.base.BaseGsonStore

object CourseCellStyleStore : BaseGsonStore<Array<CourseCellStyle>>() {
    override val useCache: Boolean = true
    override val useEncrypt: Boolean = false
    override val storeType: GsonStoreType = GsonStoreType.COURSE_STYLE

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
        val styles = CourseCellStyle.asyncCellStyle(courseSet, storedStyles)
        styleTemp = Pair(courseSet, styles)
        return styles
    }
}