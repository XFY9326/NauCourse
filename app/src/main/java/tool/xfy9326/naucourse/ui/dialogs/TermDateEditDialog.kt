package tool.xfy9326.naucourse.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_term_date_edit.view.*
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.providers.beans.jwc.TermDate
import tool.xfy9326.naucourse.utils.views.DialogUtils
import java.text.SimpleDateFormat
import java.util.*

// 必须基于CourseManageActivity使用，因为实现了TermDatePickerDialog.DatePickDialogCallback接口
class TermDateEditDialog : DialogFragment() {
    companion object {
        const val TERM_DATE = "TERM_DATE"

        private const val CONTENT_WIDTH_PERCENT = 0.85

        private val DATE_FORMAT_YMD = SimpleDateFormat(Constants.Time.FORMAT_YMD, Locale.CHINA)
    }

    private lateinit var termDate: TermDate
    private lateinit var nowStartDate: Date
    private lateinit var nowEndDate: Date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        termDate = arguments?.getSerializable(TERM_DATE) as TermDate
        nowStartDate = termDate.startDate
        nowEndDate = termDate.endDate
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
        }
        return inflater.inflate(R.layout.dialog_term_date_edit, container, false).apply {
            tv_courseTermCurrent.text = getString(R.string.current_term, termDate.getTerm())
            tv_courseTermStart.text = getString(R.string.current_term_date_start, DATE_FORMAT_YMD.format(nowStartDate))
            tv_courseTermEnd.text = getString(R.string.current_term_date_end, DATE_FORMAT_YMD.format(nowEndDate))

            btn_courseTermEditStart.setOnClickListener {
                showDateEditDialog(nowStartDate, TermDatePickerDialog.DateType.START_DATE)
            }
            btn_courseTermEditEnd.setOnClickListener {
                showDateEditDialog(nowEndDate, TermDatePickerDialog.DateType.END_DATE)
            }

            btn_courseTermEditClear.setOnClickListener {
                val activity = requireActivity()
                if (activity is OnTermEditListener) {
                    activity.onTermCustomClear()
                }
                dismiss()
            }
            btn_courseTermEditCancel.setOnClickListener {
                dismiss()
            }
            btn_courseTermEditConfirm.setOnClickListener {
                val activity = requireActivity()
                if (activity is OnTermEditListener) {
                    activity.onTermDateChanged(TermDate(nowStartDate, nowEndDate))
                }
                dismiss()
            }
        }
    }

    private fun showDateEditDialog(date: Date, dateType: TermDatePickerDialog.DateType) {
        // 如果不取消显示，直接通过这里启动会导致白色闪屏
        dismiss()
        TermDatePickerDialog().apply {
            arguments = Bundle().apply {
                putSerializable(TermDatePickerDialog.TERM_DATE, termDate)
                putSerializable(TermDatePickerDialog.DATE_TYPE, dateType)
            }
            setDate(date)
        }.show(parentFragmentManager, null)
    }

    override fun onStart() {
        super.onStart()
        DialogUtils.applyBackgroundAndWidth(requireContext(), dialog, CONTENT_WIDTH_PERCENT)
    }

    interface OnTermEditListener {
        fun onTermDateChanged(termDate: TermDate)

        fun onTermCustomClear()
    }
}