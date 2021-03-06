package tool.xfy9326.naucourse.ui.dialogs

import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.NumberPicker
import androidx.annotation.IdRes
import androidx.core.view.setMargins
import androidx.fragment.app.DialogFragment
import androidx.gridlayout.widget.GridLayout
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.constants.BaseConst
import tool.xfy9326.naucourse.constants.CourseConst
import tool.xfy9326.naucourse.constants.TimeConst
import tool.xfy9326.naucourse.databinding.DialogCourseTimeEditBinding
import tool.xfy9326.naucourse.kt.isEven
import tool.xfy9326.naucourse.kt.isOdd
import tool.xfy9326.naucourse.kt.showShortToast
import tool.xfy9326.naucourse.providers.beans.jwc.CourseTime
import tool.xfy9326.naucourse.providers.beans.jwc.TimePeriod
import tool.xfy9326.naucourse.providers.beans.jwc.TimePeriodList
import tool.xfy9326.naucourse.providers.beans.jwc.WeekMode
import tool.xfy9326.naucourse.ui.views.widgets.AdvancedFrameLayout
import tool.xfy9326.naucourse.ui.views.widgets.CourseTimeEditCell
import tool.xfy9326.naucourse.utils.views.DialogUtils
import kotlin.properties.Delegates

class CourseTimeEditDialog : DialogFragment() {
    companion object {
        const val COURSE_ID = "COURSE_ID"
        const val COURSE_TIME = "COURSE_TIME"
        const val MAX_WEEK_NUM = "MAX_WEEK_NUM"
        const val UPDATE_POSITION = "UPDATE_POSITION"

        private const val OLD_COURSE_TIME = "OLD_COURSE_TIME"

        private const val CONTENT_WIDTH_PERCENT = 0.85
        private const val DEFAULT_BUTTON_COUNT_IN_ROW = 4
        private const val DEFAULT_WEEK_NUM_CHECK = true
    }

    private lateinit var courseId: String
    private var oldCourseTime: CourseTime? = null
    private var courseTime: CourseTime? = null
    private var maxWeekNum by Delegates.notNull<Int>()
    private var position: Int? = null
    private lateinit var timeViewList: ArrayList<CourseTimeEditCell>
    private lateinit var binding: DialogCourseTimeEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = arguments?.getSerializable(UPDATE_POSITION) as Int?

