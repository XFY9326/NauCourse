package tool.xfy9326.naucourse.ui.models.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {
    private var hasInit = false

    open fun onFragmentAttach() {}

    open fun onInitCache(isRestored: Boolean) {}

    open fun onInitView(isRestored: Boolean) {}

    @Synchronized
    fun tryInit(dispatcher: CoroutineDispatcher, block: suspend () -> Unit): Boolean {
        if (!hasInit) {
            hasInit = true
            viewModelScope.launch(dispatcher) {
                block.invoke()
            }
            return true
        }
        return false
    }

    @Synchronized
    fun tryInit(block: () -> Unit): Boolean {
        if (!hasInit) {
            hasInit = true
            block.invoke()
            return true
        }
        return false
    }

    @Synchronized
    fun hasInit() = hasInit
}