package tool.xfy9326.naucourse.ui.views.viewpager

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import tool.xfy9326.naucourse.ui.fragments.TableFragment

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

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        recyclerView.overScrollMode = View.OVER_SCROLL_NEVER
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun getItemCount(): Int = maxWeekNum

    override fun createFragment(position: Int): Fragment =
        if (position < 0 || position > maxWeekNum - 1) {
            error("Course Table Fragment Can't Generate Table. WeekNum ${position + 1} must in [1,${maxWeekNum}]")
        } else {
            TableFragment().apply {
                arguments = Bundle().apply {
                    putInt(COURSE_TABLE_WEEK_NUM, position + 1)
                }
            }
        }
}