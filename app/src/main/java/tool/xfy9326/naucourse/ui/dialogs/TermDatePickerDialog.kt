package tool.xfy9326.naucourse.ui.dialogs

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import tool.xfy9326.naucourse.utils.views.DialogUtils
import java.util.*

class TermDatePickerDialog : DialogFragment(), DatePickerDialog.OnDateSetListener {
    private val calendar = Calendar.getInstance(Locale.CHINA)
    private lateinit var nowStartDate: Date
    private lateinit var nowEndDate: Date
    private lateinit var editDateType: DateType

    companion object {
        private const val DATE_TYPE = "DATE_TYPE"
        private const val TERM_DATE_START = "TERM_DATE_START"
        private const val TERM_DATE_END = "TERM_DATE_END"

        fun showTermDatePickerDialog(fragmentManager: FragmentManager, startTermDate: Date, endTermDate: Date, editDateType: DateType) {
            TermDatePickerDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(DATE_TYPE, editDateType)
                    putSerializable(TERM_DATE_START, startTermDate)
                    putSerializable(TERM_DATE_END, endTermDate)
                }
            }.show(fragmentManager, null)
        }
    }

    enum class DateType {
        START_DATE,
        END_DATE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editDateType = arguments?.getSerializable(DATE_TYPE) as DateType
        nowStartDate = arguments?.getSerializable(TERM_DATE_START) as Date
        nowEndDate = arguments?.getSerializable(TERM_DATE_END) as Date
        if (editDateType == DateType.START_DATE) {
            setDate(nowStartDate)
        } else if (editDateType == DateType.END_DATE) {
            setDate(nowEndDate)
        }
    }

    private fun setDate(date: Date) {
        calendar.time = date
        (dialog as DatePickerDialog?)?.updateDate(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return DatePickerDialog(
            requireContext(),
            this,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.no)) { _, _ ->
                val fragment = requireActivity()
                if (fragment is DatePickDialogCallback) {
                    fragment.onTermDatePartEditCanceled(nowStartDate, nowEndDate)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.let {
            DialogUtils.applyButtonTextAndBackground(requireContext(), it)
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val date = calendar.apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }.time
        val fragment = requireActivity()
        if (fragment is DatePickDialogCallback) {
            if (editDateType == DateType.START_DATE) {
                fragment.onTermDatePartSet(date, nowEndDate)
            } else if (editDateType == DateType.END_DATE) {
                fragment.onTermDatePartSet(nowStartDate, date)
            }
        }
    }

    interface DatePickDialogCallback {
        fun onTermDatePartSet(startTermDate: Date, endTermDate: Date)

        fun onTermDatePartEditCanceled(startTermDate: Date, endTermDate: Date)
    }
}