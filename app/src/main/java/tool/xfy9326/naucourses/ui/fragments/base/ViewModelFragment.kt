package tool.xfy9326.naucourses.ui.fragments.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import tool.xfy9326.naucourses.ui.models.base.BaseViewModel

abstract class ViewModelFragment<T : BaseViewModel> : Fragment() {
    private lateinit var contentViewModel: T

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contentViewModel = onCreateViewModel()
    }

    @CallSuper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(onCreateContentView(), container, false)

    @CallSuper
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        contentViewModel.onActivityCreate()

        bindViewModel(contentViewModel)
        initView(contentViewModel)
        contentViewModel.onInitView()
    }

    fun getViewModel(): T = contentViewModel

    @LayoutRes
    protected abstract fun onCreateContentView(): Int

    protected abstract fun onCreateViewModel(): T

    protected open fun initView(viewModel: T) {}

    protected open fun bindViewModel(viewModel: T) {}
}