package tool.xfy9326.naucourses.ui.views.viewpager

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import tool.xfy9326.naucourses.ui.fragments.CourseTableFragment
import tool.xfy9326.naucourses.ui.fragments.CourseTablePanelFragment

class CourseTableViewPagerAdapter(private val context: CourseTablePanelFragment, private var maxWeekNum: Int) : FragmentStateAdapter(context) {

    fun updateMaxWeekNum(maxWeekNum: Int) {
        if (this.maxWeekNum != maxWeekNum) {
            this.maxWeekNum = maxWeekNum
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = maxWeekNum

    override fun createFragment(position: Int): Fragment =
        if (position < 0 || position > maxWeekNum - 1) {
            throw error("Course Table Fragment Can't Generate Table. WeekNum ${position + 1} must in [1,${maxWeekNum}]")
        } else {
            CourseTableFragment(context, position + 1)
        }
}