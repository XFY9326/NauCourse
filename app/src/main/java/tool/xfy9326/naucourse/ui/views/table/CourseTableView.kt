package tool.xfy9326.naucourse.ui.views.table

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.LayoutParams
import androidx.core.content.ContextCompat
import androidx.core.view.children
import kotlinx.coroutines.*
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.beans.CoursePkg
import tool.xfy9326.naucourse.ui.views.widgets.AdvancedGridLayout
import tool.xfy9326.naucourse.utils.courses.CourseStyleUtils
import tool.xfy9326.naucourse.utils.courses.TimeUtils
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max


class CourseTableView : AdvancedGridLayout {
    companion object {
        const val DEFAULT_TABLE_WIDTH_SIZE = Constants.Time.MAX_WEEK_DAY + 1
        const val DEFAULT_TABLE_HEIGHT_SIZE = Constants.Course.MAX_COURSE_LENGTH

        private fun getCellHeightByWidth(view: View, width: Int, divideNum: Int = 1): Int {
            val widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
            val heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            view.measure(widthSpec, heightSpec)
            return ceil(view.measuredHeight * 1f / divideNum).toInt()
        }

        suspend fun create(context: Context, coursePkg: CoursePkg, columnSize: Int) =
            CourseTableView(context).apply {
                setTableDataInternal(coursePkg, columnSize, false)
            }
    }

    private val courseTimeStrArr = Array(TimeUtils.CLASS_TIME_ARR.size) {
        Pair(TimeUtils.CLASS_TIME_ARR[it].getStartTimeStr(), TimeUtils.CLASS_TIME_ARR[it].getEndTimeStr())
    }

    private val timeColWidth = resources.getDimensionPixelSize(R.dimen.course_table_course_time_row_size)
    private val courseCellPadding = resources.getDimensionPixelSize(R.dimen.course_cell_padding)
    private val timeCellVerticalPadding = resources.getDimensionPixelSize(R.dimen.course_time_cell_vertical_padding)
    private val courseCellTextPadding = resources.getDimensionPixelSize(R.dimen.course_cell_text_padding)
    private val timeCellTimeNumSize = resources.getDimensionPixelSize(R.dimen.course_time_cell_num_text_size).toFloat()
    private val timeCellTimeSize = resources.getDimensionPixelSize(R.dimen.course_time_cell_text_size).toFloat()
    private val timeTextPaddingTop = resources.getDimensionPixelSize(R.dimen.course_time_text_padding)

    private val courseTextColorLight = ContextCompat.getColor(context, R.color.colorCourseTextLight)
    private val courseTextColorDark = ContextCompat.getColor(context, R.color.colorCourseTextDark)
    private val otherCourseCellBackground = ContextCompat.getColor(context, R.color.colorOtherCourseCellBackground)
    private val notThisWeekCourseColor = ContextCompat.getColor(context, R.color.colorNotThisWeekCourseCell)

    private val defaultCourseCellBackgroundRadius = resources.getDimensionPixelSize(R.dimen.course_cell_background_radius).toFloat()
    private val defaultBottomFillCellHeight = resources.getDimensionPixelSize(R.dimen.course_table_bottom_corner_compat)
    private val defaultCourseTimeTextColor = ContextCompat.getColor(context, R.color.colorCourseTimeDefault)

    private var coursePkg: CoursePkg? = null
    private var listener: OnCourseCellClickListener? = null

    private constructor(context: Context) : this(context, null)

