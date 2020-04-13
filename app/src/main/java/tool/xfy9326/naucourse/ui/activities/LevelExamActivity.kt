package tool.xfy9326.naucourse.ui.activities

import androidx.lifecycle.ViewModelProvider
import tool.xfy9326.naucourse.providers.beans.jwc.LevelExam
import tool.xfy9326.naucourse.ui.activities.base.ListViewModelActivity
import tool.xfy9326.naucourse.ui.models.activity.LevelExamViewModel
import tool.xfy9326.naucourse.ui.views.recyclerview.adapters.LevelExamAdapter
import tool.xfy9326.naucourse.ui.views.recyclerview.adapters.base.ListRecyclerAdapter
import tool.xfy9326.naucourse.ui.views.recyclerview.viewholders.LevelExamViewHolder

class LevelExamActivity : ListViewModelActivity<LevelExam, LevelExamViewModel, LevelExamViewHolder>() {

    override fun onCreateViewModel(): LevelExamViewModel = ViewModelProvider(this)[LevelExamViewModel::class.java]

    override fun onCreateAdapter(): ListRecyclerAdapter<LevelExamViewHolder, LevelExam> = LevelExamAdapter(this)

}