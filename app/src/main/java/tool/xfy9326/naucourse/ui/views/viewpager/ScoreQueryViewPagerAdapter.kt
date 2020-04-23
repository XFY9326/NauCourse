package tool.xfy9326.naucourse.ui.views.viewpager

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import tool.xfy9326.naucourse.ui.fragments.CourseHistoryFragment
import tool.xfy9326.naucourse.ui.fragments.CourseScoreFragment

class ScoreQueryViewPagerAdapter(context: FragmentActivity) : FragmentStateAdapter(context) {
    companion object {
        private const val SCORE_QUERY_PAGE_COUNT = 2
    }

    override fun getItemCount(): Int = SCORE_QUERY_PAGE_COUNT

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.overScrollMode = View.OVER_SCROLL_NEVER
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun createFragment(position: Int): Fragment =
        when (position) {
            0 -> CourseScoreFragment()
            1 -> CourseHistoryFragment()
            else -> error("Score Query Fragment Create Failed! Index: $position")
        }
}