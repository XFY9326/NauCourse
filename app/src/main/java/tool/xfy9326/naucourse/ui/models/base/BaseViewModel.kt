package tool.xfy9326.naucourse.ui.models.base

import androidx.lifecycle.ViewModel

abstract class BaseViewModel : ViewModel() {
    private var hasInit = false

    open fun onFragmentAttach() {}

    open fun onInitCache(isRestored: Boolean) {}

    open fun onInitView(isRestored: Boolean) {}

    @Synchronized
    fun tryInit() =
        if (hasInit) {
            false
        } else {
            hasInit = true
            true
        }

    @Synchronized
    fun hasInit() = hasInit
}