        maxWeekNum = arguments?.getInt(MAX_WEEK_NUM) ?: CourseConst.MAX_WEEK_NUM_SIZE
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        dialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
        }
        binding = DialogCourseTimeEditBinding.inflate(layoutInflater, container, false)
        binding.apply {
            radioGroupWeekMode.check(getWeekModeId())

            buildWeekNumGrid()
            setupRadioGroup()

            buildTimeWheel()

            if (courseTime?.location == getString(R.string.no_data)) {
                etCourseLocation.setText(BaseConst.EMPTY)
            } else {
                etCourseLocation.setText(courseTime?.location)
            }

            btnCourseTimeEditConfirm.setOnClickListener {
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
                    showShortToast(R.string.weeks_empty)
                }
            }

            btnCourseTimeEditCancel.setOnClickListener {
                dismiss()
            }
        }
        return binding.root
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
            else -> throw IllegalArgumentException("Radio Button Has Error ID! Can't Convert To Week Mode! ID: $id")
        }

    private fun buildWeekNumGrid() = binding.apply {
        timeViewList.clear()
        val size = resources.getDimensionPixelSize(R.dimen.course_time_button_size)
        val margin = resources.getDimensionPixelSize(R.dimen.course_time_button_margin)
        val count =
            if (glCourseWeeks.measuredWidth != 0) {
                glCourseWeeks.measuredWidth / (size + margin * 2)
            } else {
                DEFAULT_BUTTON_COUNT_IN_ROW
            }
        glCourseWeeks.columnCount =
            when {
                count <= 0 -> 1
                count.isOdd() -> count - 1
                else -> count
            }
        val views = Array(maxWeekNum) {
            createCourseTimeButton(it + 1, size, margin, courseTime?.isWeekNumTrue(it + 1) ?: DEFAULT_WEEK_NUM_CHECK)
        }
        glCourseWeeks.replaceAllViews(views)
    }

    private fun setupRadioGroup() = binding.apply {
        radioGroupWeekMode.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioBtn_oddWeekMode -> for (cell in timeViewList) cell.isChecked = cell.showNum.isOdd()
                R.id.radioBtn_evenWeekMode -> for (cell in timeViewList) cell.isChecked = cell.showNum.isEven()
            }
        }
    }

    private fun createCourseTimeButton(num: Int, size: Int, margin: Int, checked: Boolean) =
        AdvancedFrameLayout(requireContext()).apply {
            layoutParams = GridLayout.LayoutParams().apply {
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1, 1f)
            }
            val cellView = CourseTimeEditCell(requireContext(), num, checked).apply {
                layoutParams = FrameLayout.LayoutParams(size, size).apply {
                    this.gravity = Gravity.CENTER
                    setMargins(margin)
                }
                setOnCheckedChangeListener(object : CourseTimeEditCell.OnCheckedChangeListener {
                    override fun onCheckedChanged(cellView: CourseTimeEditCell, isChecked: Boolean) {
                        if (isChecked) {
                            when (binding.radioGroupWeekMode.checkedRadioButtonId) {
                                R.id.radioBtn_oddWeekMode -> if (cellView.showNum.isEven()) binding.radioBtnAllWeeksMode.isChecked = true
                                R.id.radioBtn_evenWeekMode -> if (cellView.showNum.isOdd()) binding.radioBtnAllWeeksMode.isChecked = true
                            }
                        }
                    }
                })
            }
            timeViewList.add(cellView)
            addViewInLayout(cellView)
        }

    private fun buildTimeWheel() = binding.apply {
        val weekDayNumStrArray = resources.getStringArray(R.array.weekday_num)

        pickerCourseTimeWeekDay.apply {
            minValue = TimeConst.MIN_WEEK_DAY
            maxValue = TimeConst.MAX_WEEK_DAY
            displayedValues = Array(weekDayNumStrArray.size) {
                getString(R.string.week_day, weekDayNumStrArray[it])
            }
            if (courseTime != null) value = courseTime!!.weekDay.toInt()
        }

        pickerCourseStartTime.apply {
            minValue = CourseConst.MIN_COURSE_LENGTH
            maxValue = CourseConst.MAX_COURSE_LENGTH
            displayedValues = Array(CourseConst.MAX_COURSE_LENGTH) {
                getString(R.string.course_num, it + 1)
            }
            setOnScrollListener { v, scrollState ->
                if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
                    val newVal = v.value
                    if (pickerCourseEndTime.value < newVal) pickerCourseEndTime.value = newVal
                }
            }
            // 课程时间在单个时间项只被允许设定一段，因此只编辑第一段
            if (courseTime != null && courseTime!!.coursesNumArray.timePeriods.isNotEmpty())
                value = courseTime!!.coursesNumArray.timePeriods[0].start
        }

        pickerCourseEndTime.apply {
            minValue = CourseConst.MIN_COURSE_LENGTH
            maxValue = CourseConst.MAX_COURSE_LENGTH
            displayedValues = Array(CourseConst.MAX_COURSE_LENGTH) {
                getString(R.string.course_num, it + 1)
            }
            setOnScrollListener { v, scrollState ->
                if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
                    val newVal = v.value
                    if (pickerCourseStartTime.value > newVal) {
                        pickerCourseStartTime.value = newVal
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
        DialogUtils.applyBackgroundAndWidth(requireContext(), dialog, CONTENT_WIDTH_PERCENT)
    }

    private fun readWeeksList(weekMode: WeekMode): ArrayList<TimePeriod> {
        requireView().apply {
            val weeksList = ArrayList<TimePeriod>(1)
            var weekStart: Int? = null
            var lastCheckedNum: Int? = null
            for (cell in timeViewList) {
                if (weekMode == WeekMode.ODD_WEEK_ONLY) {
                    if (cell.showNum.isEven()) continue
                } else if (weekMode == WeekMode.EVEN_WEEK_ONLY) {
                    if (cell.showNum.isOdd()) continue
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

            return weeksList
        }
    }

    private fun generateCourseTime(): CourseTime {
        requireView().apply {
            var weekMode = getWeekModeById(binding.radioGroupWeekMode.checkedRadioButtonId)
            val weeksList = readWeeksList(weekMode)

            // 单双周若只有一个周上课，则改为任意周模式
            if (weeksList.size == 1 && !weeksList.first().hasEnd()) {
                weekMode = WeekMode.ALL_WEEKS
            }

            val weekDay = binding.pickerCourseTimeWeekDay.value.toShort()
            val locationInput = binding.etCourseLocation.text?.toString()?.trim()
            val location =
                if (locationInput.isNullOrEmpty() || locationInput.isNullOrBlank()) {
                    getString(R.string.no_data)
                } else {
                    locationInput
                }

            val courseStart = binding.pickerCourseStartTime.value
            val courseEnd = binding.pickerCourseEndTime.value
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