package tool.xfy9326.naucourse.ui.views.recyclerview.adapters

import android.content.Context
import android.view.ViewGroup
import tool.xfy9326.naucourse.beans.CreditCountItem
import tool.xfy9326.naucourse.databinding.ViewCreditCountItemBinding
import tool.xfy9326.naucourse.ui.views.recyclerview.adapters.base.ListRecyclerAdapter
import tool.xfy9326.naucourse.ui.views.recyclerview.viewholders.CreditCountViewHolder

class CreditCountAdapter(context: Context) : ListRecyclerAdapter<CreditCountViewHolder, CreditCountItem>(context) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        CreditCountViewHolder(ViewCreditCountItemBinding.inflate(layoutInflater, parent, false))

    override fun onBindViewHolder(holder: CreditCountViewHolder, position: Int, element: CreditCountItem) {
        holder.apply {
            cbCreditCountCourseName.text = element.courseName
            cbCreditCountCourseName.isChecked = element.isSelected
            etCreditCountCreditWeight.setText(element.creditWeight.toString())
            cbCreditCountCourseName.setOnCheckedChangeListener { _, isChecked ->
                element.isSelected = isChecked
            }
        }
    }

    fun getSelectedItems() =
        ArrayList<CreditCountItem>(itemCount).apply {
            currentList.forEach {
                if (it.isSelected) add(it)
            }
        }
}