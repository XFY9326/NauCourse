package tool.xfy9326.naucourse.ui.views.recyclerview.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import tool.xfy9326.naucourse.beans.CourseCellStyle
import tool.xfy9326.naucourse.beans.CourseManagePkg
import tool.xfy9326.naucourse.databinding.ViewCourseManageItemBinding
import tool.xfy9326.naucourse.providers.beans.jwc.Course
import tool.xfy9326.naucourse.providers.beans.jwc.TermDate
import tool.xfy9326.naucourse.ui.views.recyclerview.SwipeItemCallback
import tool.xfy9326.naucourse.ui.views.recyclerview.viewholders.CourseViewHolder
import tool.xfy9326.naucourse.utils.courses.CourseUtils
import tool.xfy9326.naucourse.utils.views.ViewUtils

class CourseAdapter(context: Context, @Volatile private var courseManagePkg: CourseManagePkg?, private val callback: Callback) :
    RecyclerView.Adapter<CourseViewHolder>(), SwipeItemCallback.OnItemSwipedListener<CourseViewHolder> {

    private val layoutInflater = LayoutInflater.from(context)
    private val isOperationEnabledLock = Any()

    constructor(context: Context, listener: Callback) : this(context, null, listener)

    fun setCourseManagePkg(courseManagePkg: CourseManagePkg) = synchronized(isOperationEnabledLock) {
        this.courseManagePkg = courseManagePkg
        notifyDataSetChanged()
    }

    fun getCourseSet() = courseManagePkg?.getCourseSet()

    fun getTermDate() = courseManagePkg?.termDate

    fun getCourseStyleArray() = courseManagePkg?.getCourseStyleArray()

    private fun removeCourse(position: Int) = synchronized(isOperationEnabledLock) {
        if (courseManagePkg != null) {
            val deleteItem = courseManagePkg!!.courses.removeAt(position)
            notifyItemRemoved(position)
            callback.onCourseDeleted(this, deleteItem, position)
        }
    }

    fun deleteAllCourses() = synchronized(isOperationEnabledLock) {
        if (courseManagePkg != null) {
            courseManagePkg!!.courses.clear()
            notifyDataSetChanged()
        }
    }

    fun recoverCourse(lastDeleteItem: Pair<Course, CourseCellStyle>, lastDeleteItemPosition: Int) = synchronized(isOperationEnabledLock) {
        if (courseManagePkg != null) {
            courseManagePkg!!.courses.add(lastDeleteItemPosition, lastDeleteItem)
            if (lastDeleteItemPosition == 0 && courseManagePkg!!.courses.size == 1) {
                notifyDataSetChanged()
            } else {
                notifyItemInserted(lastDeleteItemPosition)
            }
        }
    }

    fun insertCourse(course: Course, style: CourseCellStyle) = synchronized(isOperationEnabledLock) {
        if (courseManagePkg != null) {
            courseManagePkg!!.courses.add(Pair(course, style))
            notifyItemInserted(itemCount - 1)
        }
    }

    fun updateCourse(course: Course): Int? = synchronized(isOperationEnabledLock) {
        if (courseManagePkg != null) {
            val position = courseManagePkg!!.courses.indexOfFirst {
                it.first.id == course.id
            }
            if (position >= 0) {
                courseManagePkg!!.courses[position] = Pair(course, courseManagePkg!!.courses[position].second)
                notifyItemChanged(position)
                return position
            }
        }
        return null
    }

    fun importCourse(courses: ArrayList<Course>, clearAll: Boolean) = synchronized(isOperationEnabledLock) {
        if (courseManagePkg != null) {
            val result = CourseUtils.importCourseToList(
                if (clearAll) {
                    null
                } else {
                    courseManagePkg!!.courses
                }, courses
            ).sortedBy {
                it.first.id
            }
            courseManagePkg!!.courses.clear()
            courseManagePkg!!.courses.addAll(result)
            notifyDataSetChanged()
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

    override fun onSwipedItem(viewHolder: CourseViewHolder, position: Int) = removeCourse(position)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder =
        CourseViewHolder(ViewCourseManageItemBinding.inflate(layoutInflater, parent, false))

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
                    tvCourseManageDetail.text =
                        ViewUtils.getCourseDataShowText("${coursePair.first.type}${ViewUtils.COURSE_DATA_JOIN_SYMBOL}${coursePair.first.teacher}")

                    layoutCourseManageColor.setOnClickListener {
                        synchronized(isOperationEnabledLock) {
                            if (courseManagePkg != null) {
                                callback.onEditCourseColor(
                                    this@CourseAdapter,
                                    holder.adapterPosition,
                                    courseManagePkg!!.courses[holder.adapterPosition].second
                                )
                            }
                        }
                    }

                    layoutCourseManageItem.setOnClickListener {
                        synchronized(isOperationEnabledLock) {
                            if (courseManagePkg != null) {
                                if (courseManagePkg != null) {
                                    callback.onEditCourse(
                                        this@CourseAdapter,
                                        courseManagePkg!!.termDate,
                                        courseManagePkg!!.courses[holder.adapterPosition]
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    interface Callback {
        fun onCourseDeleted(adapter: CourseAdapter, lastDeleteItem: Pair<Course, CourseCellStyle>, lastDeleteItemPosition: Int)

        fun onEditCourseColor(adapter: CourseAdapter, position: Int, style: CourseCellStyle)

        fun onEditCourse(adapter: CourseAdapter, termDate: TermDate, courseItem: Pair<Course, CourseCellStyle>)
    }
}