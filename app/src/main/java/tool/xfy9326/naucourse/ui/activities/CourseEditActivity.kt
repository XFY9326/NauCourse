package tool.xfy9326.naucourse.ui.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.widget.ImageViewCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.beans.CourseCellStyle
import tool.xfy9326.naucourse.constants.BaseConst
import tool.xfy9326.naucourse.databinding.ActivityCourseEditBinding
import tool.xfy9326.naucourse.kt.enableHomeButton
import tool.xfy9326.naucourse.kt.showSnackBar
import tool.xfy9326.naucourse.kt.showSnackBarWithCallback
import tool.xfy9326.naucourse.providers.beans.jwc.Course
import tool.xfy9326.naucourse.providers.beans.jwc.CourseSet
import tool.xfy9326.naucourse.providers.beans.jwc.CourseTime
import tool.xfy9326.naucourse.providers.beans.jwc.TermDate
import tool.xfy9326.naucourse.ui.activities.base.BaseActivity
import tool.xfy9326.naucourse.ui.dialogs.CourseTimeEditDialog
import tool.xfy9326.naucourse.ui.views.recyclerview.adapters.CourseTimeAdapter
import tool.xfy9326.naucourse.utils.BaseUtils
import tool.xfy9326.naucourse.utils.courses.CourseStyleUtils
import tool.xfy9326.naucourse.utils.courses.CourseUtils
import tool.xfy9326.naucourse.utils.courses.TimeUtils
import tool.xfy9326.naucourse.utils.views.AnimUtils
import tool.xfy9326.naucourse.utils.views.DialogUtils

