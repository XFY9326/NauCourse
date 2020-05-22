package tool.xfy9326.naucourse.ui.views.table

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.graphics.applyCanvas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import tool.xfy9326.naucourse.beans.CoursePkg
import tool.xfy9326.naucourse.beans.CourseTable
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.utils.courses.CourseUtils
import tool.xfy9326.naucourse.utils.courses.TimeUtils
import tool.xfy9326.naucourse.utils.views.ColorUtils

object CourseTableViewHelper {
    fun getShowWeekDaySize(courseTable: CourseTable, courseTableStyle: CourseTableStyle) =
        getShowWeekDaySize(courseTableStyle.forceShowCourseTableWeekends || CourseUtils.hasWeekendCourse(courseTable))

    private fun getShowWeekDaySize(hasWeekendCourse: Boolean) =
        if (hasWeekendCourse) {
            CourseTableView.DEFAULT_TABLE_WIDTH_SIZE - 1
        } else {
            CourseTableView.DEFAULT_TABLE_WIDTH_SIZE - 3
        }

    fun getCourseTableStyle() =
        CourseTableStyle(
            SettingsPref.SameCourseCellHeight,
            SettingsPref.CourseTableRoundCompat,
            SettingsPref.CenterHorizontalShowCourseText,
            SettingsPref.CenterVerticalShowCourseText,
            SettingsPref.UseRoundCornerCourseCell,
            SettingsPref.DrawAllCellBackground,
            SettingsPref.ForceShowCourseTableWeekends,
            SettingsPref.CustomCourseTableBackground,
            SettingsPref.CustomCourseTableAlpha / 100f,
            SettingsPref.ShowNotThisWeekCourseInTable,
            SettingsPref.EnableCourseTableTimeTextColor,
            SettingsPref.CourseTableTimeTextColor,
            SettingsPref.HighLightCourseTableTodayDate,
            SettingsPref.CourseCellTextSize + 10f,
            SettingsPref.getNotThisWeekCourseShowType()
        )

    @SuppressLint("InflateParams")
    suspend fun drawCourseTableImage(
        context: Context,
        coursePkg: CoursePkg,
        weekNum: Int,
        targetWidth: Int
    ): Bitmap = withContext(Dispatchers.Default) {
        LinearLayoutCompat(context).let {
            it.orientation = LinearLayoutCompat.VERTICAL

            val compatStyle = coursePkg.courseTableStyle.copy(
                bottomCornerCompat = false,
                customCourseTableAlpha = 1f,
                enableCourseTableTimeTextColor = false,
                highLightCourseTableTodayDate = false
            )

            val backgroundColor = ColorUtils.getBackgroundColor(context)
            val showWeekDaySize = getShowWeekDaySize(coursePkg.courseTable, compatStyle)
            val columnSize = showWeekDaySize + 1
            val dateInfo = TimeUtils.getWeekNumDateArray(coursePkg.termDate, weekNum)

            val headerAsync = async {
                CourseTableHeaderView.create(context, showWeekDaySize, dateInfo, compatStyle)
            }

            val tableAsync = async {
                CourseTableView.create(context, coursePkg.copy(courseTableStyle = compatStyle), columnSize)
            }

            it.addView(headerAsync.await())
            it.addView(tableAsync.await())

            val widthSpec = View.MeasureSpec.makeMeasureSpec(targetWidth, View.MeasureSpec.AT_MOST)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            it.measure(widthSpec, heightSpec)
            it.layout(0, 0, it.measuredWidth, it.measuredHeight)
            it.requestLayout()

            Bitmap.createBitmap(it.width, it.height, Bitmap.Config.ARGB_8888).applyCanvas {
                drawColor(backgroundColor)
                it.draw(this)
            }
        }
    }

    fun buildRadiusDrawable(colorInt: Int, radius: Float): Drawable =
        GradientDrawable().apply {
            if (radius != 0f) cornerRadius = radius
            setColor(colorInt)
        }
}