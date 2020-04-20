package tool.xfy9326.naucourse.ui.dialogs

import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.transition.Fade
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import kotlinx.android.synthetic.main.dialog_full_screen_loading.view.*
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.utils.views.AnimUtils

class FullScreenLoadingDialog : DialogFragment() {

    companion object {
        private const val LOADING_DIALOG_TAG = "LOADING_DIALOG"

        fun showDialog(fragmentManager: FragmentManager) {
            if (fragmentManager.findFragmentByTag(LOADING_DIALOG_TAG) == null) {
                FullScreenLoadingDialog().show(fragmentManager, LOADING_DIALOG_TAG)
            }
        }

        fun close(fragmentManager: FragmentManager) =
            (fragmentManager.findFragmentByTag(LOADING_DIALOG_TAG) as DialogFragment?)?.dismissAllowingStateLoss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = Fade()
        exitTransition = Fade()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
        }
        return inflater.inflate(R.layout.dialog_full_screen_loading, container, false)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            isCancelable = false
            setBackgroundDrawable(resources.getDrawable(android.R.color.transparent, null))
        }
        (view?.iv_dialogFullScreenLoading?.drawable as AnimatedVectorDrawable?)?.apply {
            AnimatedVectorDrawableCompat.registerAnimationCallback(this, AnimUtils.getAnimationLoopCallback())
            start()
        }
    }

    override fun onStop() {
        (view?.iv_dialogFullScreenLoading?.drawable as AnimatedVectorDrawable?)?.apply {
            AnimatedVectorDrawableCompat.clearAnimationCallbacks(this)
            stop()
        }
        super.onStop()
    }

    fun show(fragmentManager: FragmentManager) = show(fragmentManager, LOADING_DIALOG_TAG)
}