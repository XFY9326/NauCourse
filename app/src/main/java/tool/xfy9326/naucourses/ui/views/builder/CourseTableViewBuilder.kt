package tool.xfy9326.naucourses.ui.views.builder

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.gridlayout.widget.GridLayout
import kotlinx.android.synthetic.main.view_table_cell_course.view.*
import kotlinx.android.synthetic.main.view_table_cell_date.view.*
import kotlinx.android.synthetic.main.view_table_cell_month.view.*
import kotlinx.android.synthetic.main.view_table_cell_time.view.*
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.beans.CourseCell
import tool.xfy9326.naucourses.beans.CourseTable
import tool.xfy9326.naucourses.providers.beans.jwc.TermDate
import tool.xfy9326.naucourses.utils.compute.TimeUtils


object CourseTableViewBuilder {
    private const val COURSE_INFO_JOIN_SYMBOL = "\n\n@"
    private const val DEFAULT_TABLE_WIDTH_SIZE = Constants.Time.MAX_WEEK_DAY + 1
    private const val DEFAULT_TABLE_HEIGHT_SIZE = Constants.Course.MAX_COURSE_LENGTH + 1

    private lateinit var layoutInflater: LayoutInflater
    private lateinit var weekDayNumStrArr: Array<String>
    private lateinit var monthStr: String

    private val courseTimeStrArr = Array(TimeUtils.CLASS_TIME_ARR.size) {
        Pair(TimeUtils.CLASS_TIME_ARR[it].getStartTimeStr(), TimeUtils.CLASS_TIME_ARR[it].getEndTimeStr())
    }

    fun initBuilder(context: Context) {
        layoutInflater = LayoutInflater.from(context)
        weekDayNumStrArr = context.resources.getStringArray(R.array.weekday_num)
        monthStr = context.getString(R.string.month)
    }

    fun generateCourseTableView(weekNum: Int, courseTable: CourseTable, termDate: TermDate, targetView: GridLayout): Array<View> {
        val dateInfo = TimeUtils.getWeekNumDateArray(termDate.startDate, weekNum)

        println(courseTable.table[Constants.Time.MAX_WEEK_DAY - 1])
        println(courseTable.table[Constants.Time.MAX_WEEK_DAY - 2])
        val colMax = if (hasWeekendCourse(courseTable)) DEFAULT_TABLE_WIDTH_SIZE - 2 else DEFAULT_TABLE_WIDTH_SIZE
        val rowMax = DEFAULT_TABLE_HEIGHT_SIZE
        targetView.columnCount = colMax
        targetView.rowCount = rowMax

        return buildCellViews(colMax, rowMax, targetView, courseTable, dateInfo)
    }

    private fun buildCellViews(
        colMax: Int, @Suppress("SameParameterValue") rowMax: Int,
        targetView: GridLayout,
        table: CourseTable,
        dateInfo: Pair<Int, Array<Int>>
    ): Array<View> {
        val result = ArrayList<View>(colMax + rowMax - 1 + table.table.sumBy {
            it.size
        })
        // 添加月份
        result.add(getMonthCellView(dateInfo.first, targetView).apply {
            val colMerge = GridLayout.spec(0)
            val rowMerge = GridLayout.spec(0)
            val gridLayoutParams = GridLayout.LayoutParams(rowMerge, colMerge).apply {
                setGravity(Gravity.CENTER)
            }
            layoutParams = gridLayoutParams
        })
        //添加课程节数与上下课时间
        for (row in 1 until rowMax) {
            result.add(getTimeCellView(row, targetView).apply {
                val colMerge = GridLayout.spec(0)
                val rowMerge = GridLayout.spec(row)
                val gridLayoutParams = GridLayout.LayoutParams(rowMerge, colMerge)
                layoutParams = gridLayoutParams
            })
        }
        // 添加课程日期与星期
        for (col in 1 until colMax) {
            result.add(getDateCellView(col, dateInfo.second[col - 1], targetView).apply {
                val colMerge = GridLayout.spec(col, 1, 1f)
                val rowMerge = GridLayout.spec(0)
                val gridLayoutParams = GridLayout.LayoutParams(rowMerge, colMerge).apply {
                    setGravity(Gravity.FILL_HORIZONTAL)
                }
                layoutParams = gridLayoutParams
            })
        }
        // 添加课程
        table.table.forEachIndexed { index, cellArr ->
            cellArr.forEach {
                result.add(getCourseCellView(it, targetView).apply {
                    val colMerge = GridLayout.spec(index + 1, 1, 1f)
                    val rowMerge = GridLayout.spec(it.timeDuration.startTime, it.timeDuration.durationLength)
                    val gridLayoutParams = GridLayout.LayoutParams(rowMerge, colMerge).apply {
                        setGravity(Gravity.FILL)
                        width = 0
                        height = 0
                    }
                    layoutParams = gridLayoutParams
                })
            }
        }
        return result.toTypedArray()
    }

    @SuppressLint("SetTextI18n")
    private fun getCourseCellView(courseInfo: CourseCell, targetView: GridLayout): View {
        val view = layoutInflater.inflate(R.layout.view_table_cell_course, targetView, false)
        view.tv_cellCourseInfo.text = "${courseInfo.courseName}$COURSE_INFO_JOIN_SYMBOL${courseInfo.courseLocation}"
        return view
    }

    private fun getDateCellView(weekDayNum: Int, dateNum: Int, targetView: GridLayout): View {
        val view = layoutInflater.inflate(R.layout.view_table_cell_date, targetView, false)
        view.tv_cellWeekdayNum.text = weekDayNumStrArr[weekDayNum - 1]
        view.tv_cellDateNum.text = dateNum.toString()
        return view
    }

    @SuppressLint("SetTextI18n")
    private fun getMonthCellView(monthNum: Int, targetView: GridLayout): View {
        val view = layoutInflater.inflate(R.layout.view_table_cell_month, targetView, false)
        view.tv_cellMonth.text = monthNum.toString() + monthStr
        return view
    }

    private fun getTimeCellView(courseTimeNum: Int, targetView: GridLayout): View {
        val view = layoutInflater.inflate(R.layout.view_table_cell_time, targetView, false)
        view.tv_cellTimeNum.text = courseTimeNum.toString()
        view.tv_cellStartTime.text = courseTimeStrArr[courseTimeNum - 1].first
        view.tv_cellEndTime.text = courseTimeStrArr[courseTimeNum - 1].second
        return view
    }

    private fun hasWeekendCourse(courseTable: CourseTable): Boolean =
        courseTable.table[Constants.Time.MAX_WEEK_DAY - 1].isEmpty() && courseTable.table[Constants.Time.MAX_WEEK_DAY - 2].isEmpty()
}