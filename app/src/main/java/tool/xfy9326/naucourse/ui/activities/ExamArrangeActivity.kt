package tool.xfy9326.naucourse.ui.activities

import androidx.lifecycle.ViewModelProvider
import tool.xfy9326.naucourse.providers.beans.jwc.Exam
import tool.xfy9326.naucourse.ui.activities.base.ListViewModelActivity
import tool.xfy9326.naucourse.ui.models.activity.ExamArrangeViewModel
import tool.xfy9326.naucourse.ui.views.recyclerview.adapters.ExamArrangeAdapter
import tool.xfy9326.naucourse.ui.views.recyclerview.adapters.base.ListRecyclerAdapter
import tool.xfy9326.naucourse.ui.views.recyclerview.viewholders.ExamArrangeViewHolder

class ExamArrangeActivity : ListViewModelActivity<Exam, ExamArrangeViewModel, ExamArrangeViewHolder>() {

    override fun onCreateViewModel(): ExamArrangeViewModel = ViewModelProvider(this)[ExamArrangeViewModel::class.java]

    override fun onCreateAdapter(): ListRecyclerAdapter<ExamArrangeViewHolder, Exam> = ExamArrangeAdapter(this)

}