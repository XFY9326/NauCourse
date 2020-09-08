@file:Suppress("unused")

package tool.xfy9326.naucourse.kt

import android.content.BroadcastReceiver
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import tool.xfy9326.naucourse.utils.debug.ExceptionUtils


fun Int.isOdd(): Boolean = this % 2 != 0

fun Int.isEven(): Boolean = this % 2 == 0

fun BroadcastReceiver.goAsync(
    coroutineScope: CoroutineScope = GlobalScope,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    block: suspend () -> Unit
) {
    val result = goAsync()
    coroutineScope.launch(dispatcher) {
        try {
            block()
        } catch (e: Exception) {
            ExceptionUtils.printStackTrace(this, e)
        } finally {
            result.finish()
        }
    }
}

inline fun Mutex.tryWithLock(owner: Any? = null, action: () -> Unit) {
    if (tryLock(owner)) {
        try {
            action()
        } finally {
            unlock(owner)
        }
    }
}


fun Context.getPackageUri(): Uri = Uri.parse("package:${packageName}")