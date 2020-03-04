package tool.xfy9326.naucourses.ui.dialogs

import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_full_screen_loading.view.*
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.utils.views.AnimUtils

class FullScreenLoadingDialog : DialogFragment() {

    companion object {
        const val LOADING_DIALOG_TAG = "LOADING_DIALOG"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
        }
        return inflater.inflate(R.layout.dialog_full_screen_loading, container, false).apply {

        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            isCancelable = false
            setBackgroundDrawable(resources.getDrawable(android.R.color.transparent, null))
        }
        (view?.iv_dialogFullScreenLoading?.drawable as AnimatedVectorDrawable?)?.apply {
            registerAnimationCallback(AnimUtils.getAnimationLoopCallback())
            start()
        }
    }

    override fun onStop() {
        (view?.iv_dialogFullScreenLoading?.drawable as AnimatedVectorDrawable?)?.apply {
            clearAnimationCallbacks()
            stop()
        }
        super.onStop()
    }

}