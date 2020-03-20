package tool.xfy9326.naucourses.ui.dialogs

import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.NumberPicker
import androidx.annotation.IdRes
import androidx.core.view.setMargins
import androidx.fragment.app.DialogFragment
import androidx.gridlayout.widget.GridLayout
import kotlinx.android.synthetic.main.dialog_course_time_edit.view.*
import kotlinx.coroutines.*
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.providers.beans.jwc.CourseTime
import tool.xfy9326.naucourses.providers.beans.jwc.TimePeriod
import tool.xfy9326.naucourses.providers.beans.jwc.TimePeriodList
import tool.xfy9326.naucourses.providers.beans.jwc.WeekMode
import tool.xfy9326.naucourses.ui.views.widgets.AdvancedFrameLayout
import tool.xfy9326.naucourses.ui.views.widgets.CourseTimeCell
import tool.xfy9326.naucourses.utils.views.ActivityUtils.showToast
import kotlin.properties.Delegates

class CourseTimeEditDialog : DialogFragment() {
    companion object {
        const val COURSE_ID = "COURSE_ID"
        const val COURSE_TIME = "COURSE_TIME"
        const val MAX_WEEK_NUM = "MAX_WEEK_NUM"
        const val UPDATE_POSITION = "UPDATE_POSITION"

        private const val OLD_COURSE_TIME = "OLD_COURSE_TIME"

        private const val CONTENT_WIDTH_PERCENT = 0.85
        private const val DEFAULT_WEEK_NUM_CHECK = true
    }

    private val editScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private lateinit var courseId: String
    private var oldCourseTime: CourseTime? = null
    private var courseTime: CourseTime? = null
    private var maxWeekNum by Delegates.notNull<Int>()
    private var position: Int? = null
    private lateinit var timeViewList: ArrayList<CourseTimeCell>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = arguments?.getSerializable(UPDATE_POSITION) as Int?

        maxWeekNum = arguments?.getInt(MAX_WEEK_NUM) ?: Constants.Course.MAX_WEEK_NUM_SIZE
        courseId = arguments?.getString(COURSE_ID)!!

        val savedCourseTime = savedInstanceState?.getSerializable(COURSE_TIME) as CourseTime?
        courseTime = savedCourseTime ?: arguments?.getSerializable(COURSE_TIME) as CourseTime?

        oldCourseTime = savedInstanceState?.getSerializable(OLD_COURSE_TIME) as CourseTime? ?: courseTime

        timeViewList = ArrayList(maxWeekNum)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(OLD_COURSE_TIME, oldCourseTime)
        outState.putSerializable(COURSE_TIME, generateCourseTime())
        super.onSaveInstanceState(outState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
        }

