package tool.xfy9326.naucourse.ui.activities.base

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.layout_list.*
import kotlinx.android.synthetic.main.layout_refresh_list.*
import kotlinx.android.synthetic.main.view_general_toolbar.*
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.ui.models.base.BaseListViewModel
import tool.xfy9326.naucourse.ui.views.recyclerview.adapters.base.ListRecyclerAdapter
import tool.xfy9326.naucourse.utils.views.ActivityUtils
import tool.xfy9326.naucourse.utils.views.ActivityUtils.enableHomeButton
import tool.xfy9326.naucourse.utils.views.I18NUtils

abstract class ListViewModelActivity<E, T : BaseListViewModel<E>, VH : RecyclerView.ViewHolder> : ViewModelActivity<T>() {
    private lateinit var adapter: ListRecyclerAdapter<VH, E>

    override fun onCreateContentView(): Int = R.layout.layout_refresh_list

    @CallSuper
    override fun bindViewModel(viewModel: T) {
        viewModel.errorMsg.observeEvent(this, Observer {
            ActivityUtils.showSnackBar(layout_refresh_list, I18NUtils.getContentErrorResId(it)!!)
        })
        viewModel.listData.observe(this, Observer {
            adapter.submitList(it)
        })
        viewModel.isRefreshing.observe(this, Observer {
            if (it) {
                asl_refreshLayout.isRefreshing = true
            } else {
                asl_refreshLayout.postStopRefreshing()
            }
        })
    }

    @CallSuper
    override fun initView(savedInstanceState: Bundle?, viewModel: T) {
        setSupportActionBar(tb_general)
        enableHomeButton()

        adapter = onCreateAdapter()
        arv_dataList.adapter = adapter

        asl_refreshLayout.setOnRefreshListener {
            viewModel.getData(forceUpdate = true)
        }
    }

    abstract fun onCreateAdapter(): ListRecyclerAdapter<VH, E>
}