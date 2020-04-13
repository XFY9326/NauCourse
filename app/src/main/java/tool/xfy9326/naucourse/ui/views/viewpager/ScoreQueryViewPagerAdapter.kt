package tool.xfy9326.naucourse.ui.views.viewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import tool.xfy9326.naucourse.ui.fragments.CourseHistoryFragment
import tool.xfy9326.naucourse.ui.fragments.CourseScoreFragment

class ScoreQueryViewPagerAdapter(context: FragmentActivity) : FragmentStateAdapter(context) {
    companion object {
        private const val SCORE_QUERY_PAGE_COUNT = 2
    }

    override fun getItemCount(): Int = SCORE_QUERY_PAGE_COUNT

    override fun createFragment(position: Int): Fragment =
        when (position) {
            0 -> CourseScoreFragment()
            1 -> CourseHistoryFragment()
            else -> throw error("Score Query Fragment Create Failed! Index: $position")
        }
}