package tool.xfy9326.naucourse.ui.views.table

data class CourseTableStyle(
    val sameCellHeight: Boolean,
    val bottomCornerCompat: Boolean,
    val centerHorizontalShowCourseText: Boolean,
    val centerVerticalShowCourseText: Boolean,
    val useRoundCornerCourseCell: Boolean,
    val drawAllCellBackground: Boolean,
    val forceShowCourseTableWeekends: Boolean,
    val customCourseTableBackground: Boolean,
    val customCourseTableAlpha: Float,
    val showNotThisWeekCourseInTable: Boolean,
    val enableCourseTableTimeTextColor: Boolean,
    val courseTableTimeTextColor: Int,
    val highLightCourseTableTodayDate: Boolean,
    val courseCellTextSize: Float
)