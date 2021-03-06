package tool.xfy9326.naucourse.ui.views.table

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.databinding.ViewCourseTableDateBinding
import tool.xfy9326.naucourse.databinding.ViewCourseTableMonthBinding
import tool.xfy9326.naucourse.ui.views.widgets.AdvancedLinearLayout
import tool.xfy9326.naucourse.utils.courses.TimeUtils

class CourseTableHeaderView : AdvancedLinearLayout {
    companion object {
        fun create(
            context: Context,
            weekDaySize: Int,
            dateInfo: Array<Pair<Int, Int>>,
            courseTableStyle: CourseTableStyle
        ) = CourseTableHeaderView(context).apply {
            setHeaderData(weekDaySize, dateInfo, courseTableStyle)
        }
    }

    private val courseCellPaddingHalf = resources.getDimensionPixelSize(R.dimen.course_cell_padding_half)
    private val courseHeaderMinHeight = resources.getDimensionPixelSize(R.dimen.course_table_course_time_row_size)

    private val weekDayNumStrArr = resources.getStringArray(R.array.weekday_num)
    private val highLightTextColor = ContextCompat.getColor(context, R.color.colorCourseTimeHighLight)
    private val highLightTextColorBackground = ContextCompat.getColor(context, R.color.colorCourseTimeHighLightBackground)
    private val otherCourseCellBackground = ContextCompat.getColor(context, R.color.colorOtherCourseCellBackground)

    private val defaultCourseCellBackgroundRadius = resources.getDimension(R.dimen.course_cell_background_radius)
    private val defaultCourseTimeTextColor = ContextCompat.getColor(context, R.color.colorCourseTimeDefault)

    private val layoutInflater = LayoutInflater.from(context)

    private constructor(context: Context) : this(context, null)

    private constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    private constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        initView()
    }

    private fun initView() {
        layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            marginEnd = courseCellPaddingHalf
        }
        gravity = Gravity.CENTER_VERTICAL
        minimumHeight = courseHeaderMinHeight
    }

    fun setHeaderData(weekDaySize: Int, dateInfo: Array<Pair<Int, Int>>, courseTableStyle: CourseTableStyle) {
        if (childCount > 0) {
            removeAllViews()
        }

        val today = TimeUtils.getTodayDate()

        addViewInLayout(getMonthView(dateInfo, courseTableStyle))

        for (i in 0 until weekDaySize) {
            addViewInLayout(getDateView(i, today, dateInfo, courseTableStyle))
        }

        refreshLayout()
    }

    private fun getMonthView(dateInfo: Array<Pair<Int, Int>>, courseTableStyle: CourseTableStyle) =
        ViewCourseTableMonthBinding.inflate(layoutInflater, this, false).apply {
            tvCellMonth.apply {
                text = context.getString(R.string.month, dateInfo.first().first)
                alpha = courseTableStyle.customCourseTableAlpha
                setTextColor(getCourseTimeTextColor(courseTableStyle))
                background = if (courseTableStyle.drawAllCellBackground) {
                    CourseTableViewHelper.buildRadiusDrawable(otherCourseCellBackground, getBackgroundRadius(courseTableStyle))
                } else {
                    null
                }
            }
        }.root

    private fun getDateView(
        weekDayNum: Int,
        today: Pair<Int, Int>,
        dateInfo: Array<Pair<Int, Int>>,
        courseTableStyle: CourseTableStyle
    ) =
        ViewCourseTableDateBinding.inflate(layoutInflater, this, false).apply {
            val isToday = if (courseTableStyle.highLightCourseTableTodayDate) {
                dateInfo[weekDayNum] == today
            } else {
                false
            }

            alpha = courseTableStyle.customCourseTableAlpha
            tvCellWeekdayNum.text = weekDayNumStrArr[weekDayNum]
            tvCellDateNum.text = dateInfo[weekDayNum].second.toString()
            if (isToday) {
                tvCellWeekdayNum.setTextColor(highLightTextColor)
                tvCellDateNum.setTextColor(highLightTextColor)
                tvCellWeekdayNum.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                tvCellDateNum.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                background = CourseTableViewHelper.buildRadiusDrawable(
                    highLightTextColorBackground,
                    context.resources.getDimensionPixelSize(R.dimen.course_date_highlight_background_radius).toFloat()
                )
            } else {
                tvCellWeekdayNum.setTextColor(getCourseTimeTextColor(courseTableStyle))
                tvCellDateNum.setTextColor(getCourseTimeTextColor(courseTableStyle))
                tvCellWeekdayNum.typeface = Typeface.DEFAULT
                tvCellDateNum.typeface = Typeface.DEFAULT
                background = if (courseTableStyle.drawAllCellBackground) {
                    CourseTableViewHelper.buildRadiusDrawable(otherCourseCellBackground, getBackgroundRadius(courseTableStyle))
                } else {
                    null
                }
            }
        }.root

    private fun getBackgroundRadius(courseTableStyle: CourseTableStyle) =
        if (courseTableStyle.useRoundCornerCourseCell) {
            defaultCourseCellBackgroundRadius
        } else {
            0f
        }

    private fun getCourseTimeTextColor(courseTableStyle: CourseTableStyle) =
        if (courseTableStyle.enableCourseTableTimeTextColor) {
            courseTableStyle.courseTableTimeTextColor
        } else {
            defaultCourseTimeTextColor
        }
}