package tool.xfy9326.naucourses.beans

import tool.xfy9326.naucourses.providers.beans.jwc.CourseSet
import tool.xfy9326.naucourses.providers.store.CourseCellStyleStore
import tool.xfy9326.naucourses.utils.views.ColorUtils
import java.io.Serializable

data class CourseCellStyle(
    val courseId: String,
    val color: Int,
    val textSize: Float
) : Serializable {
    companion object {
        private const val DEFAULT_COURSE_CELL_TEXT_SIZE = 13f

        private fun getDefaultCellStyle(courseId: String): CourseCellStyle {
            return CourseCellStyle(
                courseId,
                ColorUtils.getRandomMaterialColor(),
                DEFAULT_COURSE_CELL_TEXT_SIZE
            )
        }

        fun getStyleByCourseId(
            courseId: String, styles: Array<CourseCellStyle>, createNewWhileNotFound: Boolean = false,
            saveCreateStyle: Boolean = true
        ): CourseCellStyle? {
            for (style in styles) {
                if (style.courseId == courseId) {
                    return style
                }
            }
            return if (createNewWhileNotFound) {
                if (saveCreateStyle) {
                    val newStyle = styles.copyOf(styles.size + 1)
                    newStyle[newStyle.size - 1] = getDefaultCellStyle(courseId)
                    CourseCellStyleStore.saveStore(newStyle.requireNoNulls())
                    newStyle[newStyle.size - 1]
                } else {
                    getDefaultCellStyle(courseId)
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