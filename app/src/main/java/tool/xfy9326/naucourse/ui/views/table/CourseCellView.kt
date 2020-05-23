package tool.xfy9326.naucourse.ui.views.table

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.setPadding
import androidx.gridlayout.widget.GridLayout
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.beans.CourseCell
import tool.xfy9326.naucourse.beans.CourseCellStyle
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.ui.views.widgets.AdvancedFrameLayout
import tool.xfy9326.naucourse.ui.views.widgets.AdvancedLinearLayout
import tool.xfy9326.naucourse.utils.views.ColorUtils

class CourseCellView : AdvancedFrameLayout {
    companion object {
        private const val COURSE_INFO_JOIN_SYMBOL = "\n\n@"

        fun createAsEmptyCell(
            context: Context,
            col: Int,
            row: Int,
            internalStyle: CourseTableInternalStyle.EmptyView,
            courseTableStyle: CourseTableStyle
        ) = CourseCellView(context).apply {
            setCellAsEmptyCell(col, row, internalStyle, courseTableStyle)
        }

        fun createAsTimeCell(
            context: Context,
            row: Int,
            courseTimeStrArr: Array<Pair<String, String>>,
            internalStyle: CourseTableInternalStyle.TimeCellView,
            courseTableStyle: CourseTableStyle
        ) = CourseCellView(context).apply {
            setCellAsTimeCell(row, courseTimeStrArr, internalStyle, courseTableStyle)
        }

        fun createAsCourseCell(
            context: Context,
            col: Int,
            courseInfo: CourseCell,
            cellStyle: CourseCellStyle,
            internalStyle: CourseTableInternalStyle.CourseCellView,
            courseTableStyle: CourseTableStyle
        ) = CourseCellView(context).apply {
            setCellAsCourseCell(col, courseInfo, cellStyle, internalStyle, courseTableStyle)
        }
    }

    var row = 0
        private set
    var col = 0
        private set
    var rowSize = 1
        private set

    var courseCellType = CellType.EMPTY
        private set

    enum class CellType {
        EMPTY,
        TIME,
        COURSE
    }

    private var listener: ((CourseCell, CourseCellStyle) -> Unit)? = null

    private constructor(context: Context) : this(context, null)

    private constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    private constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    fun setCellAsEmptyCell(col: Int, row: Int, internalStyle: CourseTableInternalStyle.EmptyView, courseTableStyle: CourseTableStyle) {
        this.row = row
        this.col = col
        this.rowSize = 1
        this.courseCellType = CellType.EMPTY

        removeAllViewsInLayout()

        alpha = courseTableStyle.customCourseTableAlpha

        layoutParams = GridLayout.LayoutParams().apply {
            columnSpec = GridLayout.spec(col)
            rowSpec = GridLayout.spec(row, 1f)
        }

        setPadding(internalStyle.padding)

        addViewInLayout(View(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            background = CourseTableViewHelper.buildRadiusDrawable(internalStyle.backgroundColor, internalStyle.radius)
        })
    }

