package tool.xfy9326.naucourse.utils.views

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import kotlinx.android.synthetic.main.dialog_bottom_msg.*
import kotlinx.android.synthetic.main.dialog_course_control_panel.*
import kotlinx.android.synthetic.main.dialog_image_operation.*
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.ui.views.widgets.AnimateSlider
import tool.xfy9326.naucourse.utils.io.TextIOUtils
import tool.xfy9326.naucourse.utils.utility.IntentUtils
import kotlin.math.min

object DialogUtils {
    fun createCreditShowDialog(context: Context, lifecycle: Lifecycle, credit: Pair<Float, Float?>) =
        MaterialAlertDialogBuilder(context).apply {
            setTitle(R.string.credit_count)
            setMessage(
                if (credit.second == null) {
                    context.getString(R.string.credit_count_result, credit.first)
                } else {
                    context.getString(R.string.jwc_credit, credit.second) + context.getString(R.string.credit_count_result, credit.first)
                }
            )
            setPositiveButton(android.R.string.yes, null)
            background = context.getDrawable(R.drawable.bg_dialog)
        }.create().also {
            addAutoCloseListener(lifecycle, it)
        }

    fun createCourseColorPickerDialog(context: Context, color: Int, dialogId: Int): ColorPickerDialog =
        ColorPickerDialog.newBuilder().apply {
            setColor(color)
            setDialogTitle(R.string.course_color_edit)
            setDialogId(dialogId)
            setShowAlphaSlider(false)
            setPresets(context.resources.getIntArray(R.array.material_colors))
        }.create()

    fun createCourseAddDialog(context: Context, lifecycle: Lifecycle, listener: DialogInterface.OnClickListener) =
        MaterialAlertDialogBuilder(context).apply {
            setItems(context.resources.getStringArray(R.array.course_manage_add_list), listener)
            background = context.getDrawable(R.drawable.bg_dialog)
        }.create().also {
            addAutoCloseListener(lifecycle, it)
        }

    fun createUsingLicenseDialog(context: Context, lifecycle: Lifecycle) =
        MaterialAlertDialogBuilder(context).apply {
            setTitle(R.string.eula_license)
            setMessage(
                TextIOUtils.readAssetFileAsText(
                    context,
                    TextIOUtils.ASSETS_PATH_EULA_LICENSE
                )
            )
            setPositiveButton(android.R.string.yes, null)
            background = context.getDrawable(R.drawable.bg_dialog)
        }.create().also {
            addAutoCloseListener(lifecycle, it)
        }

    fun createOpenSourceLicenseDialog(context: Context, lifecycle: Lifecycle) =
        MaterialAlertDialogBuilder(context).apply {
            setTitle(R.string.open_source_license)
            setMessage(
                TextIOUtils.readAssetFileAsText(
                    context,
                    TextIOUtils.ASSETS_PATH_OPEN_SOURCE_LICENSE
                )
            )
            setPositiveButton(android.R.string.yes, null)
            background = context.getDrawable(R.drawable.bg_dialog)
        }.create().also {
            addAutoCloseListener(lifecycle, it)
        }

    fun createForgetPasswordDialog(context: Context, lifecycle: Lifecycle) =
        MaterialAlertDialogBuilder(context).apply {
            setTitle(R.string.forget_password)
            setMessage(R.string.forget_password_help)
            setNeutralButton(R.string.find_password) { _: DialogInterface, _: Int ->
                IntentUtils.launchUrlInBrowser(context, Constants.Others.FORGET_PASSWORD)
            }
            setPositiveButton(android.R.string.yes, null)
            background = context.getDrawable(R.drawable.bg_dialog)
        }.create().also {
            addAutoCloseListener(lifecycle, it)
        }

    fun createLogoutAttentionDialog(context: Context, lifecycle: Lifecycle, logoutListener: DialogInterface.OnClickListener) =
        MaterialAlertDialogBuilder(context).apply {
            setTitle(R.string.logout)
            setMessage(R.string.logout_msg)
            setNegativeButton(android.R.string.cancel, null)
            setPositiveButton(android.R.string.yes, logoutListener)
            background = context.getDrawable(R.drawable.bg_dialog)
        }.create().also {
            addAutoCloseListener(lifecycle, it)
        }

