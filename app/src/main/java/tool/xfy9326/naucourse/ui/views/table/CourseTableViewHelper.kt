package tool.xfy9326.naucourse.ui.views.table

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.gridlayout.widget.GridLayout
import kotlinx.android.synthetic.main.fragment_table.view.*
import kotlinx.android.synthetic.main.view_course_table_date.view.*
import kotlinx.coroutines.*
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.beans.CourseCell
import tool.xfy9326.naucourse.beans.CourseCellStyle
import tool.xfy9326.naucourse.beans.CoursePkg
import tool.xfy9326.naucourse.providers.beans.jwc.TermDate
import tool.xfy9326.naucourse.ui.views.widgets.AdvancedGridLayout
import tool.xfy9326.naucourse.ui.views.widgets.AdvancedLinearLayout
import tool.xfy9326.naucourse.ui.views.widgets.CourseCellLayout
import tool.xfy9326.naucourse.utils.compute.TimeUtils
import tool.xfy9326.naucourse.utils.views.ColorUtils
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

object CourseTableViewHelper {
    private const val COURSE_INFO_JOIN_SYMBOL = "\n\n@"

    private const val DEFAULT_TABLE_WIDTH_SIZE = Constants.Time.MAX_WEEK_DAY + 1
    private const val DEFAULT_TABLE_HEIGHT_SIZE = Constants.Course.MAX_COURSE_LENGTH

    private var listener: OnCourseCellClickListener? = null

    private val courseTimeStrArr = Array(TimeUtils.CLASS_TIME_ARR.size) {
        Pair(TimeUtils.CLASS_TIME_ARR[it].getStartTimeStr(), TimeUtils.CLASS_TIME_ARR[it].getEndTimeStr())
    }

    fun getShowWeekDaySize(hasWeekendCourse: Boolean) =
        if (hasWeekendCourse) {
            DEFAULT_TABLE_WIDTH_SIZE - 1
        } else {
            DEFAULT_TABLE_WIDTH_SIZE - 3
        }

