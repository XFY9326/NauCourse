package tool.xfy9326.naucourse.ui.fragments.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import tool.xfy9326.naucourse.ui.models.base.BaseViewModel


abstract class ViewModelFragment<T : BaseViewModel> : Fragment() {
    private lateinit var contentViewModel: T

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onSetInstance(arguments)
    }

    @CallSuper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return if (view == null) {
            inflater.inflate(onCreateContentView(), container, false)
        } else {
            val parent = requireView().parent as ViewGroup?
            parent?.removeView(view)
            view
        }
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentViewModel = onCreateViewModel()
        prepareCacheInit(contentViewModel, savedInstanceState != null)
        contentViewModel.onInitCache(savedInstanceState != null)

        bindViewModel(contentViewModel)
        initView(contentViewModel)

        contentViewModel.onInitView(savedInstanceState != null)
        setView(savedInstanceState)
    }

    fun getViewModel(): T = contentViewModel

    @LayoutRes
    protected abstract fun onCreateContentView(): Int

    protected abstract fun onCreateViewModel(): T

    protected open fun prepareCacheInit(viewModel: T, isRestored: Boolean) {}

    protected open fun initView(viewModel: T) {}

    protected open fun setView(savedInstanceState: Bundle?) {}

    protected open fun bindViewModel(viewModel: T) {}

    protected open fun onSetInstance(args: Bundle?) {}
}