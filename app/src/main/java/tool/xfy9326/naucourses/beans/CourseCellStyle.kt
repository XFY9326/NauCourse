package tool.xfy9326.naucourses.beans

import tool.xfy9326.naucourses.providers.beans.jwc.CourseSet
import tool.xfy9326.naucourses.providers.store.CourseCellStyleStore
import tool.xfy9326.naucourses.utils.debug.LogUtils
import tool.xfy9326.naucourses.utils.views.ColorUtils
import java.io.Serializable

data class CourseCellStyle(
    val courseId: String,
    var color: Int,
    val textSize: Float
) : Serializable {
    companion object {
        private const val DEFAULT_COURSE_CELL_TEXT_SIZE = 13f

        fun getDefaultCellStyle(courseId: String): CourseCellStyle {
            LogUtils.d<CourseCellStyle>("Creating New Course Style For ID: $courseId")
            return CourseCellStyle(
                courseId,
                ColorUtils.getRandomMaterialColor(),
                DEFAULT_COURSE_CELL_TEXT_SIZE
            )
        }

        fun getStyleByCourseId(
            courseId: String, styles: Array<CourseCellStyle>, createNewWhileNotFound: Boolean = false,
            saveCreateStyle: Boolean = true, copy: Boolean = false
        ): CourseCellStyle? {
            for (style in styles) {
                if (style.courseId == courseId) {
                    return if (copy) style.copy() else style
                }
            }
            return if (createNewWhileNotFound) {
                val createStyle = getDefaultCellStyle(courseId)
                if (saveCreateStyle) {
                    val newStyle = styles.copyOf(styles.size + 1)
                    newStyle[newStyle.size - 1] = createStyle
                    CourseCellStyleStore.saveStore(newStyle.requireNoNulls())
                    newStyle[newStyle.size - 1]
                } else {
                    createStyle
                }
            } else {
                null
            }
        }

        @Synchronized
        fun asyncCellStyle(courseSet: CourseSet, styleMap: Array<CourseCellStyle>? = null, saveStyle: Boolean = true): Array<CourseCellStyle> {
            val oldStyles = styleMap ?: emptyArray()
            val newStyles = ArrayList<CourseCellStyle>(courseSet.courses.size)
            for (course in courseSet.courses) {
                newStyles.add(getStyleByCourseId(course.id, oldStyles, createNewWhileNotFound = true, saveCreateStyle = false)!!)
            }
            val result = newStyles.toTypedArray()
            if (saveStyle) {
                CourseCellStyleStore.saveStore(result)
            }
            return result
        }
    }
}