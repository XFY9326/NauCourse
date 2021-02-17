package tool.xfy9326.naucourse.utils.views

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.constants.OthersConst
import tool.xfy9326.naucourse.databinding.DialogBottomMsgBinding
import tool.xfy9326.naucourse.databinding.DialogCourseControlPanelBinding
import tool.xfy9326.naucourse.databinding.DialogImageOperationBinding
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.kt.bindLifecycle
import tool.xfy9326.naucourse.kt.createWithLifecycle
import tool.xfy9326.naucourse.ui.views.widgets.AnimateSlider
import tool.xfy9326.naucourse.utils.io.TextIOUtils
import tool.xfy9326.naucourse.utils.utility.IntentUtils
import kotlin.math.min

object DialogUtils {
    fun createCourseInitConflictDialog(context: Context, lifecycle: Lifecycle) =
        MaterialAlertDialogBuilder(context).apply {
            setTitle(R.string.online_course_conflict_attention_title)
            setMessage(R.string.online_course_init_conflict_attention_msg)
            setPositiveButton(android.R.string.ok, null)
            setCancelable(false)
            background = ContextCompat.getDrawable(context, R.drawable.bg_dialog)
        }.createWithLifecycle(lifecycle)

    fun createArchiveAttentionDialog(context: Context, lifecycle: Lifecycle) =
        MaterialAlertDialogBuilder(context).apply {
            setTitle(R.string.archive_attention)
            setMessage(R.string.archive_attention_msg)
            setPositiveButton(android.R.string.ok) { _, _ ->
                AppPref.ShowArchiveAttention = false
            }
            setNeutralButton(R.string.archive_attention_passage, null)
            setCancelable(false)
            background = ContextCompat.getDrawable(context, R.drawable.bg_dialog)
        }.createWithLifecycle(lifecycle).also { dialog ->
            dialog.setOnShowListener {
                dialog.getButton(Dialog.BUTTON_NEUTRAL).setOnClickListener {
                    IntentUtils.launchUrlInBrowser(context, "https://www.xfy9326.top/2021/02/17/naucourse_puresshedule/")
                }
            }
        }

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
            setPositiveButton(android.R.string.ok, null)
            background = ContextCompat.getDrawable(context, R.drawable.bg_dialog)
        }.createWithLifecycle(lifecycle)

    fun createEditAsyncCourseAttention(context: Context, lifecycle: Lifecycle) =
        MaterialAlertDialogBuilder(context).apply {
            setTitle(R.string.attention)
            setMessage(R.string.edit_async_course_attention)
            setCancelable(false)
            setPositiveButton(R.string.i_see) { _, _ ->
                AppPref.EditAsyncCourseAttention = true
            }
            background = ContextCompat.getDrawable(context, R.drawable.bg_dialog)
        }.createWithLifecycle(lifecycle)

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
            background = ContextCompat.getDrawable(context, R.drawable.bg_dialog)
        }.createWithLifecycle(lifecycle)

    fun createUsingLicenseDialog(context: Context, lifecycle: Lifecycle) =
        MaterialAlertDialogBuilder(context).apply {
            setTitle(R.string.eula_license)
            setMessage(
                TextIOUtils.readAssetFileAsText(
                    context,
                    TextIOUtils.ASSETS_PATH_EULA_LICENSE
                )
            )
            setPositiveButton(android.R.string.ok, null)
            background = ContextCompat.getDrawable(context, R.drawable.bg_dialog)
        }.createWithLifecycle(lifecycle)

    fun createOpenSourceLicenseDialog(context: Context, lifecycle: Lifecycle) =
        MaterialAlertDialogBuilder(context).apply {
            setTitle(R.string.open_source_license)
            setMessage(
                TextIOUtils.readAssetFileAsText(
                    context,
                    TextIOUtils.ASSETS_PATH_OPEN_SOURCE_LICENSE
                )
            )
            setPositiveButton(android.R.string.ok, null)
            background = ContextCompat.getDrawable(context, R.drawable.bg_dialog)
        }.createWithLifecycle(lifecycle)

    fun createForgetPasswordDialog(context: Context, lifecycle: Lifecycle) =
        MaterialAlertDialogBuilder(context).apply {
            setTitle(R.string.forget_password)
            setMessage(R.string.forget_password_help)
            setNeutralButton(R.string.find_password) { _: DialogInterface, _: Int ->
                IntentUtils.launchUrlInBrowser(context, OthersConst.FORGET_PASSWORD)
            }
            setPositiveButton(android.R.string.ok, null)
            background = ContextCompat.getDrawable(context, R.drawable.bg_dialog)
        }.createWithLifecycle(lifecycle)

    fun createLogoutAttentionDialog(context: Context, lifecycle: Lifecycle, logoutListener: DialogInterface.OnClickListener) =
        MaterialAlertDialogBuilder(context).apply {
            setTitle(R.string.logout)
            setMessage(R.string.logout_msg)
            setNegativeButton(android.R.string.cancel, null)
            setPositiveButton(android.R.string.ok, logoutListener)
            background = ContextCompat.getDrawable(context, R.drawable.bg_dialog)
        }.createWithLifecycle(lifecycle)

    fun createImageOperationDialog(context: Context, lifecycle: Lifecycle, shareListener: (() -> Unit), saveListener: (() -> Unit)) =
        BottomSheetDialog(context).apply {
            setContentView(DialogImageOperationBinding.inflate(layoutInflater).apply {
                tvDialogShareImage.setOnClickListener {
                    shareListener.invoke()
                    dismiss()
                }
                tvDialogSaveImage.setOnClickListener {
                    saveListener.invoke()
                    dismiss()
                }
            }.root)

            val parentView = findViewById<ViewGroup>(com.google.android.material.R.id.design_bottom_sheet)
            parentView?.background = ContextCompat.getDrawable(context, R.drawable.bg_dialog)

            behavior.state = BottomSheetBehavior.STATE_EXPANDED

            bindLifecycle(lifecycle)
        }

    fun createCourseTableControlDialog(
        context: Context, lifecycle: Lifecycle, nowWeekNum: Int, nowShowWeekNum: Int,
        maxWeekNum: Int, weekNumChangeListener: ((Int) -> Unit)
    ) =
        BottomSheetDialog(context).apply {
            setContentView(DialogCourseControlPanelBinding.inflate(layoutInflater).apply {
                if (nowWeekNum != 0) tvCourseControlCurrentWeekNum.text = context.getString(R.string.current_week_num, nowWeekNum)

                sliderCourseControlWeekNum.apply {
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
            }.root)
            val parentView = findViewById<ViewGroup>(com.google.android.material.R.id.design_bottom_sheet)
            parentView?.background = ContextCompat.getDrawable(context, android.R.color.transparent)

            window?.apply {
                addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                attributes.dimAmount = 0.2f
            }

            behavior.state = BottomSheetBehavior.STATE_EXPANDED

            bindLifecycle(lifecycle)
        }

    fun createBottomMsgDialog(context: Context, lifecycle: Lifecycle, title: String, msg: String, listener: View.OnClickListener? = null) =
        BottomSheetDialog(context).apply {
            setContentView(DialogBottomMsgBinding.inflate(layoutInflater).apply {
                tvDialogBottomTitle.text = title
                tvDialogBottomContent.text = msg
                if (listener != null) {
                    btnDialogBottomConfirm.setOnClickListener(listener)
                } else {
                    btnDialogBottomConfirm.setOnClickListener {
                        dismiss()
                    }
                }
                val maxWidth = context.resources.getDimensionPixelSize(R.dimen.bottom_msg_dialog_max_width)
                val windowsWidth = context.resources.displayMetrics.widthPixels
                cvDialogBottomMsg.layoutParams.apply {
                    width = min(maxWidth, windowsWidth)
                }
            }.root)

            val parentView = findViewById<ViewGroup>(com.google.android.material.R.id.design_bottom_sheet)
            parentView?.background = ContextCompat.getDrawable(context, android.R.color.transparent)

            behavior.state = BottomSheetBehavior.STATE_EXPANDED

            bindLifecycle(lifecycle)
        }

    fun applyBackgroundAndWidth(context: Context?, dialog: Dialog?, widthPercent: Double) {
        dialog?.apply {
            val displayMetrics = context?.resources?.displayMetrics!!
            window?.apply {
                setLayout((displayMetrics.widthPixels * widthPercent).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
                setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_dialog))
            }
        }
    }

    fun applyButtonTextAndBackground(context: Context, dialog: Dialog?) {
        dialog?.apply {
            findViewById<Button>(android.R.id.button1)?.setTextColor(ContextCompat.getColor(context, R.color.colorDialogButtonText))
            findViewById<Button>(android.R.id.button2)?.setTextColor(ContextCompat.getColor(context, R.color.colorDialogButtonText))
            findViewById<Button>(android.R.id.button3)?.setTextColor(ContextCompat.getColor(context, R.color.colorDialogButtonText))
            window?.apply {
                setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.bg_dialog))
            }
        }
    }
}