    private constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    private constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        initView()
    }

    private fun initView() {
        val layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        setLayoutParams(layoutParams)
        orientation = VERTICAL
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val style = coursePkg?.courseTableStyle
        if (style != null) {
            val width = MeasureSpec.getSize(widthSpec)
            val height = MeasureSpec.getSize(heightSpec)
            val courseColWidth = floor((width - timeColWidth) * 1f / (columnCount - 1)).toInt()
            var rowHeight = height / (rowCount - 1)

            for (child in children) {
                if (child is CourseCellView) {
                    if (child.col != 0) {
                        child.layoutParams.width = courseColWidth
                    }
                    if (style.sameCellHeight) {
                        val cellHeight = getCellHeightByWidth(child, child.layoutParams.width, child.rowSize)
                        rowHeight = max(cellHeight, rowHeight)
                    }
                } else {
                    if (child.layoutParams.width == 0) {
                        child.layoutParams.width = courseColWidth
                    }
                }
            }

            for (child in children) {
                if (child is CourseCellView) {
                    child.minimumHeight = rowHeight
                }
            }
        }
        super.onMeasure(widthSpec, heightSpec)
    }

    suspend fun setTableData(coursePkg: CoursePkg, columnSize: Int) {
        setTableDataInternal(coursePkg, columnSize, true)
    }

    suspend fun setTableDataInternal(coursePkg: CoursePkg, columnSize: Int, refreshInMain: Boolean) {
        this.coursePkg = coursePkg

        withContext(Dispatchers.Default) {
            val courseCellsAsync = getCourseCells(coursePkg.courseTableStyle, columnSize)
            val timeCellsAsync = getCourseTimeCells(coursePkg.courseTableStyle)
            val fillCellsAsync = getFillCells(coursePkg.courseTableStyle, columnSize, DEFAULT_TABLE_HEIGHT_SIZE + 1)

            val fillCells = awaitAll(*fillCellsAsync)
            val timeCells = awaitAll(*timeCellsAsync)
            val courseCells = courseCellsAsync.awaitAll()

            if (refreshInMain) {
                launch(Dispatchers.Main) {
                    refreshCells(fillCells, timeCells, courseCells, columnSize)
                }
            } else {
                refreshCells(fillCells, timeCells, courseCells, columnSize)
            }
        }
    }

    private fun refreshCells(fillCells: List<View>, timeCells: List<View>, courseCells: List<View>, col: Int) {
        if (childCount > 0) {
            removeAllViews()
        }

        columnCount = col
        rowCount = DEFAULT_TABLE_HEIGHT_SIZE + 1

        for (cell in fillCells) {
            addViewInLayout(cell)
        }
        for (cell in timeCells) {
            addViewInLayout(cell)
        }
        for (cell in courseCells) {
            addViewInLayout(cell)
        }

        refreshLayout()
    }

    private suspend fun getCourseCells(courseTableStyle: CourseTableStyle, columnSize: Int) = coroutineScope {
        val courseTable = coursePkg!!.courseTable
        val styles = coursePkg!!.styles
        val backgroundRadius = getBackgroundRadius(courseTableStyle)
        val courseCellInternalStyle = CourseTableInternalStyle.CourseCellView(
            backgroundRadius, courseCellPadding,
            courseCellTextPadding, courseTextColorDark, courseTextColorLight, notThisWeekCourseColor
        )
        val emptyCellStyle = CourseTableInternalStyle.EmptyView(
            otherCourseCellBackground,
            backgroundRadius,
            courseCellPadding
        )

        val result = ArrayList<Deferred<View>>()

        courseTable.table.forEachIndexed { index, cellArr ->
            val col = index + 1
            if (col < columnSize && (courseTableStyle.drawAllCellBackground || cellArr.isNotEmpty())) {
                var cellArrCount = 0
                var rowCount =
                    if (courseTableStyle.drawAllCellBackground) {
                        1
                    } else {
                        cellArr.first().timeDuration.startTime
                    }
                while (rowCount <= DEFAULT_TABLE_HEIGHT_SIZE) {
                    if (cellArrCount < cellArr.size) {
                        val cell = cellArr[cellArrCount]
                        if (cell.timeDuration.startTime == rowCount) {
                            cellArrCount++
                            if (cell.thisWeekCourse || courseTableStyle.showNotThisWeekCourseInTable) {
                                val courseStyle = CourseStyleUtils.getStyleByCourseId(cell.courseId, styles, true)!!
                                result.add(async {
                                    CourseCellView.createAsCourseCell(
                                        context, col, cell, courseStyle, courseCellInternalStyle,
                                        courseTableStyle
                                    ).also {
                                        it.setOnCourseCellClickListener { cell, style ->
                                            listener?.onCourseCellClick(cell, style)
                                        }
                                    }
                                })
                                if (courseTableStyle.drawAllCellBackground) {
                                    rowCount += cell.timeDuration.durationLength
                                    continue
                                }
                            } else if (!courseTableStyle.drawAllCellBackground) {
                                if (cellArrCount < cellArr.size) {
                                    rowCount = cellArr[cellArrCount].timeDuration.startTime
                                    continue
                                } else {
                                    break
                                }
                            }
                        }
                    }
                    if (courseTableStyle.drawAllCellBackground) {
                        val row = rowCount - 1
                        result.add(async {
                            CourseCellView.createAsEmptyCell(context, col, row, emptyCellStyle, courseTableStyle)
                        })
                    }
                    rowCount++
                }
            }
        }

        result
    }

    private suspend fun getCourseTimeCells(courseTableStyle: CourseTableStyle) = coroutineScope {
        val backgroundRadius = getBackgroundRadius(courseTableStyle)
        val defaultTextColor = getCourseTimeTextColor(courseTableStyle)
        val timeCellStyle = CourseTableInternalStyle.TimeCellView(
            timeColWidth, otherCourseCellBackground, backgroundRadius, timeCellVerticalPadding,
            defaultTextColor, timeCellTimeNumSize, timeCellTimeSize, courseCellPadding, timeTextPaddingTop
        )

        Array(DEFAULT_TABLE_HEIGHT_SIZE) {
            async {
                CourseCellView.createAsTimeCell(context, it, courseTimeStrArr, timeCellStyle, courseTableStyle)
            }
        }
    }

    private suspend fun getFillCells(courseTableStyle: CourseTableStyle, columnSize: Int, rowMax: Int) = coroutineScope {
        val bottomFillCellHeight = getBottomFillCellHeight(courseTableStyle)
        val fillCellSize =
            if (courseTableStyle.drawAllCellBackground) {
                1
            } else {
                columnSize
            }

        Array(fillCellSize) {
            async {
                View(context).apply {
                    visibility = View.INVISIBLE
                    layoutParams = LayoutParams().apply {
                        columnSpec = spec(it)
                        rowSpec = spec(rowMax - 1)
                        width =
                            if (it == 0) {
                                timeColWidth
                            } else {
                                0
                            }
                        height = bottomFillCellHeight
                    }
                }
            }
        }
    }

    private fun getBackgroundRadius(courseTableStyle: CourseTableStyle) =
        if (courseTableStyle.useRoundCornerCourseCell) {
            defaultCourseCellBackgroundRadius
        } else {
            0f
        }

    private fun getBottomFillCellHeight(courseTableStyle: CourseTableStyle) =
        if (courseTableStyle.bottomCornerCompat) {
            defaultBottomFillCellHeight
        } else {
            0
        }

    private fun getCourseTimeTextColor(courseTableStyle: CourseTableStyle) =
        if (courseTableStyle.enableCourseTableTimeTextColor) {
            courseTableStyle.courseTableTimeTextColor
        } else {
            defaultCourseTimeTextColor
        }

    fun setOnCourseCellClickListener(listener: OnCourseCellClickListener) {
        this.listener = listener
    }
}