package tool.xfy9326.naucourses.ui.activities

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import kotlinx.android.synthetic.main.activity_course_manage.*
import kotlinx.android.synthetic.main.view_general_toolbar.*
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.beans.CourseCellStyle
import tool.xfy9326.naucourses.providers.beans.jwc.Course
import tool.xfy9326.naucourses.ui.activities.base.ViewModelActivity
import tool.xfy9326.naucourses.ui.models.activity.CourseManageViewModel
import tool.xfy9326.naucourses.ui.views.recyclerview.AdvancedDivider
import tool.xfy9326.naucourses.ui.views.recyclerview.SwipeItemCallback
import tool.xfy9326.naucourses.ui.views.recyclerview.adapters.CourseAdapter
import tool.xfy9326.naucourses.utils.BaseUtils.dpToPx
import tool.xfy9326.naucourses.utils.views.ActivityUtils
import tool.xfy9326.naucourses.utils.views.ActivityUtils.enableHomeButton


class CourseManageActivity : ViewModelActivity<CourseManageViewModel>(), CourseAdapter.Callback {
    companion object {
        private val dividerLeftMargin = 50.dpToPx()
        private val dividerRightMargin = 10.dpToPx()

        private const val COLOR_PICKER_DIALOG_ID = 1
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
                    setMargins(dividerLeftMargin, dividerRightMargin)
                })
            ItemTouchHelper(SwipeItemCallback(courseAdapter)).attachToRecyclerView(this)
            adapter = courseAdapter
        }
    }

    override fun bindViewModel(viewModel: CourseManageViewModel) {
        viewModel.courseManagePkg.observe(this, Observer {
            courseAdapter.setCourseManagePkg(it)
        })
    }

    override fun onCourseDeleted(adapter: CourseAdapter, lastDeleteItem: Pair<Course, CourseCellStyle>, lastDeleteItemPosition: Int) {
        ActivityUtils.showSnackBarWithCallback(layout_courseManage, R.string.delete_course_success, R.string.revoke, View.OnClickListener {
            adapter.recoverCourse(lastDeleteItem, lastDeleteItemPosition)
        })
    }

    override fun onEditCourseColor(adapter: CourseAdapter, position: Int, style: CourseCellStyle) {
        ColorPickerDialog.newBuilder().apply {
            setColor(style.color)
            setDialogTitle(R.string.course_color_edit)
            setDialogId(COLOR_PICKER_DIALOG_ID)
            setPresets(resources.getIntArray(R.array.material_colors_600))
            setShowAlphaSlider(false)
        }.create().apply {
            setColorPickerDialogListener(object : ColorPickerDialogListener {
                private var selectedColor: Int? = null

                override fun onColorSelected(dialogId: Int, color: Int) {
                    if (dialogId == COLOR_PICKER_DIALOG_ID) {
                        selectedColor = color
                    }
                }

                override fun onDialogDismissed(dialogId: Int) {
                    if (dialogId == COLOR_PICKER_DIALOG_ID && selectedColor != null) {
                        style.color = selectedColor!!
                        adapter.updateCourseStyle(position, style)
                    }
                }
            })
        }.show(supportFragmentManager, null)
    }

    override fun onEditCourse(adapter: CourseAdapter, courseItem: Pair<Course, CourseCellStyle>) {

    }
}