class CourseEditActivity : BaseActivity(), CourseTimeAdapter.CourseTimeCallback, CourseTimeEditDialog.OnEditCompleteListener,
    ColorPickerDialogListener {
    companion object {
        const val COURSE_DATA = "COURSE_DATA"
        const val TERM_DATE = "TERM_DATE"
        const val COURSE_CELL_STYLE = "COURSE_CELL_STYLE"

        private const val COLOR_PICKER_DIALOG_ID = 2
        private const val VIEW_EXPANDED = "VIEW_EXPANDED"
        private const val TIME_LIST = "TIME_LIST"

        private const val EXPAND_ANIMATION_DURATION = 250L
    }

    private lateinit var courseId: String
    private var courseData: Course? = null
    private lateinit var termDate: TermDate
    private lateinit var courseStyle: CourseCellStyle
    private var newCourseStyle: CourseCellStyle? = null

    private lateinit var courseTimeAdapter: CourseTimeAdapter

    private var isShowMoreInfoExpanded = false

    private val binding by lazy {
        ActivityCourseEditBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            isShowMoreInfoExpanded = it.getBoolean(VIEW_EXPANDED)
            newCourseStyle = it.getSerializable(COURSE_CELL_STYLE) as CourseCellStyle?
        }
        readIntentData()
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar.tbGeneral)
        enableHomeButton()
        viewInit(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(VIEW_EXPANDED, isShowMoreInfoExpanded)
        outState.putSerializable(COURSE_CELL_STYLE, newCourseStyle)
        outState.putSerializable(TIME_LIST, courseTimeAdapter.getCourseTimeList())
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_course_edit, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                checkSaveForExit()
                return true
            }
            R.id.menu_courseEditSave -> {
                currentFocus?.clearFocus()
                BaseUtils.hideKeyboard(this, binding.layoutCourseEdit.windowToken)

                val newCourse = getEditResult(true)
                if (newCourse != null) {
                    val outputStyle = (newCourseStyle ?: courseStyle)
                    if (newCourse != courseData || outputStyle != courseStyle) {
                        setResult(RESULT_OK, Intent().putExtra(COURSE_DATA, newCourse).putExtra(COURSE_CELL_STYLE, outputStyle))
                    } else {
                        setResult(RESULT_OK)
                    }
                    super.onBackPressed()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() = checkSaveForExit()

    private fun readIntentData() {
        intent!!.apply {
            termDate = getSerializableExtra(TERM_DATE) as TermDate
            courseData = getSerializableExtra(COURSE_DATA) as Course?
            courseId = courseData?.id ?: CourseUtils.getNewCourseId()

            val tempCourseStyle = getSerializableExtra(COURSE_CELL_STYLE) as CourseCellStyle?
            if (tempCourseStyle == null) {
                if (newCourseStyle == null) {
                    newCourseStyle = CourseStyleUtils.getDefaultCellStyle(courseId)
                }
                courseStyle = newCourseStyle!!
            } else {
                courseStyle = tempCourseStyle
            }
        }
    }

    private fun viewInit(savedInstanceState: Bundle?) {
        binding.btnShowMoreCourseEditInfo.setOnClickListener {
            currentFocus?.clearFocus()
            BaseUtils.hideKeyboard(this, binding.layoutCourseEdit.windowToken)

            (it as AppCompatImageButton).apply {
                if (isShowMoreInfoExpanded) {
                    isShowMoreInfoExpanded = false
                    setImageResource(R.drawable.ic_load_more)
                    loadLess()
                } else {
                    isShowMoreInfoExpanded = true
                    setImageResource(R.drawable.ic_load_less)
                    loadMore()
                }
            }
        }

        binding.fabCourseEdit.setOnClickListener {
            CourseTimeEditDialog().apply {
                arguments = Bundle().apply {
                    putString(CourseTimeEditDialog.COURSE_ID, courseId)
                    putInt(CourseTimeEditDialog.MAX_WEEK_NUM, TimeUtils.getWeekLength(termDate))
                }
            }.show(supportFragmentManager, null)
        }

        if (isShowMoreInfoExpanded) {
            binding.btnShowMoreCourseEditInfo.setImageResource(R.drawable.ic_load_less)
            binding.layoutCourseEditInfo.visibility = View.VISIBLE
        }

        ImageViewCompat.setImageTintList(binding.ivCourseEditColor, ColorStateList.valueOf((newCourseStyle ?: courseStyle).color))
        binding.layoutCourseEditColor.setOnClickListener {
            DialogUtils.createCourseColorPickerDialog(this, (newCourseStyle ?: courseStyle).color, COLOR_PICKER_DIALOG_ID)
                .show(supportFragmentManager, null)
        }

        applyCourseInfoData()

        val timeList = if (savedInstanceState != null) {
            @Suppress("UNCHECKED_CAST")
            savedInstanceState.getSerializable(TIME_LIST) as ArrayList<CourseTime>
        } else {
            getSortedTimeList()
        }

        courseTimeAdapter = CourseTimeAdapter(this, timeList, this)

        binding.arvCourseDetailList.adapter = courseTimeAdapter
    }

    override fun onDeleteCourseTime(adapter: CourseTimeAdapter, courseTime: CourseTime, position: Int) {
        binding.layoutCourseEdit.showSnackBarWithCallback(R.string.delete_course_time_success, R.string.revoke) {
            adapter.recoverCourseTime(courseTime, position)
        }
    }

    override fun onEditCourseTime(adapter: CourseTimeAdapter, courseTime: CourseTime, position: Int) {
        CourseTimeEditDialog().apply {
            arguments = Bundle().apply {
                putSerializable(CourseTimeEditDialog.UPDATE_POSITION, position)
                putInt(CourseTimeEditDialog.MAX_WEEK_NUM, TimeUtils.getWeekLength(termDate))
                putString(CourseTimeEditDialog.COURSE_ID, courseId)
                putSerializable(CourseTimeEditDialog.COURSE_TIME, courseTime)
            }
        }.show(supportFragmentManager, null)
    }

    override fun onCourseTimeUpdated(courseTime: CourseTime, position: Int?) {
        if (position == null) {
            courseTimeAdapter.appendCourseTime(courseTime)
        } else {
            courseTimeAdapter.editCourseTime(courseTime, position)
        }
    }

    private fun getSortedTimeList(): ArrayList<CourseTime> =
        if (courseData != null) {
            ArrayList(courseData!!.timeSet).apply {
                sortWith { o1, o2 ->
                    o1.compareTo(o2)
                }
            }
        } else {
            ArrayList()
        }

    private fun loadMore() {
        binding.layoutCourseEditInfo.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        animateExpand(binding.layoutCourseEditInfo.measuredHeight)
        binding.layoutCourseEditInfo.animation = AnimUtils.getAnimationFadeVisible(this)
        binding.layoutCourseEditInfo.visibility = View.VISIBLE
    }

    private fun loadLess() {
        animateExpand(-binding.layoutCourseEditInfo.measuredHeight)
        binding.layoutCourseEditInfo.animation = AnimUtils.getAnimationFadeGone(this)
        binding.layoutCourseEditInfo.visibility = View.GONE
    }

    private fun animateExpand(changeHeight: Int) =
        ValueAnimator.ofInt(binding.cvCourseBaseInfo.measuredHeight, binding.cvCourseBaseInfo.measuredHeight + changeHeight).apply {
            duration = EXPAND_ANIMATION_DURATION
            interpolator = FastOutSlowInInterpolator()
            addUpdateListener {
                val value = animatedValue as Int
                binding.cvCourseBaseInfo.layoutParams.height = value
                binding.cvCourseBaseInfo.requestLayout()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    binding.btnShowMoreCourseEditInfo.visibility = View.INVISIBLE
                }

                override fun onAnimationEnd(animation: Animator?) = resetAfterExpandLayout()
                override fun onAnimationCancel(animation: Animator?) = resetAfterExpandLayout()
            })
            start()
        }

    private fun resetAfterExpandLayout() {
        binding.cvCourseBaseInfo.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        binding.cvCourseBaseInfo.requestLayout()

        binding.btnShowMoreCourseEditInfo.animation = AnimUtils.getAnimationFadeVisible(this@CourseEditActivity)
        binding.btnShowMoreCourseEditInfo.visibility = View.VISIBLE
    }

    private fun applyCourseInfoData() {
        binding.etCourseName.setText(courseData?.name)
        binding.etTeacherName.setText(courseData?.teacher)
        binding.etCourseCredit.setText(courseData?.credit?.toString())
        binding.etTeachClass.setText(courseData?.teachClass)
        binding.etCourseClass.setText(courseData?.courseClass)
        binding.etCourseType.setText(courseData?.type)
        binding.etCourseProperty.setText(courseData?.property)
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        if (dialogId == COLOR_PICKER_DIALOG_ID) {
            synchronized(this) {
                newCourseStyle = (newCourseStyle ?: courseStyle).copy(color = color)
                ImageViewCompat.setImageTintList(binding.ivCourseEditColor, ColorStateList.valueOf(color))
            }
        }
    }

    // 来自ColorPickerDialog
    override fun onDialogDismissed(dialogId: Int) {}

    private fun showConflictMsg(courseTime1: CourseTime, courseTime2: CourseTime) {
        val weekDayNumStrArray = resources.getStringArray(R.array.weekday_num)
        DialogUtils.createBottomMsgDialog(
            this, lifecycle, getString(R.string.time_conflict_title), getString(
                R.string.time_conflict_msg,
                courseTime1.rawWeeksStr, weekDayNumStrArray[courseTime1.weekDay.toInt() - 1], courseTime1.rawCoursesNumStr,
                courseTime2.rawWeeksStr, weekDayNumStrArray[courseTime2.weekDay.toInt() - 1], courseTime2.rawCoursesNumStr
            )
        ).show()
    }

    private fun getEditResult(showAttention: Boolean): Course? {
        val timeSet = courseTimeAdapter.getCourseTimeSet()
        if (timeSet.size == 0) {
            if (showAttention) binding.layoutCourseEdit.showSnackBar(R.string.course_time_empty)
        } else if (timeSet.size != courseTimeAdapter.getCourseTimeList().size) {
            if (showAttention) binding.layoutCourseEdit.showSnackBar(R.string.same_course_time_exists)
        } else {
            val conflictResult = CourseSet.checkCourseTimeConflict(timeSet)
            if (conflictResult == null) {
                val courseName = binding.etCourseName.text?.toString()?.trim()
                if (courseName.isNullOrBlank() || courseName.isNullOrEmpty()) {
                    if (showAttention) binding.layoutCourseEdit.showSnackBar(R.string.course_name_empty)
                } else {
                    return Course(
                        courseId,
                        courseName,
                        getEditText(binding.etTeacherName)!!,
                        getEditText(binding.etCourseClass, true),
                        getEditText(binding.etTeachClass)!!,
                        getEditText(binding.etCourseCredit)?.toFloatOrNull() ?: 0f,
                        getEditText(binding.etCourseType)!!,
                        getEditText(binding.etCourseProperty, true),
                        timeSet
                    )
                }
            } else {
                if (showAttention) showConflictMsg(conflictResult.first, conflictResult.second)
            }
        }
        return null
    }

    private fun getEditText(editText: AppCompatEditText, nullable: Boolean = false): String? {
        val result = editText.text?.toString()?.trim()
        return if (result.isNullOrBlank() || result.isNullOrEmpty()) {
            if (nullable) null else BaseConst.EMPTY
        } else {
            result
        }
    }

    private fun checkSaveForExit() {
        val newCourse = getEditResult(false)
        if (newCourse != courseData || (newCourseStyle ?: courseStyle) != courseStyle) {
            binding.layoutCourseEdit.showSnackBarWithCallback(R.string.exit_edit_without_save, android.R.string.ok) {
                setResult(RESULT_OK)
                super.onBackPressed()
            }
        } else {
            setResult(RESULT_OK)
            super.onBackPressed()
        }
    }
}