package tool.xfy9326.naucourses.ui.views.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.setPadding
import androidx.gridlayout.widget.GridLayout
import kotlinx.android.synthetic.main.view_table_cell_date.view.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.beans.CourseCell
import tool.xfy9326.naucourses.beans.CourseCellStyle
import tool.xfy9326.naucourses.beans.CoursePkg
import tool.xfy9326.naucourses.beans.CourseTable
import tool.xfy9326.naucourses.ui.views.widgets.AdvancedGridLayout
import tool.xfy9326.naucourses.ui.views.widgets.CourseCellLayout
import tool.xfy9326.naucourses.utils.compute.TimeUtils
import tool.xfy9326.naucourses.utils.views.ColorUtils
import kotlin.math.ceil
import kotlin.math.max
import kotlin.properties.Delegates


object CourseTableViewHelper {
    private const val COURSE_INFO_JOIN_SYMBOL = "\n\n@"

    const val DEFAULT_TABLE_WIDTH_SIZE = Constants.Time.MAX_WEEK_DAY + 1
    private const val DEFAULT_TABLE_HEIGHT_SIZE = Constants.Course.MAX_COURSE_LENGTH

    private lateinit var layoutInflater: LayoutInflater
    private lateinit var defaultValues: CourseBuilderDefaultValues
    private lateinit var listener: OnCourseCellClickListener

    private var COURSE_CELL_PADDING by Delegates.notNull<Int>()
    private var COURSE_CELL_BOTTOM_COMPAT_PADDING by Delegates.notNull<Int>()
    private var COURSE_CELL_TEXT_PADDING by Delegates.notNull<Int>()

    private const val DEFAULT_COURSE_CELL_BACKGROUND_ALPHA = 0.6f
    private var DEFAULT_COURSE_CELL_BACKGROUND_RADIUS by Delegates.notNull<Float>()

    private const val TIME_CELL_TIME_NUM_TEXT_SIZE = 17
    private const val TIME_CELL_TIME_TEXT_SIZE = 10

    private val courseTimeStrArr = Array(TimeUtils.CLASS_TIME_ARR.size) {
        Pair(TimeUtils.CLASS_TIME_ARR[it].getStartTimeStr(), TimeUtils.CLASS_TIME_ARR[it].getEndTimeStr())
    }

    fun initBuilder(context: Context, listener: OnCourseCellClickListener) {
        this.listener = listener
        layoutInflater = LayoutInflater.from(context)
        with(context) {
            COURSE_CELL_PADDING = resources.getDimensionPixelSize(R.dimen.course_cell_padding)
            COURSE_CELL_BOTTOM_COMPAT_PADDING = resources.getDimensionPixelSize(R.dimen.course_table_bottom_corner_compat)
            COURSE_CELL_TEXT_PADDING = resources.getDimensionPixelSize(R.dimen.course_cell_text_padding)
            DEFAULT_COURSE_CELL_BACKGROUND_RADIUS = resources.getDimensionPixelSize(R.dimen.course_cell_background_radius).toFloat()

            defaultValues = CourseBuilderDefaultValues(
                getString(R.string.not_current_week_course),
                resources.getStringArray(R.array.weekday_num),
                getColor(R.color.colorCourseTimeDefault),
                getColor(R.color.colorCourseTimeHighLight),
                getColor(R.color.colorCourseTextLight),
                getColor(R.color.colorCourseTextDark)
            )
        }
    }

    interface OnCourseCellClickListener {
        fun onCourseCellClick(courseCell: CourseCell, cellStyle: CourseCellStyle)
    }

    @Suppress("ArrayInDataClass")
    private data class CourseBuilderDefaultValues(
        val notThisWeekCourseStr: String,
        val weekDayNumStrArr: Array<String>,
        val defaultTextColor: Int,
        val highLightTextColor: Int,
        val courseTextColorLight: Int,
        val courseTextColorDark: Int
    )

    data class CourseTableStyle(
        val sameCellHeight: Boolean,
        val bottomCornerCompat: Boolean,
        val centerHorizontalShowCourseText: Boolean
    )