        return inflater.inflate(R.layout.dialog_course_time_edit, container, false).apply {
            radioGroup_weekMode.check(getWeekModeId())

            editScope.launch {
                buildWeekNumGrid(this@apply)
                setupRadioGroup(this@apply)
            }

            editScope.launch {
                buildTimeWheel(this@apply)
            }

            et_courseLocation.setText(courseTime?.location)

            btn_courseTimeEditConfirm.setOnClickListener {
                editScope.launch {
                    val newCourseTime = generateCourseTime()
                    if (newCourseTime.weeksArray.size > 0) {
                        if (newCourseTime != oldCourseTime) {
                            val activity = requireActivity()
                            if (activity is OnEditCompleteListener) {
                                activity.onCourseTimeUpdated(newCourseTime, position)
                            }
                        }
                        dismiss()
                    } else {
                        showToast(requireContext(), R.string.weeks_empty)
                    }
                }
            }

            btn_courseTimeEditCancel.setOnClickListener {
                dismiss()
            }
        }
    }

    @IdRes
    private fun getWeekModeId(): Int =
        when (courseTime?.weekMode) {
            WeekMode.ODD_WEEK_ONLY -> R.id.radioBtn_oddWeekMode
            WeekMode.EVEN_WEEK_ONLY -> R.id.radioBtn_evenWeekMode
            WeekMode.ALL_WEEKS -> R.id.radioBtn_allWeeksMode
            else -> R.id.radioBtn_allWeeksMode
        }

    private fun getWeekModeById(@IdRes id: Int): WeekMode =
        when (id) {
            R.id.radioBtn_oddWeekMode -> WeekMode.ODD_WEEK_ONLY
            R.id.radioBtn_evenWeekMode -> WeekMode.EVEN_WEEK_ONLY
            R.id.radioBtn_allWeeksMode -> WeekMode.ALL_WEEKS
            else -> throw IllegalArgumentException("Radio Button Id Error! Can't Convert To Week Mode! Id: $id")
        }

    private fun buildWeekNumGrid(view: View) = view.apply {
        timeViewList.clear()
        val size = resources.getDimension(R.dimen.course_time_button_size).toInt()
        val views = Array(maxWeekNum) {
            createCourseTimeButton(view, it + 1, size, courseTime?.isWeekNumTrue(it + 1) ?: DEFAULT_WEEK_NUM_CHECK)
        }
        layout_courseWeeks.replaceAllViews(views, true)
    }

    private fun setupRadioGroup(view: View) = view.apply {
        radioGroup_weekMode.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioBtn_oddWeekMode -> for (cell in timeViewList) cell.isChecked = cell.showNum % 2 != 0
                R.id.radioBtn_evenWeekMode -> for (cell in timeViewList) cell.isChecked = cell.showNum % 2 == 0
            }
        }
    }

    private fun createCourseTimeButton(view: View, num: Int, size: Int, checked: Boolean) =
        AdvancedFrameLayout(requireContext()).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            }
            val cellView = CourseTimeCell(requireContext(), num, checked).apply {
                layoutParams = FrameLayout.LayoutParams(size, size).apply {
                    this.gravity = Gravity.CENTER
                    setMargins(resources.getDimension(R.dimen.course_time_button_margin).toInt())
                }
                setOnCheckedChangeListener(object : CourseTimeCell.OnCheckedChangeListener {
                    override fun onCheckedChanged(cellView: CourseTimeCell, isChecked: Boolean) {
                        if (isChecked) {
                            when (view.radioGroup_weekMode.checkedRadioButtonId) {
                                R.id.radioBtn_oddWeekMode -> if (cellView.showNum % 2 == 0) view.radioBtn_allWeeksMode.isChecked = true
                                R.id.radioBtn_evenWeekMode -> if (cellView.showNum % 2 != 0) view.radioBtn_allWeeksMode.isChecked = true
                            }
                        }
                    }
                })
            }
            timeViewList.add(cellView)
            addViewInLayout(cellView)
        }

    private fun buildTimeWheel(view: View) = view.apply {
        val weekDayNumStrArray = resources.getStringArray(R.array.weekday_num)

        picker_courseTimeWeekDay.apply {
            minValue = Constants.Time.MIN_WEEK_DAY
            maxValue = Constants.Time.MAX_WEEK_DAY
            displayedValues = Array(weekDayNumStrArray.size) {
                getString(R.string.week_day, weekDayNumStrArray[it])
            }
            if (courseTime != null) value = courseTime!!.weekDay.toInt()
        }

        picker_courseStartTime.apply {
            minValue = Constants.Course.MIN_COURSE_LENGTH
            maxValue = Constants.Course.MAX_COURSE_LENGTH
            displayedValues = Array(Constants.Course.MAX_COURSE_LENGTH) {
                getString(R.string.course_num, it + 1)
            }
            setOnScrollListener { v, scrollState ->
                if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
                    val newVal = v.value
                    if (view.picker_courseEndTime.value < newVal) view.picker_courseEndTime.value = newVal
                }
            }
            // 课程时间在单个时间项只被允许设定一段，因此只编辑第一段
            if (courseTime != null && courseTime!!.coursesNumArray.timePeriods.isNotEmpty())
                value = courseTime!!.coursesNumArray.timePeriods[0].start
        }

        picker_courseEndTime.apply {
            minValue = Constants.Course.MIN_COURSE_LENGTH
            maxValue = Constants.Course.MAX_COURSE_LENGTH
            displayedValues = Array(Constants.Course.MAX_COURSE_LENGTH) {
                getString(R.string.course_num, it + 1)
            }
            setOnScrollListener { v, scrollState ->
                if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
                    val newVal = v.value
                    if (view.picker_courseStartTime.value > newVal) {
                        view.picker_courseStartTime.value = newVal
                    }
                }
            }
            // 课程时间在单个时间项只被允许设定一段，因此只编辑第一段
            if (courseTime != null && courseTime!!.coursesNumArray.timePeriods.isNotEmpty())
                value = courseTime!!.coursesNumArray.timePeriods[0].end ?: courseTime!!.coursesNumArray.timePeriods[0].start
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.apply {
            val displayMetrics = activity?.resources?.displayMetrics!!
            window?.apply {
                setLayout((displayMetrics.widthPixels * CONTENT_WIDTH_PERCENT).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
                setBackgroundDrawable(activity?.getDrawable(R.drawable.bg_dialog))
            }
        }
    }

    override fun onDestroy() {
        editScope.cancel()
        super.onDestroy()
    }

    private fun generateCourseTime(): CourseTime {
        requireView().apply {
            var weekMode = getWeekModeById(requireView().radioGroup_weekMode.checkedRadioButtonId)

            val weeksList = ArrayList<TimePeriod>(1)
            var weekStart: Int? = null
            var lastCheckedNum: Int? = null
            for (cell in timeViewList) {
                if (weekMode == WeekMode.ODD_WEEK_ONLY) {
                    if (cell.showNum % 2 == 0) continue
                } else if (weekMode == WeekMode.EVEN_WEEK_ONLY) {
                    if (cell.showNum % 2 != 0) continue
                }

                if (cell.isChecked) {
                    if (weekStart == null) {
                        weekStart = cell.showNum
                    }
                    lastCheckedNum = cell.showNum
                } else if (weekStart != null && lastCheckedNum != null) {
                    weeksList.add(
                        if (weekStart == lastCheckedNum) {
                            TimePeriod(weekStart)
                        } else {
                            TimePeriod(weekStart, lastCheckedNum)
                        }
                    )
                    weekStart = null
                }
            }

            // 补全最后一个周数段
            if (weekStart != null) {
                weeksList.add(
                    if (weekStart == lastCheckedNum) {
                        TimePeriod(weekStart)
                    } else {
                        TimePeriod(weekStart, lastCheckedNum)
                    }
                )
            }

            // 单双周若只有一个周上课，则改为任意周模式
            if (weeksList.size == 1 && !weeksList.first().hasEnd()) {
                weekMode = WeekMode.ALL_WEEKS
            }

            val weekDay = picker_courseTimeWeekDay.value.toShort()
            val location = et_courseLocation.text?.toString()?.trim() ?: getString(R.string.no_data)

            val courseStart = picker_courseStartTime.value
            val courseEnd = picker_courseEndTime.value
            val coursesNumArray = TimePeriodList(
                arrayOf(
                    if (courseStart == courseEnd) {
                        TimePeriod(courseStart)
                    } else {
                        TimePeriod(courseStart, courseEnd)
                    }
                )
            )

            return CourseTime(requireContext(), courseId, location, weekMode, TimePeriodList(weeksList.toTypedArray()), weekDay, coursesNumArray)
        }
    }

    interface OnEditCompleteListener {
        fun onCourseTimeUpdated(courseTime: CourseTime, position: Int?)
    }
}