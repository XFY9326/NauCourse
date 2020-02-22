package tool.xfy9326.naucourses.ui.views.viewpager

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import tool.xfy9326.naucourses.ui.fragments.TableFragment

class CourseTableViewPagerAdapter(context: Fragment, private var maxWeekNum: Int) : FragmentStateAdapter(context) {

    companion object {
        const val COURSE_TABLE_WEEK_NUM = "COURSE_TABLE_WEEK_NUM"
    }

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
            TableFragment().apply {
                arguments = Bundle().apply {
                    putInt(COURSE_TABLE_WEEK_NUM, position + 1)
                }
            }
        }
}