    suspend fun createCourseTableView(
        context: Context,
        showWeekend: Boolean,
        coursePkg: CoursePkg,
        targetView: AdvancedGridLayout,
        headerWidth: Pair<Int, Int>,
        courseTableStyle: CourseTableStyle
    ) = withContext(Dispatchers.Default) {
        val courseTable = coursePkg.courseTable
        val styles = coursePkg.styles

        val colMax = if (showWeekend) DEFAULT_TABLE_WIDTH_SIZE else DEFAULT_TABLE_WIDTH_SIZE - 2
        val rowMax = DEFAULT_TABLE_HEIGHT_SIZE

        val resultDeferred = ArrayList<Deferred<View>>(rowMax)
        val lock = Any()
        var maxHeight = 0

        //添加课程节数与上下课时间
        for (row in 0 until rowMax) {
            resultDeferred.add(async(Dispatchers.Default) {
                val view = getTimeCellView(context, headerWidth.first, row, row + 1)
                if (courseTableStyle.sameCellHeight) {
                    val measuredHeight = getHeightByWidth(view, headerWidth.first)
                    synchronized(lock) {
                        maxHeight = max(maxHeight, measuredHeight)
                    }
                }
                view
            })
        }
        // 添加课程
        courseTable.table.forEachIndexed { index, cellArr ->
            cellArr.forEach {
                resultDeferred.add(async(Dispatchers.Default) {
                    val view = getCourseCellView(
                        context, it, headerWidth.second, index + 1,
                        CourseCellStyle.getStyleByCourseId(it.courseId, styles, true)!!,
                        courseTableStyle.centerHorizontalShowCourseText
                    )
                    if (courseTableStyle.sameCellHeight) {
                        val measuredHeight = getHeightByWidth(view, headerWidth.second, it.timeDuration.durationLength)
                        synchronized(lock) {
                            maxHeight = max(maxHeight, measuredHeight)
                        }
                    }
                    view
                })
            }
        }
        val result = Array(resultDeferred.size) {
            resultDeferred[it].await()
        }
        result.forEach {
            if (courseTableStyle.sameCellHeight) it.layoutParams.height = maxHeight

            if (courseTableStyle.bottomCornerCompat) {
                val layout = (it as CourseCellLayout)
                if (layout.rowNum == rowMax - 1) {
                    layout.setPadding(0, 0, 0, COURSE_CELL_BOTTOM_COMPAT_PADDING)
                    // sameCellHeight开启时会计算所有课程格高度，此处减少计算量
                    if (courseTableStyle.sameCellHeight) {
                        layout.layoutParams.height += COURSE_CELL_BOTTOM_COMPAT_PADDING
                    } else {
                        val measuredHeight = getHeightByWidth(layout, headerWidth.first)
                        layout.layoutParams.height = measuredHeight + COURSE_CELL_BOTTOM_COMPAT_PADDING
                    }
                }
            }
        }
        withContext(Dispatchers.Main) {
            if (targetView.columnCount != colMax) {
                targetView.columnCount = colMax
            }
            if (targetView.rowCount != rowMax) {
                targetView.rowCount = rowMax
            }
            targetView.replaceAllViews(result)
        }
    }

    private fun getHeightByWidth(view: View, width: Int, divideNum: Int = 1): Int {
        val widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(widthSpec, heightSpec)
        return ceil(view.measuredHeight * 1f / divideNum).toInt()
    }

    fun getWindowsWidth(context: Context) = context.resources.displayMetrics.widthPixels

