package tool.xfy9326.naucourse.ui.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import kotlinx.android.synthetic.main.activity_course_manage.*
import kotlinx.android.synthetic.main.view_general_toolbar.*
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.beans.CourseCellStyle
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.providers.beans.jwc.*
import tool.xfy9326.naucourse.ui.activities.base.ViewModelActivity
import tool.xfy9326.naucourse.ui.dialogs.CourseImportDialog
import tool.xfy9326.naucourse.ui.dialogs.FullScreenLoadingDialog
import tool.xfy9326.naucourse.ui.dialogs.TermDateEditDialog
import tool.xfy9326.naucourse.ui.dialogs.TermDatePickerDialog
import tool.xfy9326.naucourse.ui.models.activity.CourseManageViewModel
import tool.xfy9326.naucourse.ui.views.recyclerview.AdvancedDivider
import tool.xfy9326.naucourse.ui.views.recyclerview.SwipeItemCallback
import tool.xfy9326.naucourse.ui.views.recyclerview.adapters.CourseAdapter
import tool.xfy9326.naucourse.utils.courses.TimeUtils
import tool.xfy9326.naucourse.utils.views.ActivityUtils.enableHomeButton
import tool.xfy9326.naucourse.utils.views.ActivityUtils.showSnackBar
import tool.xfy9326.naucourse.utils.views.ActivityUtils.showSnackBarWithCallback
import tool.xfy9326.naucourse.utils.views.ActivityUtils.showToast
import tool.xfy9326.naucourse.utils.views.DialogUtils
import java.util.*


