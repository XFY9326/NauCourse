package tool.xfy9326.naucourse.ui.views.table

object CourseTableInternalStyle {
    data class EmptyView(
        val backgroundColor: Int,
        val radius: Float,
        val padding: Int
    )

    data class TimeCellView(
        val width: Int,
        val backgroundColor: Int,
        val radius: Float,
        val verticalPadding: Int,
        val textColor: Int,
        val timeNumTextSize: Float,
        val timeTextSize: Float,
        val padding: Int,
        val timeTextPaddingTop: Int
    )

    data class CourseCellView(
        val radius: Float,
        val padding: Int,
        val textPadding: Int,
        val darkTextColor: Int,
        val lightTextColor: Int,
        val notThisWeekCourseColor: Int
    )
}