    fun setCellAsTimeCell(
        row: Int,
        courseTimeStrArr: Array<Pair<String, String>>,
        internalStyle: CourseTableInternalStyle.TimeCellView,
        courseTableStyle: CourseTableStyle
    ) {
        this.row = row
        this.col = 0
        this.rowSize = 1
        this.courseCellType = CellType.TIME

        removeAllViewsInLayout()

        alpha = courseTableStyle.customCourseTableAlpha

        val colMerge = GridLayout.spec(col, 1)
        val rowMerge = GridLayout.spec(row, 1f)
        layoutParams = GridLayout.LayoutParams().apply {
            columnSpec = colMerge
            rowSpec = rowMerge
        }

        setPadding(internalStyle.padding)

        val courseTimeNumText = (row + 1).toString()
        val courseTimeText = "${courseTimeStrArr[row].first}${Constants.CHANGE_LINE}${courseTimeStrArr[row].second}"

        addViewInLayout(AdvancedLinearLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            gravity = Gravity.CENTER

            orientation = LinearLayoutCompat.VERTICAL

            setPadding(0, internalStyle.verticalPadding, 0, internalStyle.verticalPadding)

            if (courseTableStyle.drawAllCellBackground) {
                background = CourseTableViewHelper.buildRadiusDrawable(internalStyle.backgroundColor, internalStyle.radius)
            }

            // 课程节次
            addViewInLayout(TextView(context).apply {
                text = courseTimeNumText
                textSize = internalStyle.timeNumTextSize
                typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                setTextColor(internalStyle.textColor)

                gravity = Gravity.CENTER

                layoutParams =
                    LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT)
            })

            // 课程开始与结束时间
            addViewInLayout(TextView(context).apply {
                text = courseTimeText
                textSize = internalStyle.timeTextSize
                setTextColor(internalStyle.textColor)
                setPadding(0, internalStyle.timeTextPaddingTop, 0, 0)

                gravity = Gravity.CENTER

                layoutParams =
                    LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT)
            })
        })
    }

    fun setCellAsCourseCell(
        col: Int,
        courseInfo: CourseCell,
        cellStyle: CourseCellStyle,
        internalStyle: CourseTableInternalStyle.CourseCellView,
        courseTableStyle: CourseTableStyle
    ) {
        this.row = courseInfo.timeDuration.startTime - 1
        this.col = col
        this.rowSize = courseInfo.timeDuration.durationLength
        this.courseCellType = CellType.COURSE

        removeAllViewsInLayout()

        alpha = courseTableStyle.customCourseTableAlpha

        val colMerge = GridLayout.spec(col, 1)
        val rowMerge = GridLayout.spec(row, courseInfo.timeDuration.durationLength, 1f)
        layoutParams = GridLayout.LayoutParams().apply {
            columnSpec = colMerge
            rowSpec = rowMerge
        }

        setPadding(internalStyle.padding)

        // 课程信息文字
        addViewInLayout(TextView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

            textSize = courseTableStyle.courseCellTextSize

            background =
                if (!courseInfo.thisWeekCourse && courseTableStyle.notThisWeekCourseShowType.contains(SettingsPref.NotThisWeekCourseCellStyle.COLOR)) {
                    CourseTableViewHelper.buildRadiusDrawable(internalStyle.notThisWeekCourseColor, internalStyle.radius)
                } else {
                    CourseTableViewHelper.buildRadiusDrawable(cellStyle.color, internalStyle.radius)
                }

            setTextColor(
                if (ColorUtils.isLightColor(cellStyle.color)) {
                    internalStyle.darkTextColor
                } else {
                    internalStyle.lightTextColor
                }
            )
            setPadding(internalStyle.textPadding)

            val baseShowText =
                if (courseInfo.courseTime.location.isEmpty() || courseInfo.courseTime.location.isBlank()) {
                    courseInfo.courseName
                } else {
                    "${courseInfo.courseName}${COURSE_INFO_JOIN_SYMBOL}${courseInfo.courseTime.location}"
                }

            text =
                if (courseInfo.thisWeekCourse || !courseTableStyle.notThisWeekCourseShowType.contains(SettingsPref.NotThisWeekCourseCellStyle.TEXT)) {
                    baseShowText
                } else {
                    val notThisWeekText = "${context.getString(R.string.not_current_week_course)}${Constants.CHANGE_LINE}"
                    SpannableStringBuilder(notThisWeekText + baseShowText).apply {
                        setSpan(StyleSpan(Typeface.BOLD), 0, notThisWeekText.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                    }
                }

            gravity = if (courseTableStyle.centerHorizontalShowCourseText && courseTableStyle.centerVerticalShowCourseText) {
                Gravity.CENTER
            } else if (courseTableStyle.centerVerticalShowCourseText) {
                Gravity.CENTER_VERTICAL
            } else if (courseTableStyle.centerHorizontalShowCourseText) {
                Gravity.CENTER_HORIZONTAL
            } else {
                Gravity.TOP or Gravity.START
            }

            isClickable = true

            setOnClickListener {
                listener?.invoke(courseInfo, cellStyle)
            }
        })
    }

    fun setOnCourseCellClickListener(listener: ((CourseCell, CourseCellStyle) -> Unit)?) {
        this.listener = listener
    }
}