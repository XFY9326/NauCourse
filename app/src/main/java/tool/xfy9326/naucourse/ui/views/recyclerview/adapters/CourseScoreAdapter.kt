package tool.xfy9326.naucourse.ui.views.recyclerview.adapters

import android.content.Context
import android.view.View
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.constants.BaseConst
import tool.xfy9326.naucourse.providers.beans.jwc.CourseScore
import tool.xfy9326.naucourse.ui.views.recyclerview.adapters.base.ListRecyclerAdapter
import tool.xfy9326.naucourse.ui.views.recyclerview.viewholders.CourseScoreViewHolder
import java.lang.ref.WeakReference

class CourseScoreAdapter(context: Context) : ListRecyclerAdapter<CourseScoreViewHolder, CourseScore>(context, DifferItemCallback()) {
    private val weakContext = WeakReference(context)

    override fun onBindLayout(): Int = R.layout.view_course_score_item

    override fun onCreateViewHolder(view: View): CourseScoreViewHolder = CourseScoreViewHolder(view)

    override fun onBindViewHolder(holder: CourseScoreViewHolder, position: Int, element: CourseScore) {
        holder.apply {
            tvCourseScoreName.text = element.name
            tvCourseScoreCourseType.text = element.type
            tvCourseScoreCourseProperty.text = element.property
            tvCourseScoreClass.text = weakContext.get()?.getString(R.string.course_class, element.teachClass)
            tvCourseScoreOrdinary.text = weakContext.get()?.getString(R.string.ordinary_score, getShowScoreText(element.ordinaryGrades, element))
            tvCourseScoreMidTerm.text = weakContext.get()?.getString(R.string.mid_term_score, getShowScoreText(element.midTermGrades, element))
            tvCourseScoreFinalTerm.text = weakContext.get()?.getString(R.string.final_term_score, getShowScoreText(element.finalTermGrades, element))
            tvCourseScoreCredit.text = weakContext.get()?.getString(R.string.course_credit, element.credit)
            tvCourseScoreOverAll.text = weakContext.get()?.getString(R.string.over_all_score, getShowScoreText(element.overAllGrades, element))
        }
    }

    private fun getShowScoreText(score: Float, element: CourseScore): String {
        if (element.notEntry) return weakContext.get()?.getString(R.string.score_not_entry)!!
        if (element.notMeasure) return weakContext.get()?.getString(R.string.score_not_measure)!!
        if (element.notPublish) return weakContext.get()?.getString(R.string.score_not_publish)!!
        return String.format(BaseConst.KEEP_TWO_DECIMAL_PLACES, score)
    }

    private class DifferItemCallback : SimpleDifferItemCallBack<CourseScore>() {
        override fun areContentsTheSame(oldItem: CourseScore, newItem: CourseScore): Boolean {
            return oldItem.courseId == newItem.courseId
        }
    }
}