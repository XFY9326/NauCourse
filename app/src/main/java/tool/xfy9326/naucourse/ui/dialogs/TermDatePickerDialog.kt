package tool.xfy9326.naucourse.ui.dialogs

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import tool.xfy9326.naucourse.providers.beans.jwc.TermDate
import tool.xfy9326.naucourse.utils.views.DialogUtils
import java.util.*

class TermDatePickerDialog : DialogFragment(), DatePickerDialog.OnDateSetListener {
    private lateinit var dateType: DateType
    private lateinit var termDate: TermDate
    private val calendar = Calendar.getInstance(Locale.CHINA)

    enum class DateType {
        START_DATE,
        END_DATE
    }

    companion object {
        const val DATE_TYPE = "DATE_TYPE"
        const val TERM_DATE = "TERM_DATE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dateType = arguments?.getSerializable(DATE_TYPE) as DateType
        termDate = arguments?.getSerializable(TERM_DATE) as TermDate
    }

    fun setDate(date: Date) {
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
                    fragment.onTermDatePartEditCanceled(termDate)
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
            fragment.onTermDatePartSet(date, dateType, termDate)
        }
    }

    interface DatePickDialogCallback {
        fun onTermDatePartSet(date: Date, dateType: DateType, termDate: TermDate)

        fun onTermDatePartEditCanceled(termDate: TermDate)
    }
}