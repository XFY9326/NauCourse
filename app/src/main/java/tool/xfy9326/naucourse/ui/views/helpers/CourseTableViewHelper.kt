package tool.xfy9326.naucourse.ui.views.helpers

import android.annotation.SuppressLint
import android.content.Context
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
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.gridlayout.widget.GridLayout
import kotlinx.android.synthetic.main.view_table_cell_date.view.*
import kotlinx.coroutines.*
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.beans.CourseCell
import tool.xfy9326.naucourse.beans.CourseCellStyle
import tool.xfy9326.naucourse.beans.CoursePkg
import tool.xfy9326.naucourse.ui.views.widgets.AdvancedGridLayout
import tool.xfy9326.naucourse.ui.views.widgets.CourseCellLayout
import tool.xfy9326.naucourse.utils.compute.TimeUtils
import tool.xfy9326.naucourse.utils.views.ColorUtils
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

    private var DEFAULT_COURSE_CELL_BACKGROUND_RADIUS by Delegates.notNull<Float>()

    private var DEFAULT_COURSE_DATE_HIGHLIGHT_BACKGROUND_RADIUS by Delegates.notNull<Float>()

    private var TIME_CELL_TIME_TEXT_SIZE by Delegates.notNull<Int>()
    private var TIME_CELL_TIME_NUM_TEXT_SIZE by Delegates.notNull<Int>()

    private val courseTimeStrArr = Array(TimeUtils.CLASS_TIME_ARR.size) {
        Pair(TimeUtils.CLASS_TIME_ARR[it].getStartTimeStr(), TimeUtils.CLASS_TIME_ARR[it].getEndTimeStr())
    }

    fun initBuilder(context: Context) {
        layoutInflater = LayoutInflater.from(context)
        with(context) {
            TIME_CELL_TIME_TEXT_SIZE = resources.getDimensionPixelSize(R.dimen.course_time_cell_text_size)
            TIME_CELL_TIME_NUM_TEXT_SIZE = resources.getDimensionPixelSize(R.dimen.course_time_cell_num_text_size)

            COURSE_CELL_PADDING = resources.getDimensionPixelSize(R.dimen.course_cell_padding)
            COURSE_CELL_BOTTOM_COMPAT_PADDING = resources.getDimensionPixelSize(R.dimen.course_table_bottom_corner_compat)
            COURSE_CELL_TEXT_PADDING = resources.getDimensionPixelSize(R.dimen.course_cell_text_padding)
            DEFAULT_COURSE_CELL_BACKGROUND_RADIUS = resources.getDimensionPixelSize(R.dimen.course_cell_background_radius).toFloat()

            DEFAULT_COURSE_DATE_HIGHLIGHT_BACKGROUND_RADIUS =
                resources.getDimensionPixelSize(R.dimen.course_date_highlight_background_radius).toFloat()

            defaultValues = CourseBuilderDefaultValues(
                getString(R.string.not_current_week_course),
                resources.getStringArray(R.array.weekday_num),
                ContextCompat.getColor(this, R.color.colorCourseTimeDefault),
                ContextCompat.getColor(this, R.color.colorCourseTimeHighLight),
                ContextCompat.getColor(this, R.color.colorCourseTimeHighLightBackground),
                ContextCompat.getColor(this, R.color.colorCourseTextLight),
                ContextCompat.getColor(this, R.color.colorCourseTextDark)
            )
        }
    }

    fun setOnCourseCellClickListener(listener: OnCourseCellClickListener) {
        this.listener = listener
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
        val highLightTextColorBackground: Int,
        val courseTextColorLight: Int,
        val courseTextColorDark: Int
    )

    data class CourseTableStyle(
        val sameCellHeight: Boolean,
        val bottomCornerCompat: Boolean,
        val centerHorizontalShowCourseText: Boolean,
        val centerVerticalShowCourseText: Boolean,
        val useRoundCornerCourseCell: Boolean,
        val drawAllCellBackground: Boolean,
        val forceShowCourseTableWeekends: Boolean,
        val customCourseTableBackground: Boolean,
        val customCourseTableAlpha: Float
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

        val resultDeferred = ArrayList<Deferred<View>>((colMax * rowMax * 0.75).toInt())
        val lock = Any()
        var maxHeight = 0

        //添加课程节数与上下课时间
        for (row in 0 until rowMax) {
            resultDeferred.add(async(Dispatchers.Default) {
                val view = getTimeCellView(context, headerWidth.first, row, row + 1, courseTableStyle.drawAllCellBackground, courseTableStyle)
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
            val col = index + 1
            if (col < colMax) {
                val emptyCell = if (courseTableStyle.drawAllCellBackground) {
                    IntArray(rowMax) { it }
                } else {
                    IntArray(0)
                }
                cellArr.forEach {
                    if (courseTableStyle.drawAllCellBackground) {
                        emptyCell.fill(-1, it.timeDuration.startTime - 1, it.timeDuration.startTime + it.timeDuration.durationLength - 1)
                    }
                    resultDeferred.add(async(Dispatchers.Default) {
                        val view = getCourseCellView(
                            context, it, headerWidth.second, col,
                            CourseCellStyle.getStyleByCourseId(it.courseId, styles, true)!!,
                            courseTableStyle
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
                if (courseTableStyle.drawAllCellBackground) {
                    emptyCell.forEach {
                        if (it >= 0) {
                            resultDeferred.add(async(Dispatchers.Default) {
                                val view = generateEmptyCellView(context, col, it, headerWidth.second, courseTableStyle)
                                if (courseTableStyle.sameCellHeight) {
                                    val measuredHeight = getHeightByWidth(view, headerWidth.second)
                                    synchronized(lock) {
                                        maxHeight = max(maxHeight, measuredHeight)
                                    }
                                }
                                view
                            })
                        }
                    }
                }
            }
        }
        val result = Array(resultDeferred.size) {
            resultDeferred[it].await()
        }
        if (courseTableStyle.sameCellHeight || courseTableStyle.bottomCornerCompat) {
            result.forEach {
                if (courseTableStyle.sameCellHeight) it.layoutParams.height = maxHeight

                if (courseTableStyle.bottomCornerCompat) {
                    val layout = (it as CourseCellLayout)
                    if (layout.rowNum == rowMax - 1) {
                        layout.setPadding(
                            layout.paddingLeft, layout.paddingTop, layout.paddingRight,
                            layout.paddingBottom + COURSE_CELL_BOTTOM_COMPAT_PADDING
                        )
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

    private fun generateEmptyCellView(context: Context, col: Int, row: Int, headerWidth: Int, courseTableStyle: CourseTableStyle): View =
        CourseCellLayout(context).apply {
            rowNum = row
            colNum = col

            layoutParams = GridLayout.LayoutParams().apply {
                columnSpec = GridLayout.spec(col)
                rowSpec = GridLayout.spec(row, 1f)
                width = headerWidth
            }

            alpha = courseTableStyle.customCourseTableAlpha

            setPadding(COURSE_CELL_PADDING)

            addViewInLayout(View(context).apply {
                layoutParams = LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                background = buildOtherCourseCellBackground(context, getCourseCellRadius(courseTableStyle))
            })
        }

    @SuppressLint("SetTextI18n")
    private fun getCourseCellView(
        context: Context,
        courseInfo: CourseCell,
        headerWidth: Int,
        col: Int,
        cellStyle: CourseCellStyle,
        courseTableStyle: CourseTableStyle
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

            alpha = courseTableStyle.customCourseTableAlpha

            // 课程信息文字
            addViewInLayout(TextView(context).apply {
                layoutParams = LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                textSize = cellStyle.textSize
                background = buildCourseCellBackground(cellStyle.color, getCourseCellRadius(courseTableStyle))
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
                    listener.onCourseCellClick(courseInfo, cellStyle)
                }
            })
        }

    @SuppressLint("SetTextI18n")
    private fun getTimeCellView(
        context: Context,
        headerWidth: Int,
        row: Int,
        courseTimeNum: Int,
        drawBackground: Boolean,
        courseTableStyle: CourseTableStyle
    ): View =
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

            alpha = courseTableStyle.customCourseTableAlpha

            setPadding(COURSE_CELL_PADDING)

            gravity = Gravity.CENTER
            orientation = LinearLayoutCompat.VERTICAL

            addViewInLayout(TextView(context).apply {
                layoutParams = LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                gravity = Gravity.CENTER

                setTextColor(defaultValues.defaultTextColor)

                if (drawBackground) {
                    background = buildOtherCourseCellBackground(context, 0f)
                }

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

    private fun getCourseCellRadius(courseTableStyle: CourseTableStyle) =
        if (courseTableStyle.useRoundCornerCourseCell) {
            DEFAULT_COURSE_CELL_BACKGROUND_RADIUS
        } else {
            0f
        }

    @Suppress("SameParameterValue")
    private fun buildCourseCellBackground(colorInt: Int, radius: Float): Drawable =
        GradientDrawable().apply {
            if (radius != 0f) cornerRadius = radius
            setColor(colorInt)
        }

    private fun buildOtherCourseCellBackground(context: Context, radius: Float) =
        buildCourseCellBackground(ContextCompat.getColor(context, R.color.colorOtherCourseCellBackground), radius)

    suspend fun drawHeaderBackground(context: Context, targetView: View, courseTableStyle: CourseTableStyle) = withContext(Dispatchers.Main) {
        targetView.apply {
            background = if (courseTableStyle.drawAllCellBackground) {
                buildOtherCourseCellBackground(context, 0f)
            } else {
                null
            }
            alpha = courseTableStyle.customCourseTableAlpha
        }
    }

    suspend fun buildCourseTableHeader(
        context: Context, monthView: TextView, courseTableHeaderDate: Array<View>,
        today: Pair<Int, Int>, dateInfo: Pair<Int, Array<Int>>, weekDayShowSize: Int,
        hasWeekendCourse: Boolean
    ) =
        withContext(Dispatchers.Main) {
            monthView.text = context.getString(R.string.month, dateInfo.first)
            monthView.setTextColor(defaultValues.defaultTextColor)

            if (hasWeekendCourse) {
                courseTableHeaderDate[courseTableHeaderDate.size - 1].visibility = View.VISIBLE
                courseTableHeaderDate[courseTableHeaderDate.size - 2].visibility = View.VISIBLE
            } else {
                courseTableHeaderDate[courseTableHeaderDate.size - 1].visibility = View.GONE
                courseTableHeaderDate[courseTableHeaderDate.size - 2].visibility = View.GONE
            }
            for (i in 0 until weekDayShowSize) {
                val isToday = dateInfo.first == today.first && dateInfo.second[i] == today.second
                launch { setDateCellView(i + 1, dateInfo.second[i], courseTableHeaderDate[i], isToday) }
            }
        }

    private fun setDateCellView(weekDayNum: Int, dateNum: Int, targetView: View, isToday: Boolean) {
        targetView.tv_cellWeekdayNum.text = defaultValues.weekDayNumStrArr[weekDayNum - 1]
        targetView.tv_cellDateNum.text = dateNum.toString()
        if (isToday) {
            targetView.tv_cellWeekdayNum.setTextColor(defaultValues.highLightTextColor)
            targetView.tv_cellDateNum.setTextColor(defaultValues.highLightTextColor)
            targetView.tv_cellWeekdayNum.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            targetView.tv_cellDateNum.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            targetView.background =
                buildCourseCellBackground(defaultValues.highLightTextColorBackground, DEFAULT_COURSE_DATE_HIGHLIGHT_BACKGROUND_RADIUS)
        } else {
            targetView.tv_cellWeekdayNum.setTextColor(defaultValues.defaultTextColor)
            targetView.tv_cellDateNum.setTextColor(defaultValues.defaultTextColor)
            targetView.tv_cellWeekdayNum.typeface = Typeface.DEFAULT
            targetView.tv_cellDateNum.typeface = Typeface.DEFAULT
            targetView.background = null
        }
    }
}