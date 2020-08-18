package tool.xfy9326.naucourse.ui.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.layout_list.view.*
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.beans.CreditCountItem
import tool.xfy9326.naucourse.ui.views.recyclerview.adapters.CreditCountAdapter
import tool.xfy9326.naucourse.utils.BaseUtils


class CreditCountCourseSelectDialog : DialogFragment() {
    companion object {
        const val CREDIT_COUNT_SELECT_ITEM = "CREDIT_COUNT_SELECT_ITEM"
        const val CREDIT_COUNT_HISTORY_ITEM = "CREDIT_COUNT_HISTORY_ITEM"
    }

    private lateinit var currentItems: ArrayList<CreditCountItem>
    private lateinit var historyItems: ArrayList<CreditCountItem>
    private lateinit var adapter: CreditCountAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readData(savedInstanceState)
    }

    @Suppress("UNCHECKED_CAST")
    private fun readData(savedInstanceState: Bundle?) {
        historyItems = arguments?.getSerializable(CREDIT_COUNT_HISTORY_ITEM) as ArrayList<CreditCountItem>
        currentItems = (savedInstanceState?.getSerializable(CREDIT_COUNT_SELECT_ITEM) ?: arguments?.getSerializable(CREDIT_COUNT_SELECT_ITEM))
                as ArrayList<CreditCountItem>
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(CREDIT_COUNT_SELECT_ITEM, ArrayList(adapter.currentList))
        super.onSaveInstanceState(outState)
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.select_credit_count_course)

            adapter = CreditCountAdapter(requireContext())
            adapter.submitList(currentItems)

            val view = layoutInflater.inflate(R.layout.layout_list, null).apply {
                layout_list.setPadding(0, resources.getDimensionPixelSize(R.dimen.credit_count_course_select_dialog_padding), 0, 0)
                arv_dataList.adapter = adapter
                setView(this)
            }

            setNegativeButton(android.R.string.cancel, null)
            setPositiveButton(R.string.count) { _, _ ->
                BaseUtils.hideKeyboard(requireContext(), view.windowToken)
                requireActivity().let {
                    if (it is OnCreditCountCourseSelectedListener) it.onCreditCountCourseSelected(adapter.getSelectedItems(), historyItems)
                }
            }
        }.create()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
            clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        }
    }

    interface OnCreditCountCourseSelectedListener {
        fun onCreditCountCourseSelected(items: ArrayList<CreditCountItem>, history: ArrayList<CreditCountItem>)
    }
}