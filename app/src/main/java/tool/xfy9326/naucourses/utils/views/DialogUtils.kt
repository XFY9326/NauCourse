package tool.xfy9326.naucourses.utils.views

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
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
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.ui.views.widgets.StyledColorPickerDialog
import tool.xfy9326.naucourses.utils.utility.IOUtils
import tool.xfy9326.naucourses.utils.utility.IntentUtils
import kotlin.math.min

object DialogUtils {
    fun createCourseColorPickerDialog(context: Context, color: Int, dialogId: Int): StyledColorPickerDialog {
        ColorPickerDialog.newBuilder().apply {
            setColor(color)
            setDialogTitle(R.string.course_color_edit)
            setDialogId(dialogId)
            setPresets(context.resources.getIntArray(R.array.material_colors_600))
            setShowAlphaSlider(false)
        }.create().apply {
            // 使用构造器后，将构造的参数传给继承的Dialog
            val styledDialog = StyledColorPickerDialog()
            styledDialog.arguments = arguments
            return styledDialog
        }
    }

    fun createCourseAddDialog(context: Context, lifecycle: Lifecycle, listener: DialogInterface.OnClickListener): AlertDialog =
        MaterialAlertDialogBuilder(context).apply {
            setItems(context.resources.getStringArray(R.array.course_manage_add_list), listener)
            background = context.getDrawable(R.drawable.bg_dialog)
        }.create().also {
            addAutoCloseListener(lifecycle, it)
        }

    fun createUsingLicenseDialog(context: Context, lifecycle: Lifecycle): AlertDialog =
        MaterialAlertDialogBuilder(context).apply {
            setTitle(R.string.eula_license)
            setMessage(
                IOUtils.readAssetFileAsText(
                    context,
                    IOUtils.ASSETS_PATH_EULA_LICENSE
                )
            )
            setPositiveButton(android.R.string.yes, null)
            background = context.getDrawable(R.drawable.bg_dialog)
        }.create().also {
            addAutoCloseListener(lifecycle, it)
        }

    fun createOpenSourceLicenseDialog(context: Context, lifecycle: Lifecycle): AlertDialog =
        MaterialAlertDialogBuilder(context).apply {
            setTitle(R.string.open_source_license)
            setMessage(
                IOUtils.readAssetFileAsText(
                    context,
                    IOUtils.ASSETS_PATH_OPEN_SOURCE_LICENSE
                )
            )
            setPositiveButton(android.R.string.yes, null)
            background = context.getDrawable(R.drawable.bg_dialog)
        }.create().also {
            addAutoCloseListener(lifecycle, it)
        }

    fun createForgetPasswordDialog(context: Context, lifecycle: Lifecycle): AlertDialog =
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

    fun createLogoutAttentionDialog(context: Context, lifecycle: Lifecycle, logoutListener: DialogInterface.OnClickListener): AlertDialog =
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
                addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                    private var startValue = -1f
                    override fun onStartTrackingTouch(slider: Slider) {
                        startValue = slider.value
                    }

                    override fun onStopTrackingTouch(slider: Slider) {
                        if (startValue != slider.value) {
                            startValue = slider.value
                            weekNumChangeListener.invoke(slider.value.toInt())
                        }
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

    // Activity销毁时自动关闭Dialog，防止窗体泄漏
    private fun addAutoCloseListener(lifecycle: Lifecycle, dialog: Dialog) {
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