package tool.xfy9326.naucourse.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.dialog_term_date_edit.view.*
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.constants.CourseConst
import tool.xfy9326.naucourse.constants.TimeConst
import tool.xfy9326.naucourse.kt.showShortToast
import tool.xfy9326.naucourse.providers.beans.jwc.TermDate
import tool.xfy9326.naucourse.utils.courses.TimeUtils
import tool.xfy9326.naucourse.utils.views.DialogUtils
import java.text.SimpleDateFormat
import java.util.*

// 必须基于CourseManageActivity使用，因为实现了TermDatePickerDialog.DatePickDialogCallback接口
class TermDateEditDialog : DialogFragment() {
    companion object {
        private const val TERM_DATE_START = "TERM_DATE_START"
        private const val TERM_DATE_END = "TERM_DATE_END"

        private const val CONTENT_WIDTH_PERCENT = 0.85

        private val DATE_FORMAT_YMD = SimpleDateFormat(TimeConst.FORMAT_YMD, Locale.CHINA)

        fun startTermDateEditDialog(fragmentManager: FragmentManager, termDate: TermDate) {
            startTermDateEditDialog(fragmentManager, termDate.startDate, termDate.endDate)
        }

        fun startTermDateEditDialog(fragmentManager: FragmentManager, startTermDate: Date, endTermDate: Date) {
            TermDateEditDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(TERM_DATE_START, startTermDate)
                    putSerializable(TERM_DATE_END, endTermDate)
                }
            }.show(fragmentManager, null)
        }
    }

    private lateinit var nowStartDate: Date
    private lateinit var nowEndDate: Date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nowStartDate = arguments?.getSerializable(TERM_DATE_START) as Date
        nowEndDate = arguments?.getSerializable(TERM_DATE_END) as Date
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
        }
        return inflater.inflate(R.layout.dialog_term_date_edit, container, false).apply {
            tv_courseTermCurrent.text = getString(R.string.current_term, TermDate.getTerm(nowStartDate))
            tv_courseTermStart.text = getString(R.string.current_term_date_start, DATE_FORMAT_YMD.format(nowStartDate))
            tv_courseTermEnd.text = getString(R.string.current_term_date_end, DATE_FORMAT_YMD.format(nowEndDate))

            btn_courseTermEditStart.setOnClickListener {
                showDateEditDialog(TermDatePickerDialog.DateType.START_DATE)
            }
            btn_courseTermEditEnd.setOnClickListener {
                showDateEditDialog(TermDatePickerDialog.DateType.END_DATE)
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
                if (nowStartDate >= nowEndDate) {
                    showShortToast(R.string.term_date_error)
                } else {
                    val weekLength = TimeUtils.getWeekLength(nowStartDate, nowEndDate)
                    if (weekLength < CourseConst.MIN_WEEK_NUM_SIZE || weekLength > CourseConst.MAX_WEEK_NUM_SIZE) {
                        showShortToast(R.string.term_date_length_error, CourseConst.MIN_WEEK_NUM_SIZE, CourseConst.MAX_WEEK_NUM_SIZE, weekLength)
                    } else {
                        val activity = requireActivity()
                        if (activity is OnTermEditListener) {
                            activity.onTermDateChanged(TermDate(nowStartDate, nowEndDate))
                        }
                        dismiss()
                    }
                }
            }
        }
    }

    private fun showDateEditDialog(dateType: TermDatePickerDialog.DateType) {
        // 如果不取消显示，直接通过这里启动会导致白色闪屏
        dismiss()
        TermDatePickerDialog.showTermDatePickerDialog(parentFragmentManager, nowStartDate, nowEndDate, dateType)
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