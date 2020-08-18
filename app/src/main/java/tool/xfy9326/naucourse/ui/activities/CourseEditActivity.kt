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
import kotlinx.android.synthetic.main.activity_course_edit.*
import kotlinx.android.synthetic.main.view_general_toolbar.*
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.beans.CourseCellStyle
import tool.xfy9326.naucourse.constants.BaseConst
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            isShowMoreInfoExpanded = it.getBoolean(VIEW_EXPANDED)
            newCourseStyle = it.getSerializable(COURSE_CELL_STYLE) as CourseCellStyle?
        }
        readIntentData()
        setContentView(R.layout.activity_course_edit)
        setSupportActionBar(tb_general)
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
                BaseUtils.hideKeyboard(this, layout_courseEdit.windowToken)

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
        btn_showMoreCourseEditInfo.setOnClickListener {
            currentFocus?.clearFocus()
            BaseUtils.hideKeyboard(this, layout_courseEdit.windowToken)

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

        fab_courseEdit.setOnClickListener {
            CourseTimeEditDialog().apply {
                arguments = Bundle().apply {
                    putString(CourseTimeEditDialog.COURSE_ID, courseId)
                    putInt(CourseTimeEditDialog.MAX_WEEK_NUM, TimeUtils.getWeekLength(termDate))
                }
            }.show(supportFragmentManager, null)
        }

        if (isShowMoreInfoExpanded) {
            btn_showMoreCourseEditInfo.setImageResource(R.drawable.ic_load_less)
            layout_courseEditInfo.visibility = View.VISIBLE
        }

        ImageViewCompat.setImageTintList(iv_courseEditColor, ColorStateList.valueOf((newCourseStyle ?: courseStyle).color))
        layout_courseEditColor.setOnClickListener {
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

        arv_courseDetailList.adapter = courseTimeAdapter
    }

    override fun onDeleteCourseTime(adapter: CourseTimeAdapter, courseTime: CourseTime, position: Int) {
        layout_courseEdit.showSnackBarWithCallback(R.string.delete_course_time_success, R.string.revoke) {
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
        layout_courseEditInfo.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        animateExpand(layout_courseEditInfo.measuredHeight)
        layout_courseEditInfo.animation = AnimUtils.getAnimationFadeVisible(this)
        layout_courseEditInfo.visibility = View.VISIBLE
    }

    private fun loadLess() {
        animateExpand(-layout_courseEditInfo.measuredHeight)
        layout_courseEditInfo.animation = AnimUtils.getAnimationFadeGone(this)
        layout_courseEditInfo.visibility = View.GONE
    }

    private fun animateExpand(changeHeight: Int) =
        ValueAnimator.ofInt(cv_courseBaseInfo.measuredHeight, cv_courseBaseInfo.measuredHeight + changeHeight).apply {
            duration = EXPAND_ANIMATION_DURATION
            interpolator = FastOutSlowInInterpolator()
            addUpdateListener {
                val value = animatedValue as Int
                cv_courseBaseInfo.layoutParams.height = value
                cv_courseBaseInfo.requestLayout()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    btn_showMoreCourseEditInfo.visibility = View.INVISIBLE
                }

                override fun onAnimationEnd(animation: Animator?) = resetAfterExpandLayout()
                override fun onAnimationCancel(animation: Animator?) = resetAfterExpandLayout()
            })
            start()
        }

    private fun resetAfterExpandLayout() {
        cv_courseBaseInfo.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        cv_courseBaseInfo.requestLayout()

        btn_showMoreCourseEditInfo.animation = AnimUtils.getAnimationFadeVisible(this@CourseEditActivity)
        btn_showMoreCourseEditInfo.visibility = View.VISIBLE
    }

    private fun applyCourseInfoData() {
        et_courseName.setText(courseData?.name)
        et_teacherName.setText(courseData?.teacher)
        et_courseCredit.setText(courseData?.credit?.toString())
        et_teachClass.setText(courseData?.teachClass)
        et_courseClass.setText(courseData?.courseClass)
        et_courseType.setText(courseData?.type)
        et_courseProperty.setText(courseData?.property)
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        if (dialogId == COLOR_PICKER_DIALOG_ID) {
            synchronized(this) {
                newCourseStyle = (newCourseStyle ?: courseStyle).copy(color = color)
                ImageViewCompat.setImageTintList(iv_courseEditColor, ColorStateList.valueOf(color))
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
            if (showAttention) layout_courseEdit.showSnackBar(R.string.course_time_empty)
        } else if (timeSet.size != courseTimeAdapter.getCourseTimeList().size) {
            if (showAttention) layout_courseEdit.showSnackBar(R.string.same_course_time_exists)
        } else {
            val conflictResult = CourseSet.checkCourseTimeConflict(timeSet)
            if (conflictResult == null) {
                val courseName = et_courseName.text?.toString()?.trim()
                if (courseName.isNullOrBlank() || courseName.isNullOrEmpty()) {
                    if (showAttention) layout_courseEdit.showSnackBar(R.string.course_name_empty)
                } else {
                    return Course(
                        courseId,
                        courseName,
                        getEditText(et_teacherName)!!,
                        getEditText(et_courseClass, true),
                        getEditText(et_teachClass)!!,
                        getEditText(et_courseCredit)?.toFloatOrNull() ?: 0f,
                        getEditText(et_courseType)!!,
                        getEditText(et_courseProperty, true),
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
            layout_courseEdit.showSnackBarWithCallback(R.string.exit_edit_without_save, android.R.string.ok) {
                setResult(RESULT_OK)
                super.onBackPressed()
            }
        } else {
            setResult(RESULT_OK)
            super.onBackPressed()
        }
    }
}