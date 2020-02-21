package tool.xfy9326.naucourses.ui.activities.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import tool.xfy9326.naucourses.ui.models.base.BaseViewModel

abstract class ViewModelActivity<T : BaseViewModel> : AppCompatActivity() {
    private lateinit var contentViewModel: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(onCreateContentView())
        initActivity(savedInstanceState)
    }

    private fun initActivity(savedInstanceState: Bundle?) {
        contentViewModel = onCreateViewModel()
        bindViewModel(contentViewModel)
        initView(savedInstanceState, contentViewModel)
        contentViewModel.onActivityCreate()
        if (savedInstanceState == null) {
            contentViewModel.onInitView()
        }
    }

    @LayoutRes
    protected abstract fun onCreateContentView(): Int

    protected abstract fun onCreateViewModel(): T

    protected abstract fun initView(savedInstanceState: Bundle?, viewModel: T)

    protected open fun bindViewModel(viewModel: T) {}
}