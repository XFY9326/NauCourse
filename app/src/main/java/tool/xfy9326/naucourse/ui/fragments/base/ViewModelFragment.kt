package tool.xfy9326.naucourse.ui.fragments.base

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import tool.xfy9326.naucourse.ui.models.base.BaseViewModel


abstract class ViewModelFragment<T : BaseViewModel> : Fragment() {
    private lateinit var contentViewModel: T

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contentViewModel = onCreateViewModel()
        contentViewModel.onFragmentAttach()
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onSetInstance(arguments)
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareCacheInit(contentViewModel, savedInstanceState != null)
        contentViewModel.onInitCache(savedInstanceState != null)

        bindViewModel(contentViewModel)
        initView(contentViewModel)

        contentViewModel.onInitView(savedInstanceState != null)
        setView(savedInstanceState)
    }

    fun getViewModel(): T = contentViewModel

    protected abstract fun onCreateViewModel(): T

    protected open fun prepareCacheInit(viewModel: T, isRestored: Boolean) {}

    protected open fun initView(viewModel: T) {}

    protected open fun setView(savedInstanceState: Bundle?) {}

    protected open fun bindViewModel(viewModel: T) {}

    protected open fun onSetInstance(args: Bundle?) {}
}