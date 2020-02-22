package tool.xfy9326.naucourses.beans

import android.graphics.Color
import tool.xfy9326.naucourses.providers.store.CourseCellStyleStore
import tool.xfy9326.naucourses.utils.BaseUtils.dpToPx

data class CourseCellStyle(
    val courseId: String,
    val color: Int,
    val cornerRadius: Float,
    val alpha: Float,
    val textSize: Float
) {
    companion object {
        private val DEFAULT_COURSE_CELL_COLOR = Color.parseColor("#607D8B")
        private const val DEFAULT_COURSE_CELL_BACKGROUND_ALPHA = 0.6f
        private val DEFAULT_COURSE_CELL_BACKGROUND_RADIUS = 8f.dpToPx()
        private const val DEFAULT_COURSE_CELL_TEXT_SIZE = 13f

        fun getDefaultCellStyle(courseId: String): CourseCellStyle {
            return CourseCellStyle(
                courseId,
                DEFAULT_COURSE_CELL_COLOR,
                DEFAULT_COURSE_CELL_BACKGROUND_RADIUS,
                DEFAULT_COURSE_CELL_BACKGROUND_ALPHA,
                DEFAULT_COURSE_CELL_TEXT_SIZE
            )
        }

        fun getStyleByCourseId(courseId: String, styles: Array<CourseCellStyle>, createNewWhileNotFound: Boolean = false): CourseCellStyle? {
            for (style in styles) {
                if (style.courseId == courseId) {
                    return style
                }
            }
            return if (createNewWhileNotFound) {
                val newStyle = styles.copyOf(styles.size + 1)
                newStyle[newStyle.size - 1] = getDefaultCellStyle(courseId)
                CourseCellStyleStore.saveStore(newStyle.requireNoNulls())
                newStyle[newStyle.size - 1]
            } else {
                null
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CourseCellStyle

        if (courseId != other.courseId) return false

        return true
    }

    override fun hashCode(): Int {
        return courseId.hashCode()
    }
}