    fun createImageOperationDialog(context: Context, lifecycle: Lifecycle, shareListener: (() -> Unit), saveListener: (() -> Unit)) =
        BottomSheetDialog(context).apply {
            setContentView(R.layout.dialog_image_operation)
            val parentView = findViewById<ViewGroup>(com.google.android.material.R.id.design_bottom_sheet)
            parentView?.background = context.getDrawable(R.drawable.bg_dialog)

            tv_dialogShareImage.setOnClickListener {
                shareListener.invoke()
                dismiss()
            }
            tv_dialogSaveImage.setOnClickListener {
                saveListener.invoke()
                dismiss()
            }

            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }.also {
            addAutoCloseListener(lifecycle, it)
        }

    fun createCourseTableControlDialog(
        context: Context, lifecycle: Lifecycle, nowWeekNum: Int, nowShowWeekNum: Int,
        maxWeekNum: Int, weekNumChangeListener: ((Int) -> Unit)
    ) =
        BottomSheetDialog(context).apply {
            setContentView(R.layout.dialog_course_control_panel)
            val parentView = findViewById<ViewGroup>(com.google.android.material.R.id.design_bottom_sheet)
            parentView?.background = context.getDrawable(android.R.color.transparent)

            if (nowWeekNum != 0) tv_courseControlCurrentWeekNum.text = context.getString(R.string.current_week_num, nowWeekNum)

            slider_courseControlWeekNum.apply {
                valueTo = maxWeekNum.toFloat()
                value = nowShowWeekNum.toFloat()
                setLabelFormatter {
                    context.getString(R.string.week_num, it.toInt())
                }
                setOnSlideFinishListener(object : AnimateSlider.OnSlideFinishListener {
                    override fun onValueChanged(slider: Slider, value: Float) {
                        weekNumChangeListener.invoke(slider.value.toInt())
                    }
                })
            }

            window?.apply {
                addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                attributes.dimAmount = 0.2f
            }

            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }.also {
            addAutoCloseListener(lifecycle, it)
        }

    fun createBottomMsgDialog(context: Context, lifecycle: Lifecycle, title: String, msg: String, listener: View.OnClickListener? = null) =
        BottomSheetDialog(context).apply {
            setContentView(R.layout.dialog_bottom_msg)
            val parentView = findViewById<ViewGroup>(com.google.android.material.R.id.design_bottom_sheet)
            parentView?.background = context.getDrawable(android.R.color.transparent)

            tv_dialogBottomTitle.text = title
            tv_dialogBottomContent.text = msg
            if (listener != null) {
                btn_dialogBottomConfirm.setOnClickListener(listener)
            } else {
                btn_dialogBottomConfirm.setOnClickListener {
                    dismiss()
                }
            }
            val maxWidth = context.resources.getDimensionPixelSize(R.dimen.bottom_msg_dialog_max_width)
            val windowsWidth = context.resources.displayMetrics.widthPixels
            cv_dialogBottomMsg.layoutParams.apply {
                width = min(maxWidth, windowsWidth)
            }

            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }.also {
            addAutoCloseListener(lifecycle, it)
        }

    fun applyBackgroundAndWidth(context: Context?, dialog: Dialog?, widthPercent: Double) {
        dialog?.apply {
            val displayMetrics = context?.resources?.displayMetrics!!
            window?.apply {
                setLayout((displayMetrics.widthPixels * widthPercent).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
                setBackgroundDrawable(context.getDrawable(R.drawable.bg_dialog))
            }
        }
    }

    fun applyButtonTextAndBackground(context: Context, dialog: Dialog) {
        dialog.apply {
            findViewById<Button>(android.R.id.button1)?.setTextColor(ContextCompat.getColor(context, R.color.colorDialogButtonText))
            findViewById<Button>(android.R.id.button2)?.setTextColor(ContextCompat.getColor(context, R.color.colorDialogButtonText))
            findViewById<Button>(android.R.id.button3)?.setTextColor(ContextCompat.getColor(context, R.color.colorDialogButtonText))
            window?.apply {
                setBackgroundDrawable(context.getDrawable(R.drawable.bg_dialog))
            }
        }
    }

    // Activity销毁时自动关闭Dialog，防止窗体泄漏
    fun addAutoCloseListener(lifecycle: Lifecycle, dialog: Dialog) {
        val observer = object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                if (dialog.isShowing) dialog.dismiss()
                lifecycle.removeObserver(this)
            }
        }
        lifecycle.addObserver(observer)
        dialog.setOnDismissListener {
            lifecycle.removeObserver(observer)
        }
    }
}