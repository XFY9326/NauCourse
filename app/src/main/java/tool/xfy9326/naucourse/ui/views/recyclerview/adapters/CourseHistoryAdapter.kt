package tool.xfy9326.naucourse.ui.views.recyclerview.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.providers.beans.jwc.CourseHistory
import tool.xfy9326.naucourse.ui.views.recyclerview.adapters.base.ListRecyclerAdapter
import tool.xfy9326.naucourse.ui.views.recyclerview.viewholders.CourseHistoryViewHolder
import java.lang.ref.WeakReference

class CourseHistoryAdapter(context: Context) : ListRecyclerAdapter<CourseHistoryViewHolder, CourseHistory>(context) {
    private val weakContext = WeakReference(context)

    override fun onBindLayout(): Int = R.layout.view_course_history_item

    override fun onCreateViewHolder(view: View): CourseHistoryViewHolder = CourseHistoryViewHolder(view)

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CourseHistoryViewHolder, position: Int, element: CourseHistory) {
        holder.apply {
            tvCourseHistoryName.text = element.name
            tvCourseHistoryProperty.text = "${element.academicProperty} ${element.courseProperty}".trim()
            tvCourseHistoryTerm.text = element.term.toString()
            tvCourseHistoryCredit.text = weakContext.get()?.getString(R.string.course_credit, element.credit)
            tvCourseHistoryWeight.text = weakContext.get()?.getString(R.string.credit_weight, element.creditWeight)
            tvCourseHistoryScore.text = weakContext.get()?.getString(R.string.score, element.scoreRawText)

            if (element.type.isEmpty() || element.type.isBlank()) {
                tvCourseHistoryType.visibility = View.GONE
            } else {
                tvCourseHistoryType.text = weakContext.get()?.getString(R.string.type, element.type)
                tvCourseHistoryType.visibility = View.VISIBLE
            }

            if (element.notes.isEmpty() || element.notes.isBlank()) {
                tvCourseHistoryNote.visibility = View.GONE
            } else {
                tvCourseHistoryNote.text = weakContext.get()?.getString(R.string.notes, element.notes)
                tvCourseHistoryNote.visibility = View.VISIBLE
            }
        }
    }
}