class CourseManageActivity : ViewModelActivity<CourseManageViewModel>(), CourseAdapter.Callback, TermDateEditDialog.OnTermEditListener,
    ColorPickerDialogListener, TermDatePickerDialog.DatePickDialogCallback, CourseImportDialog.CourseImportCallback {
    companion object {
        private const val COLOR_PICKER_DIALOG_ID = 1

        private const val COURSE_EDIT_RESULT = 1
        private const val COURSE_ADD_RESULT = 2
    }

    private lateinit var courseAdapter: CourseAdapter

    override fun onCreateContentView(): Int = R.layout.activity_course_manage

    override fun onCreateViewModel(): CourseManageViewModel = ViewModelProvider(this)[CourseManageViewModel::class.java]

    override fun initView(savedInstanceState: Bundle?, viewModel: CourseManageViewModel) {
        setSupportActionBar(tb_general)
        enableHomeButton()

        courseAdapter = CourseAdapter(this, this)

        arv_courseManageList.apply {
            addItemDecoration(
                AdvancedDivider(
                    this@CourseManageActivity,
                    DividerItemDecoration.VERTICAL
                ).apply {
                    setMargins(
                        resources.getDimensionPixelSize(R.dimen.course_manage_divider_left_margin),
                        resources.getDimensionPixelSize(R.dimen.course_manage_divider_right_margin)
                    )
                })
            ItemTouchHelper(SwipeItemCallback(courseAdapter)).attachToRecyclerView(this)
            adapter = courseAdapter
        }

        fab_courseManage.setOnClickListener {
            DialogUtils.createCourseAddDialog(this, lifecycle,
                DialogInterface.OnClickListener { _, which ->
                    when (which) {
                        0 -> addNewCourse()
                        1 -> {
                            FullScreenLoadingDialog.showDialog(supportFragmentManager)
                            getViewModel().importCourse(CourseManageViewModel.ImportCourseType.CURRENT_TERM)
                        }
                        2 -> {
                            FullScreenLoadingDialog.showDialog(supportFragmentManager)
                            getViewModel().importCourse(CourseManageViewModel.ImportCourseType.NEXT_TERM)
                        }
                    }
                }).show()
        }

        if (!AppPref.EditAsyncCourseAttention && SettingsPref.AutoAsyncCourseData) {
            DialogUtils.createEditAsyncCourseAttention(this, lifecycle).show()
        }
    }

    private fun addNewCourse() =
        startActivityForResult(
            Intent(this, CourseEditActivity::class.java).putExtra(CourseEditActivity.TERM_DATE, courseAdapter.getTermDate()),
            COURSE_ADD_RESULT
        )

    override fun bindViewModel(viewModel: CourseManageViewModel) {
        viewModel.courseManagePkg.observe(this, Observer {
            courseAdapter.setCourseManagePkg(it)
        })
        viewModel.saveSuccess.observeNotification(this, {
            super.onBackPressed()
        })
        viewModel.rawTermDate.observeEvent(this, Observer {
            courseAdapter.updateTermDate(it)
        })
        viewModel.importCourseResult.observeEvent(this, Observer {
            FullScreenLoadingDialog.close(supportFragmentManager)
            if (it == null) {
                showSnackBar(layout_courseManage, R.string.course_import_failed)
            } else {
                CourseImportDialog().apply {
                    arguments = Bundle().apply {
                        putSerializable(CourseImportDialog.COURSE_SET, it.first)
                        putSerializable(CourseImportDialog.COURSE_TYPE, it.second)
                    }
                    isCancelable = false
                }.show(supportFragmentManager, null)
            }
        })
    }

    override fun onCourseDeleted(adapter: CourseAdapter, lastDeleteItem: Pair<Course, CourseCellStyle>, lastDeleteItemPosition: Int) {
        getViewModel().setDataChanged()
        showSnackBarWithCallback(layout_courseManage, R.string.delete_course_success, R.string.revoke, View.OnClickListener {
            adapter.recoverCourse(lastDeleteItem, lastDeleteItemPosition)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_course_manage, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                checkSaveForExit()
                return true
            }
            R.id.menu_courseManageTermDate -> {
                val term = courseAdapter.getTermDate()
                if (term != null) {
                    startTermDateEditDialog(term)
                } else {
                    showSnackBar(layout_courseManage, R.string.term_date_empty)
                }
            }
            R.id.menu_courseManageDeleteAll -> {
                showSnackBarWithCallback(layout_courseManage, R.string.delete_all_courses_msg, android.R.string.yes, View.OnClickListener {
                    getViewModel().setDataChanged()
                    courseAdapter.deleteAllCourses()
                    showSnackBar(layout_courseManage, R.string.delete_course_success)
                })
            }
            R.id.menu_courseManageSave ->
                if (getViewModel().dataChanged) {
                    val editResult = getEditResult()
                    if (editResult != null) {
                        getViewModel().setDataChanged()
                        getViewModel().saveAll(editResult.first, editResult.second, editResult.third)
                    }
                } else {
                    super.onBackPressed()
                }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startTermDateEditDialog(termDate: TermDate) {
        TermDateEditDialog().apply {
            arguments = Bundle().apply {
                putSerializable(TermDateEditDialog.TERM_DATE, termDate)
            }
        }.show(supportFragmentManager, null)
    }

    override fun onTermDateChanged(termDate: TermDate) {
        if (courseAdapter.getTermDate() != termDate) {
            courseAdapter.updateTermDate(termDate)
            getViewModel().requireCleanTermDate = false
            getViewModel().setDataChanged()
        }
    }

    override fun onTermCustomClear() {
        getViewModel().requireCleanTermDate = true
        getViewModel().refreshRawTermDate()
        getViewModel().setDataChanged()
    }

    override fun onTermDatePartSet(date: Date, dateType: TermDatePickerDialog.DateType, termDate: TermDate) {
        var newTermDate = termDate
        when (dateType) {
            TermDatePickerDialog.DateType.START_DATE ->
                if (date >= termDate.endDate) {
                    showToast(R.string.term_date_error_start)
                } else {
                    newTermDate = TermDate(date, termDate.endDate)
                }
            TermDatePickerDialog.DateType.END_DATE ->
                if (date <= termDate.startDate) {
                    showToast(R.string.term_date_error_end)
                } else {
                    newTermDate = TermDate(termDate.startDate, date)
                }
        }
        val weekLength = TimeUtils.getWeekLength(newTermDate, true)
        if (weekLength < Constants.Course.MIN_WEEK_NUM_SIZE || weekLength > Constants.Course.MAX_WEEK_NUM_SIZE) {
            showToast(R.string.term_date_length_error, Constants.Course.MIN_WEEK_NUM_SIZE, Constants.Course.MAX_WEEK_NUM_SIZE, weekLength)
            newTermDate = termDate
        }
        startTermDateEditDialog(newTermDate)
    }

    override fun onTermDatePartEditCanceled(termDate: TermDate) = startTermDateEditDialog(termDate)

    override fun onBackPressed() = checkSaveForExit()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            if (data != null) {
                val course = data.getSerializableExtra(CourseEditActivity.COURSE_DATA) as Course?
                val courseCellStyle = data.getSerializableExtra(CourseEditActivity.COURSE_CELL_STYLE) as CourseCellStyle?
                if (course != null && courseCellStyle != null) {
                    if (requestCode == COURSE_EDIT_RESULT) {
                        val position = courseAdapter.updateCourse(course)
                        if (position != null) {
                            getViewModel().setDataChanged()
                            courseAdapter.updateCourseStyle(position, courseCellStyle)
                        } else {
                            showSnackBar(layout_courseManage, R.string.course_edit_failed)
                        }
                    } else if (requestCode == COURSE_ADD_RESULT) {
                        getViewModel().setDataChanged()
                        courseAdapter.insertCourse(course, courseCellStyle)
                    }
                }
            }
        } else {
            showSnackBar(layout_courseManage, R.string.course_edit_failed)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        System.gc()
        super.onDestroy()
    }

    override fun onCourseImport(courses: ArrayList<Course>, term: Term, type: CourseManageViewModel.ImportCourseType) {
        getViewModel().setDataChanged()
        if (type == CourseManageViewModel.ImportCourseType.NEXT_TERM) {
            DialogUtils.createBottomMsgDialog(
                this, lifecycle, getString(R.string.next_course_import_attention),
                getString(R.string.next_course_import_attention_msg, term.toString())
            ).show()
        }
        courseAdapter.importCourse(courses)
    }

    override fun onEditCourseColor(adapter: CourseAdapter, position: Int, style: CourseCellStyle) {
        DialogUtils.createCourseColorPickerDialog(this, style.color, COLOR_PICKER_DIALOG_ID).show(supportFragmentManager, null).also {
            synchronized(this) {
                getViewModel().apply {
                    colorEditStyle = style
                    colorEditPosition = position
                }
            }
        }
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        if (dialogId == COLOR_PICKER_DIALOG_ID) {
            synchronized(this) {
                // DialogFragment旋转后注册的listener会丢失，因此使用Activity作为监听
                // 针对第三方的DialogFragment无法更改接口，只能在外部存储数据
                getViewModel().apply {
                    if (colorEditPosition != null && colorEditStyle != null) {
                        if (colorEditStyle!!.color != color) {
                            colorEditStyle!!.color = color
                            courseAdapter.updateCourseStyle(colorEditPosition!!, colorEditStyle!!)
                            getViewModel().setDataChanged()
                        }
                    }
                    colorEditStyle = null
                    colorEditPosition = null
                }
            }
        }
    }

    // 来自ColorPickerDialog
    override fun onDialogDismissed(dialogId: Int) {}

    override fun onEditCourse(adapter: CourseAdapter, termDate: TermDate, courseItem: Pair<Course, CourseCellStyle>) {
        startActivityForResult(
            Intent(this, CourseEditActivity::class.java)
                .putExtra(CourseEditActivity.TERM_DATE, termDate)
                .putExtra(CourseEditActivity.COURSE_DATA, courseItem.first)
                .putExtra(CourseEditActivity.COURSE_CELL_STYLE, courseItem.second),
            COURSE_EDIT_RESULT
        )
    }

    private fun showConflictMsg(course1: Course, courseTime1: CourseTime, course2: Course, courseTime2: CourseTime) {
        val weekDayNumStrArray = resources.getStringArray(R.array.weekday_num)
        DialogUtils.createBottomMsgDialog(
            this, lifecycle, getString(R.string.time_conflict_title), getString(
                R.string.course_conflict_msg,
                course1.name,
                courseTime1.rawWeeksStr, weekDayNumStrArray[courseTime1.weekDay.toInt() - 1], courseTime1.rawCoursesNumStr,
                course2.name,
                courseTime2.rawWeeksStr, weekDayNumStrArray[courseTime2.weekDay.toInt() - 1], courseTime2.rawCoursesNumStr
            )
        ).show()
    }

    private fun getEditResult(): Triple<CourseSet, Array<CourseCellStyle>, TermDate>? {
        val courseSet = courseAdapter.getCourseSet()
        if (courseSet != null) {
            val conflictResult = CourseSet.checkCourseTimeConflict(courseSet.courses)
            if (conflictResult.isSuccess) {
                val styles = courseAdapter.getCourseStyleArray()
                val termDate = courseAdapter.getTermDate()
                if (styles != null && termDate != null) {
                    // TermDate更新时会更新CourseSet的学期，导入课程时不会更新CourseSet的学期
                    // 因此此处只是常规检查，为以后留下可用接口
                    if (courseSet.term == termDate.getTerm()) {
                        return Triple(courseSet, styles, termDate)
                    } else {
                        showSnackBar(layout_courseManage, R.string.course_term_error)
                    }
                } else {
                    showSnackBar(layout_courseManage, R.string.save_failed)
                }
            } else {
                showConflictMsg(
                    conflictResult.conflictCourse1!!, conflictResult.conflictCourseTime1!!,
                    conflictResult.conflictCourse2!!, conflictResult.conflictCourseTime2!!
                )
            }
        } else {
            showSnackBar(layout_courseManage, R.string.save_failed)
        }
        return null
    }

    private fun checkSaveForExit() {
        if (getViewModel().dataChanged) {
            showSnackBarWithCallback(layout_courseManage, R.string.exit_edit_without_save, android.R.string.yes, View.OnClickListener {
                super.onBackPressed()
            })
        } else {
            super.onBackPressed()
        }
    }
}