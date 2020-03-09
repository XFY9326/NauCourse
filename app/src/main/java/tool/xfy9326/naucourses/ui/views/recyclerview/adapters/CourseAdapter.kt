package tool.xfy9326.naucourses.ui.views.recyclerview.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.beans.CourseCellStyle
import tool.xfy9326.naucourses.beans.CourseManagePkg
import tool.xfy9326.naucourses.providers.beans.jwc.Course
import tool.xfy9326.naucourses.providers.beans.jwc.TermDate
import tool.xfy9326.naucourses.ui.views.recyclerview.viewholders.CourseViewHolder

class CourseAdapter(context: Context, @Volatile private var courseManagePkg: CourseManagePkg?) : RecyclerView.Adapter<CourseViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)
    private val isOperationEnabledLock = Any()

    constructor(context: Context) : this(context, null)

    fun setCourseManagePkg(courseManagePkg: CourseManagePkg) = synchronized(isOperationEnabledLock) {
        this.courseManagePkg = courseManagePkg
        notifyDataSetChanged()
    }

    fun getCourseSet() = courseManagePkg?.getCourseSet()

    fun getCourseStyleArray() = courseManagePkg?.getCourseStyleArray()

    fun removeCourse(position: Int) = synchronized(isOperationEnabledLock) {
        if (courseManagePkg != null) {
            courseManagePkg!!.courses.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun insertCourse(course: Course, style: CourseCellStyle) = synchronized(isOperationEnabledLock) {
        if (courseManagePkg != null) {
            courseManagePkg!!.courses.add(Pair(course, style))
            notifyItemInserted(itemCount - 1)
        }
    }

    fun updateCourse(course: Course) = synchronized(isOperationEnabledLock) {
        if (courseManagePkg != null) {
            val position = courseManagePkg!!.courses.indexOfFirst {
                it.first.id == course.id
            }
            if (position > 0) {
                courseManagePkg!!.courses[position] = Pair(course, courseManagePkg!!.courses[position].second)
                notifyItemChanged(position)
            }
        }
    }

    fun updateCourseStyle(position: Int, style: CourseCellStyle) = synchronized(isOperationEnabledLock) {
        if (courseManagePkg != null) {
            courseManagePkg!!.courses[position] = Pair(courseManagePkg!!.courses[position].first, style)
            notifyItemChanged(position)
        }
    }

    fun updateTermDate(termDate: TermDate) = synchronized(isOperationEnabledLock) {
        if (courseManagePkg != null) {
            courseManagePkg!!.termDate = termDate
            courseManagePkg!!.courseTerm = termDate.getTerm()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder =
        CourseViewHolder(layoutInflater.inflate(R.layout.view_course_manage_item, parent, false))

    override fun getItemCount(): Int = synchronized(isOperationEnabledLock) {
        if (courseManagePkg != null) {
            courseManagePkg!!.courses.size
        } else {
            0
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        if (position < itemCount) {
            val coursePair = synchronized(isOperationEnabledLock) {
                if (courseManagePkg != null) {
                    courseManagePkg!!.courses[position]
                } else {
                    null
                }
            }
            if (coursePair != null) {
                holder.apply {
                    ImageViewCompat.setImageTintList(ivCourseManageColor, ColorStateList.valueOf(coursePair.second.color))

                    tvCourseManageName.text = coursePair.first.name
                    tvCourseManageDetail.text = "${coursePair.first.type}·${coursePair.first.teacher}"

                }
            }
        }
    }
}