    @SuppressLint("SetTextI18n")
    private fun getCourseCellView(
        context: Context,
        courseInfo: CourseCell,
        headerWidth: Int,
        col: Int,
        cellStyle: CourseCellStyle,
        centerShowCourseText: Boolean
    ): View =
        CourseCellLayout(context).apply {
            rowNum = courseInfo.timeDuration.startTime - 1
            colNum = col

            val colMerge = GridLayout.spec(col)
            val rowMerge = GridLayout.spec(courseInfo.timeDuration.startTime - 1, courseInfo.timeDuration.durationLength, 1f)
            layoutParams = GridLayout.LayoutParams().apply {
                columnSpec = colMerge
                rowSpec = rowMerge
                width = headerWidth
            }
            setPadding(COURSE_CELL_PADDING)

            gravity = Gravity.TOP or Gravity.CENTER

            alpha = DEFAULT_COURSE_CELL_BACKGROUND_ALPHA

            // 课程信息文字
            addViewInLayout(TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                textSize = cellStyle.textSize
                background = buildCourseCellBackground(cellStyle.color, DEFAULT_COURSE_CELL_BACKGROUND_RADIUS)
                setTextColor(
                    if (ColorUtils.isLightColor(cellStyle.color)) {
                        defaultValues.courseTextColorDark
                    } else {
                        defaultValues.courseTextColorLight
                    }
                )
                setPadding(COURSE_CELL_TEXT_PADDING)

                val baseShowText =
                    if (courseInfo.courseLocation.isEmpty() || courseInfo.courseLocation.isBlank()) {
                        courseInfo.courseName
                    } else {
                        "${courseInfo.courseName}$COURSE_INFO_JOIN_SYMBOL${courseInfo.courseLocation}"
                    }

                text =
                    if (courseInfo.thisWeekCourse) {
                        baseShowText
                    } else {
                        val notThisWeekText = "${defaultValues.notThisWeekCourseStr}${Constants.CHANGE_LINE}"
                        SpannableStringBuilder(notThisWeekText + baseShowText).apply {
                            setSpan(StyleSpan(Typeface.BOLD), 0, notThisWeekText.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                        }
                    }

                gravity = if (centerShowCourseText) {
                    Gravity.CENTER_HORIZONTAL
                } else {
                    Gravity.TOP or Gravity.START
                }

                isClickable = true
                setOnClickListener {
                    listener.onCourseCellClick(courseInfo, cellStyle)
                }
            })
        }

    @SuppressLint("SetTextI18n")
    private fun getTimeCellView(context: Context, headerWidth: Int, row: Int, courseTimeNum: Int): View =
        CourseCellLayout(context).apply {
            rowNum = row
            colNum = 0

            val colMerge = GridLayout.spec(0)
            val rowMerge = GridLayout.spec(row, 1f)
            layoutParams = GridLayout.LayoutParams().apply {
                columnSpec = colMerge
                rowSpec = rowMerge
                width = headerWidth
            }

            gravity = Gravity.CENTER
            orientation = LinearLayoutCompat.VERTICAL

            addViewInLayout(TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                gravity = Gravity.CENTER

                val courseTimeNumText = "${courseTimeNum}${Constants.CHANGE_LINE}"
                val courseTimeText =
                    "${courseTimeStrArr[courseTimeNum - 1].first}${Constants.CHANGE_LINE}${courseTimeStrArr[courseTimeNum - 1].second}"
                text = SpannableStringBuilder(courseTimeNumText + courseTimeText).apply {
                    // 课程节次
                    setSpan(AbsoluteSizeSpan(TIME_CELL_TIME_NUM_TEXT_SIZE, true), 0, courseTimeNumText.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                    // 课程开始与结束时间
                    setSpan(
                        AbsoluteSizeSpan(TIME_CELL_TIME_TEXT_SIZE, true),
                        courseTimeNumText.length,
                        courseTimeNumText.length + courseTimeText.length,
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE
                    )
                }
            })
        }

    @Suppress("SameParameterValue")
    private fun buildCourseCellBackground(colorInt: Int, radius: Float): Drawable =
        GradientDrawable().apply {
            cornerRadius = radius
            colorFilter = PorterDuffColorFilter(colorInt, PorterDuff.Mode.SRC)
        }

    fun setDateCellView(weekDayNum: Int, dateNum: Int, targetView: View, isToday: Boolean) {
        targetView.tv_cellWeekdayNum.text = defaultValues.weekDayNumStrArr[weekDayNum - 1]
        targetView.tv_cellDateNum.text = dateNum.toString()
        if (isToday) {
            targetView.tv_cellWeekdayNum.setTextColor(defaultValues.highLightTextColor)
            targetView.tv_cellDateNum.setTextColor(defaultValues.highLightTextColor)
        } else {
            targetView.tv_cellWeekdayNum.setTextColor(defaultValues.defaultTextColor)
            targetView.tv_cellDateNum.setTextColor(defaultValues.defaultTextColor)
        }
    }

    fun hasWeekendCourse(courseTable: CourseTable): Boolean =
        courseTable.table.isNotEmpty() &&
                (courseTable.table[Constants.Time.MAX_WEEK_DAY - 1].isNotEmpty() || courseTable.table[Constants.Time.MAX_WEEK_DAY - 2].isNotEmpty())
}