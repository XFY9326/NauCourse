package tool.xfy9326.naucourse.kt

import android.app.Dialog
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner


// Activity销毁时自动关闭Dialog，防止窗体泄漏
fun Dialog.bindLifecycle(lifecycle: Lifecycle) {
    val observer = object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            if (isShowing) dismiss()
            owner.lifecycle.removeObserver(this)
        }
    }
    lifecycle.addObserver(observer)
    setOnDismissListener {
        lifecycle.removeObserver(observer)
    }
}

fun AlertDialog.Builder.createWithLifecycle(lifecycle: Lifecycle) = create().also {
    it.bindLifecycle(lifecycle)
}