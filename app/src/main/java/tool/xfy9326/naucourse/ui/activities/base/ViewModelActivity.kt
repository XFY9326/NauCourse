package tool.xfy9326.naucourse.ui.activities.base

import android.os.Bundle
import android.view.View
import tool.xfy9326.naucourse.ui.models.base.BaseViewModel

abstract class ViewModelActivity<T : BaseViewModel> : BaseActivity() {
    private lateinit var contentViewModel: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(onCreateContentView())
        initActivity(savedInstanceState)
    }

    private fun initActivity(savedInstanceState: Bundle?) {
        contentViewModel = onCreateViewModel()
        contentViewModel.onInitCache(savedInstanceState != null)
        bindViewModel(contentViewModel)
        initView(savedInstanceState, contentViewModel)
        contentViewModel.onInitView(savedInstanceState != null)
    }

    protected fun getViewModel(): T = contentViewModel

    protected abstract fun onCreateContentView(): View

    protected abstract fun onCreateViewModel(): T

    protected open fun initView(savedInstanceState: Bundle?, viewModel: T) {}

    protected open fun bindViewModel(viewModel: T) {}
}