package tool.xfy9326.naucourse.ui.views.recyclerview.adapters

import android.content.Context
import android.view.View
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.providers.beans.jwc.Exam
import tool.xfy9326.naucourse.ui.views.recyclerview.adapters.base.ListRecyclerAdapter
import tool.xfy9326.naucourse.ui.views.recyclerview.viewholders.ExamArrangeViewHolder
import tool.xfy9326.naucourse.utils.courses.TimeUtils
import tool.xfy9326.naucourse.utils.views.I18NUtils
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

class ExamArrangeAdapter(context: Context) : ListRecyclerAdapter<ExamArrangeViewHolder, Exam>(context, DifferItemCallback()) {
    companion object {
        private val DATE_FORMAT_YMD_HM_CH = SimpleDateFormat(Constants.Time.FORMAT_YMD_HM_CH, Locale.CHINA)
    }

    private val weakContext = WeakReference(context)

    override fun onBindLayout(): Int = R.layout.view_exam_item

    override fun onCreateViewHolder(view: View): ExamArrangeViewHolder = ExamArrangeViewHolder(view)

    override fun onBindViewHolder(holder: ExamArrangeViewHolder, position: Int, element: Exam) {
        holder.apply {
            tvExamProperty.text = element.property
            tvExamTypeAndCredit.text = weakContext.get()?.getString(R.string.exam_course_credit_and_type, element.credit, element.type)
            tvExamName.text = element.name
            tvExamTeachClass.text = weakContext.get()?.getString(R.string.course_class, element.teachClass)
            tvExamStartTime.text = weakContext.get()?.getString(R.string.exam_start_time, DATE_FORMAT_YMD_HM_CH.format(element.startDate))
            tvExamEndTime.text = weakContext.get()?.getString(R.string.exam_end_time, DATE_FORMAT_YMD_HM_CH.format(element.endDate))
            tvExamLocation.text = weakContext.get()?.getString(R.string.exam_location, element.location)

            val countDown = TimeUtils.getCountDownTime(element.startDate)
            if (countDown == null) {
                layoutExamCountDown.visibility = View.GONE
            } else {
                tvExamCountDown.text = countDown.first.toString()
                tvExamCountDownTimeUnit.setText(I18NUtils.getTimeUnitResId(countDown.second))

                layoutExamCountDown.visibility = View.VISIBLE
            }

        }
    }

    private class DifferItemCallback : SimpleDifferItemCallBack<Exam>() {
        override fun areContentsTheSame(oldItem: Exam, newItem: Exam): Boolean {
            return oldItem.courseId == newItem.courseId
        }
    }
}