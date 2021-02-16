package tool.xfy9326.naucourse.ui.views.recyclerview.viewholders

import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import tool.xfy9326.naucourse.databinding.ViewCreditCountItemBinding

class CreditCountViewHolder(binding: ViewCreditCountItemBinding) : RecyclerView.ViewHolder(binding.root) {
    val cbCreditCountCourseName: MaterialCheckBox = binding.cbCreditCountCourseName
    val etCreditCountCreditWeight: AppCompatEditText = binding.etCreditCountCreditWeight
}