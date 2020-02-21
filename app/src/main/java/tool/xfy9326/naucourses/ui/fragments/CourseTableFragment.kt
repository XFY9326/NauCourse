package tool.xfy9326.naucourses.ui.fragments

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_course_table.*
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.ui.fragments.base.ViewModelFragment
import tool.xfy9326.naucourses.ui.models.fragment.CourseTableViewModel
import tool.xfy9326.naucourses.ui.views.builder.CourseTableViewBuilder

class CourseTableFragment(private val fragment: CourseTablePanelFragment, private val weekNum: Int) : ViewModelFragment<CourseTableViewModel>() {
    private var courseTableHash = 0

    override fun onCreateContentView(): Int = R.layout.fragment_course_table

    override fun onCreateViewModel(): CourseTableViewModel =
        ViewModelProvider(fragment)[CourseTableViewModel::class.java].apply {
            courseTablePkg[weekNum - 1].observe(this@CourseTableFragment, Observer {
                courseTableHash = it.courseTable.hashCode()
                val views = CourseTableViewBuilder.generateCourseTableView(weekNum, it.courseTable, it.termDate, gl_courseTable)
                gl_courseTable.removeAllViewsInLayout()
                views.forEach { view ->
                    gl_courseTable.addView(view)
                }
            })
            requestCourseTable(weekNum, courseTableHash)
        }
}