    suspend fun buildCourseTable(
        context: Context,
        coursePkg: CoursePkg,
        targetView: AdvancedGridLayout,
        targetWidth: Int,
        columnSize: Int,
        courseTableStyle: CourseTableStyle
    ) = withContext(Dispatchers.Default) {
        val courseTable = coursePkg.courseTable
        val styles = coursePkg.styles

        val timeColWidth = context.resources.getDimensionPixelSize(R.dimen.course_table_course_time_row_size)
        val courseColWidth = floor((targetWidth - timeColWidth) * 1f / (columnSize - 1)).toInt()

        var rowMax = DEFAULT_TABLE_HEIGHT_SIZE

        val resultDeferred = ArrayList<Deferred<CourseCellLayout>>((columnSize * rowMax * 0.75).toInt())

        val courseCellPadding = context.resources.getDimensionPixelSize(R.dimen.course_cell_padding)
        val timeCellVerticalPadding = context.resources.getDimensionPixelSize(R.dimen.course_time_cell_vertical_padding)
        val courseCellTextPadding = context.resources.getDimensionPixelSize(R.dimen.course_cell_text_padding)
        val backgroundRadius =
            if (courseTableStyle.useRoundCornerCourseCell) {
                context.resources.getDimensionPixelSize(R.dimen.course_cell_background_radius).toFloat()
            } else {
                0f
            }
        val timeCellTimeNumSize = context.resources.getDimensionPixelSize(R.dimen.course_time_cell_num_text_size).toFloat()
        val timeCellTimeSize = context.resources.getDimensionPixelSize(R.dimen.course_time_cell_text_size).toFloat()
        val timeTextPaddingTop = context.resources.getDimensionPixelSize(R.dimen.course_time_text_padding)

        val defaultTextColor = ContextCompat.getColor(context, R.color.colorCourseTimeDefault)
        val courseTextColorLight = ContextCompat.getColor(context, R.color.colorCourseTextLight)
        val courseTextColorDark = ContextCompat.getColor(context, R.color.colorCourseTextDark)
        val otherCourseCellBackground = ContextCompat.getColor(context, R.color.colorOtherCourseCellBackground)

        val lock = Any()
        var maxHeight = 0

        //添加课程节数与上下课时间
        for (row in 0 until rowMax) {
            resultDeferred.add(async(Dispatchers.Default) {
                val view = buildTimeCellView(
                    context, row, CourseTableInternalStyle.TimeCellView(
                        timeColWidth, otherCourseCellBackground, backgroundRadius, timeCellVerticalPadding,
                        defaultTextColor, timeCellTimeNumSize, timeCellTimeSize, courseCellPadding, timeTextPaddingTop
                    ), courseTableStyle
                )
                if (courseTableStyle.sameCellHeight) {
                    val viewHeight = getCellHeightByWidth(view, timeColWidth)
                    synchronized(lock) {
                        maxHeight = max(maxHeight, viewHeight)
                    }
                }
                view
            })
        }

        // 添加课程
        courseTable.table.forEachIndexed { index, cellArr ->
            val col = index + 1
            if (col < columnSize) {
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
                        val view = buildCourseCellView(
                            context, col, it, CourseCellStyle.getStyleByCourseId(it.courseId, styles, true)!!,
                            CourseTableInternalStyle.CourseCellView(
                                courseColWidth, backgroundRadius, courseCellPadding,
                                courseCellTextPadding, courseTextColorDark, courseTextColorLight
                            ), courseTableStyle
                        )
                        if (courseTableStyle.sameCellHeight) {
                            val viewHeight = getCellHeightByWidth(view, courseColWidth, it.timeDuration.durationLength)
                            synchronized(lock) {
                                maxHeight = max(maxHeight, viewHeight)
                            }
                        }
                        view
                    })
                }
                if (courseTableStyle.drawAllCellBackground) {
                    emptyCell.forEach {
                        if (it >= 0) {
                            resultDeferred.add(async(Dispatchers.Default) {
                                buildEmptyCellView(
                                    context, col, it,
                                    CourseTableInternalStyle.EmptyView(
                                        courseColWidth,
                                        otherCourseCellBackground,
                                        backgroundRadius,
                                        courseCellPadding
                                    ),
                                    courseTableStyle
                                )
                            })
                        }
                    }
                }
            }
        }
        val result = ArrayList<View>(resultDeferred.size)
        resultDeferred.forEach {
            result.add(it.await())
        }

        if (courseTableStyle.sameCellHeight) {
            result.forEach {
                it.layoutParams.height = maxHeight
            }
        }

        if (courseTableStyle.bottomCornerCompat) {
            val bottomCompat = context.resources.getDimensionPixelSize(R.dimen.course_table_bottom_corner_compat)
            result.add(View(context).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    columnSpec = GridLayout.spec(0)
                    rowSpec = GridLayout.spec(rowMax)
                    width = timeColWidth
                    height = bottomCompat
                }
            })
            rowMax++
        }

        launch(Dispatchers.Main) {
            if (targetView.childCount != 0) {
                targetView.removeAllViews()
            }
            if (targetView.columnCount != columnSize) {
                targetView.columnCount = columnSize
            }
            if (targetView.rowCount != rowMax) {
                targetView.rowCount = rowMax
            }
            targetView.replaceAllViews(result.toTypedArray())
        }
    }

    private fun getCellHeightByWidth(view: View, width: Int, divideNum: Int = 1): Int {
        val widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(widthSpec, heightSpec)
        return ceil(view.measuredHeight * 1f / divideNum).toInt()
    }

    private fun buildCourseCellView(
        context: Context, col: Int, courseInfo: CourseCell, cellStyle: CourseCellStyle,
        internalStyle: CourseTableInternalStyle.CourseCellView, courseTableStyle: CourseTableStyle
    ) =
        CourseCellLayout(context, courseInfo.timeDuration.startTime - 1, col).apply {
            val colMerge = GridLayout.spec(colNum)
            val rowMerge = GridLayout.spec(rowNum, courseInfo.timeDuration.durationLength, 1f)
            layoutParams = GridLayout.LayoutParams().apply {
                columnSpec = colMerge
                rowSpec = rowMerge
                width = internalStyle.width
            }

            setPadding(internalStyle.padding)

            alpha = courseTableStyle.customCourseTableAlpha

            // 课程信息文字
            addViewInLayout(TextView(context).apply {
                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

                textSize = cellStyle.textSize
                background = buildRadiusDrawable(cellStyle.color, internalStyle.radius)
                setTextColor(
                    if (ColorUtils.isLightColor(cellStyle.color)) {
                        internalStyle.darkTextColor
                    } else {
                        internalStyle.lightTextColor
                    }
                )
                setPadding(internalStyle.textPadding)

                val baseShowText =
                    if (courseInfo.courseLocation.isEmpty() || courseInfo.courseLocation.isBlank()) {
                        courseInfo.courseName
                    } else {
                        "${courseInfo.courseName}${COURSE_INFO_JOIN_SYMBOL}${courseInfo.courseLocation}"
                    }

                text =
                    if (courseInfo.thisWeekCourse) {
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
                    listener?.onCourseCellClick(courseInfo, cellStyle)
                }
            })
        }

    private fun buildTimeCellView(
        context: Context, row: Int,
        internalStyle: CourseTableInternalStyle.TimeCellView, courseTableStyle: CourseTableStyle
    ) =
        CourseCellLayout(context, row, 0).apply {
            val colMerge = GridLayout.spec(colNum)
            val rowMerge = GridLayout.spec(rowNum, 1f)
            layoutParams = GridLayout.LayoutParams().apply {
                columnSpec = colMerge
                rowSpec = rowMerge
                width = internalStyle.width
            }

            alpha = courseTableStyle.customCourseTableAlpha

            setPadding(internalStyle.padding)

            val courseTimeNumText = (rowNum + 1).toString()
            val courseTimeText = "${courseTimeStrArr[rowNum].first}${Constants.CHANGE_LINE}${courseTimeStrArr[rowNum].second}"

            addViewInLayout(AdvancedLinearLayout(context).apply {
                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                gravity = Gravity.CENTER

                orientation = LinearLayoutCompat.VERTICAL

                setPadding(0, internalStyle.verticalPadding, 0, internalStyle.verticalPadding)

                if (courseTableStyle.drawAllCellBackground) {
                    background = buildRadiusDrawable(internalStyle.backgroundColor, internalStyle.radius)
                }

                // 课程节次
                addViewInLayout(TextView(context).apply {
                    text = courseTimeNumText
                    textSize = internalStyle.timeNumTextSize
                    typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    setTextColor(internalStyle.textColor)

                    gravity = Gravity.CENTER

                    layoutParams = LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                })

                // 课程开始与结束时间
                addViewInLayout(TextView(context).apply {
                    text = courseTimeText
                    textSize = internalStyle.timeTextSize
                    setTextColor(internalStyle.textColor)
                    setPadding(0, internalStyle.timeTextPaddingTop, 0, 0)

                    gravity = Gravity.CENTER

                    layoutParams = LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                })
            })
        }

    private fun buildEmptyCellView(
        context: Context, col: Int, row: Int,
        internalStyle: CourseTableInternalStyle.EmptyView, courseTableStyle: CourseTableStyle
    ) =
        CourseCellLayout(context, row, col).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                columnSpec = GridLayout.spec(col)
                rowSpec = GridLayout.spec(row, 1f)
                width = internalStyle.width
            }

            alpha = courseTableStyle.customCourseTableAlpha

            setPadding(internalStyle.padding)

            addViewInLayout(View(context).apply {
                layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                background = buildRadiusDrawable(internalStyle.backgroundColor, internalStyle.radius)
            })
        }

    suspend fun buildCourseTableHeader(
        context: Context,
        termDate: TermDate,
        weekNum: Int,
        columnSize: Int,
        headerLayout: AdvancedLinearLayout,
        courseTableStyle: CourseTableStyle
    ) = withContext(Dispatchers.Default) {
        context.apply {
            val layoutInflater = LayoutInflater.from(context)

            val weekDayNumStrArr = resources.getStringArray(R.array.weekday_num)
            val dateInfo = TimeUtils.getWeekNumDateArray(termDate, weekNum)
            val today = TimeUtils.getTodayDate()

            val defaultTextColor = ContextCompat.getColor(this, R.color.colorCourseTimeDefault)
            val highLightTextColor = ContextCompat.getColor(this, R.color.colorCourseTimeHighLight)
            val highLightTextColorBackground = ContextCompat.getColor(this, R.color.colorCourseTimeHighLightBackground)
            val otherCourseCellBackground = ContextCompat.getColor(context, R.color.colorOtherCourseCellBackground)

            val backgroundRadius =
                if (courseTableStyle.useRoundCornerCourseCell) {
                    context.resources.getDimensionPixelSize(R.dimen.course_cell_background_radius).toFloat()
                } else {
                    0f
                }

            val views = Array(columnSize) {
                val isToday = dateInfo.first == today.first && dateInfo.second[it] == today.second

                layoutInflater.inflate(R.layout.view_course_table_date, headerLayout, false).apply {
                    tv_cellWeekdayNum.text = weekDayNumStrArr[it]
                    tv_cellDateNum.text = dateInfo.second[it].toString()
                    if (isToday) {
                        tv_cellWeekdayNum.setTextColor(highLightTextColor)
                        tv_cellDateNum.setTextColor(highLightTextColor)
                        tv_cellWeekdayNum.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                        tv_cellDateNum.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                        background = buildRadiusDrawable(
                            highLightTextColorBackground,
                            context.resources.getDimensionPixelSize(R.dimen.course_date_highlight_background_radius).toFloat()
                        )
                    } else {
                        tv_cellWeekdayNum.setTextColor(defaultTextColor)
                        tv_cellDateNum.setTextColor(defaultTextColor)
                        tv_cellWeekdayNum.typeface = Typeface.DEFAULT
                        tv_cellDateNum.typeface = Typeface.DEFAULT
                        background =
                            if (courseTableStyle.drawAllCellBackground) {
                                buildRadiusDrawable(otherCourseCellBackground, backgroundRadius)
                            } else {
                                null
                            }
                    }
                }
            }

            launch(Dispatchers.Main) {
                headerLayout.alpha = courseTableStyle.customCourseTableAlpha

                if (headerLayout.childCount > 1) {
                    headerLayout.removeViewsInLayout(1, headerLayout.childCount - 1)
                }

                headerLayout.tv_cellMonth.apply {
                    text = getString(R.string.month, today.first)
                    background =
                        if (courseTableStyle.drawAllCellBackground) {
                            buildRadiusDrawable(otherCourseCellBackground, backgroundRadius)
                        } else {
                            null
                        }
                }

                for (view in views) {
                    headerLayout.addViewInLayout(view)
                }
                headerLayout.refreshLayout()
            }
        }
    }

    fun setOnCourseCellClickListener(listener: OnCourseCellClickListener) {
        this.listener = listener
    }

    private fun buildRadiusDrawable(colorInt: Int, radius: Float): Drawable =
        GradientDrawable().apply {
            if (radius != 0f) cornerRadius = radius
            setColor(colorInt)
        }
}