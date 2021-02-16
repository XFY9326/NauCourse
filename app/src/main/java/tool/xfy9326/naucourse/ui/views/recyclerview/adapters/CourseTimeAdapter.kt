package tool.xfy9326.naucourse.ui.views.recyclerview.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.databinding.ViewCourseTimeItemBinding
import tool.xfy9326.naucourse.providers.beans.jwc.CourseTime
import tool.xfy9326.naucourse.ui.views.recyclerview.viewholders.CourseTimeViewHolder
import java.lang.ref.WeakReference

class CourseTimeAdapter(context: Context, private val timeList: ArrayList<CourseTime>, private val listener: CourseTimeCallback) :
    RecyclerView.Adapter<CourseTimeViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)
    private val contextReference = WeakReference(context)
    private val weekDayStrArray = context.resources.getStringArray(R.array.weekday_num)

    override fun getItemCount(): Int = timeList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseTimeViewHolder =
        CourseTimeViewHolder(ViewCourseTimeItemBinding.inflate(layoutInflater, parent, false))

    fun appendCourseTime(courseTime: CourseTime) {
        timeList.add(courseTime)
        notifyItemInserted(timeList.size - 1)
    }

    fun editCourseTime(courseTime: CourseTime, position: Int) {
        timeList[position] = courseTime
        notifyItemChanged(position)
    }

    fun recoverCourseTime(courseTime: CourseTime, position: Int) {
        timeList.add(position, courseTime)
        if (position == 0 && timeList.size == 1) {
            notifyDataSetChanged()
        } else {
            notifyItemInserted(position)
        }
    }

    fun getCourseTimeSet(): HashSet<CourseTime> = timeList.toHashSet()

    fun getCourseTimeList(): ArrayList<CourseTime> = timeList

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CourseTimeViewHolder, position: Int) {
        val courseTime = timeList[position]
        holder.apply {
            tvCourseTimeWeeks.text = contextReference.get()!!.getString(R.string.course_detail_weeks, courseTime.rawWeeksStr)
            tvCourseTimeCourses.text = contextReference.get()!!.getString(
                R.string.course_detail_time, weekDayStrArray[courseTime.weekDay - 1],
                courseTime.rawCoursesNumStr
            )
            tvCourseTimeLocation.text = contextReference.get()!!.getString(R.string.course_detail_location, courseTime.location)
            layoutCourseTimeDelete.setOnClickListener {
                timeList.remove(courseTime)
                listener.onDeleteCourseTime(this@CourseTimeAdapter, courseTime, holder.adapterPosition)
                notifyItemRemoved(holder.adapterPosition)
            }
            cvCourseEditTime.setOnClickListener {
                listener.onEditCourseTime(this@CourseTimeAdapter, courseTime, holder.adapterPosition)
            }
        }
    }

    interface CourseTimeCallback {
        fun onDeleteCourseTime(adapter: CourseTimeAdapter, courseTime: CourseTime, position: Int)

        fun onEditCourseTime(adapter: CourseTimeAdapter, courseTime: CourseTime, position: Int)
    }
}