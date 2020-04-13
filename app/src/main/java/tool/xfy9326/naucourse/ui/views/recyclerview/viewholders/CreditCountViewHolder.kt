package tool.xfy9326.naucourse.ui.views.recyclerview.viewholders

import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import kotlinx.android.synthetic.main.view_credit_count_item.view.*

class CreditCountViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val cbCreditCountCourseName: MaterialCheckBox = view.cb_creditCountCourseName
    val etCreditCountCreditWeight: AppCompatEditText = view.et_creditCountCreditWeight
}