package tool.xfy9326.naucourse.ui.activities

import androidx.lifecycle.ViewModelProvider
import tool.xfy9326.naucourse.providers.beans.jwc.SuspendCourse
import tool.xfy9326.naucourse.ui.activities.base.ListViewModelActivity
import tool.xfy9326.naucourse.ui.models.activity.SuspendCourseViewModel
import tool.xfy9326.naucourse.ui.views.recyclerview.adapters.SuspendCourseAdapter
import tool.xfy9326.naucourse.ui.views.recyclerview.adapters.base.ListRecyclerAdapter
import tool.xfy9326.naucourse.ui.views.recyclerview.viewholders.SuspendCourseViewHolder

class SuspendCourseActivity : ListViewModelActivity<SuspendCourse, SuspendCourseViewModel, SuspendCourseViewHolder>() {
    override fun onCreateAdapter(): ListRecyclerAdapter<SuspendCourseViewHolder, SuspendCourse> = SuspendCourseAdapter(this)

    override fun onCreateViewModel(): SuspendCourseViewModel = ViewModelProvider(this)[SuspendCourseViewModel::class.java]
}