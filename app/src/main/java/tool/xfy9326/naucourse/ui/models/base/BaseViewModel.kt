package tool.xfy9326.naucourse.ui.models.base

import androidx.lifecycle.ViewModel

abstract class BaseViewModel : ViewModel() {
    open fun onInitCache(isRestored: Boolean) {}

    open fun onInitView(isRestored: Boolean) {}
}