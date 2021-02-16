package tool.xfy9326.naucourse.ui.activities.base

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import tool.xfy9326.naucourse.databinding.LayoutRefreshListBinding
import tool.xfy9326.naucourse.kt.enableHomeButton
import tool.xfy9326.naucourse.kt.showSnackBar
import tool.xfy9326.naucourse.ui.models.base.BaseListViewModel
import tool.xfy9326.naucourse.ui.views.recyclerview.adapters.base.ListRecyclerAdapter
import tool.xfy9326.naucourse.utils.views.I18NUtils

abstract class ListViewModelActivity<E, T : BaseListViewModel<E>, VH : RecyclerView.ViewHolder> : ViewModelActivity<T>() {
    private lateinit var adapter: ListRecyclerAdapter<VH, E>

    private val binding by lazy {
        LayoutRefreshListBinding.inflate(layoutInflater)
    }

    override fun onCreateContentView() = binding.root

    @CallSuper
    override fun bindViewModel(viewModel: T) {
        viewModel.errorMsg.observeEvent(this) {
            binding.layoutRefreshList.showSnackBar(I18NUtils.getContentErrorResId(it)!!)
        }
        viewModel.listData.observe(this) {
            adapter.submitList(it)
        }
        viewModel.isRefreshing.observe(this) {
            if (it) {
                binding.aslRefreshLayout.isRefreshing = true
            } else {
                binding.aslRefreshLayout.postStopRefreshing()
            }
        }
    }

    @CallSuper
    override fun initView(savedInstanceState: Bundle?, viewModel: T) {
        setSupportActionBar(binding.toolbar.tbGeneral)
        enableHomeButton()

        adapter = onCreateAdapter()
        binding.list.arvDataList.adapter = adapter

        binding.aslRefreshLayout.setOnRefreshListener {
            viewModel.getData(forceUpdate = true)
        }
    }

    abstract fun onCreateAdapter(): ListRecyclerAdapter<VH, E>
}