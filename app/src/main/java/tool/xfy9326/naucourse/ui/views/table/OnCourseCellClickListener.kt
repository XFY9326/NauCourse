package tool.xfy9326.naucourse.ui.views.table

import tool.xfy9326.naucourse.beans.CourseCell
import tool.xfy9326.naucourse.beans.CourseCellStyle

interface OnCourseCellClickListener {
    fun onCourseCellClick(courseCell: CourseCell, cellStyle: CourseCellStyle)
}