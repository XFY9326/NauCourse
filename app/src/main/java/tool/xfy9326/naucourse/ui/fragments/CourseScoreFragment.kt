package tool.xfy9326.naucourse.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import tool.xfy9326.naucourse.databinding.LayoutListBinding
import tool.xfy9326.naucourse.ui.fragments.base.ViewModelFragment
import tool.xfy9326.naucourse.ui.models.activity.ScoreQueryViewModel
import tool.xfy9326.naucourse.ui.views.recyclerview.adapters.CourseScoreAdapter

class CourseScoreFragment : ViewModelFragment<ScoreQueryViewModel>() {
    private lateinit var adapter: CourseScoreAdapter

    private var _binding: LayoutListBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = view
        return if (v == null) {
            val binding = LayoutListBinding.inflate(layoutInflater, container, false).also {
                this._binding = it
            }
            binding.root
        } else {
            val parent = requireView().parent as ViewGroup?
            parent?.removeView(v)
            _binding = LayoutListBinding.bind(v)
            v
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateViewModel(): ScoreQueryViewModel = ViewModelProvider(requireActivity())[ScoreQueryViewModel::class.java]

    override fun prepareCacheInit(viewModel: ScoreQueryViewModel, isRestored: Boolean) {
        adapter = CourseScoreAdapter(requireContext())
    }

    override fun bindViewModel(viewModel: ScoreQueryViewModel) {
        viewModel.courseScore.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })
        viewModel.scrollToTop.observeNotification(viewLifecycleOwner, CourseScoreFragment::class.java.simpleName) {
            binding.arvDataList.smoothScrollToPosition(0)
        }
    }

    override fun initView(viewModel: ScoreQueryViewModel) {
        binding.arvDataList.adapter = adapter
    }
}