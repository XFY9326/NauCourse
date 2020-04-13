package tool.xfy9326.naucourse.ui.fragments

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.layout_list.*
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.ui.fragments.base.ViewModelFragment
import tool.xfy9326.naucourse.ui.models.activity.ScoreQueryViewModel
import tool.xfy9326.naucourse.ui.views.recyclerview.adapters.CourseScoreAdapter

class CourseScoreFragment : ViewModelFragment<ScoreQueryViewModel>() {
    private lateinit var adapter: CourseScoreAdapter

    override fun onCreateContentView(): Int = R.layout.layout_list

    override fun onCreateViewModel(): ScoreQueryViewModel = ViewModelProvider(requireActivity())[ScoreQueryViewModel::class.java]

    override fun prepareCacheInit(viewModel: ScoreQueryViewModel, isRestored: Boolean) {
        adapter = CourseScoreAdapter(requireContext())
    }

    override fun bindViewModel(viewModel: ScoreQueryViewModel) {
        viewModel.courseScore.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })
        viewModel.scrollToTop.observeNotification(viewLifecycleOwner, {
            arv_dataList.smoothScrollToPosition(0)
        }, CourseScoreFragment::class.java.simpleName)
    }

    override fun initView(viewModel: ScoreQueryViewModel) {
        arv_dataList.adapter = adapter
    }
}