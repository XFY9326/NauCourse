package tool.xfy9326.naucourses.ui.models.base

import androidx.lifecycle.ViewModel

abstract class BaseViewModel : ViewModel() {
    open fun onActivityCreate() {}

    open fun onInitView() {}
}