package tool.xfy9326.naucourse.ui.views.recyclerview.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import tool.xfy9326.naucourse.databinding.ViewNewsItemBinding

class NewsViewHolder(binding: ViewNewsItemBinding) : RecyclerView.ViewHolder(binding.root) {
    val cvNewsCard: MaterialCardView = binding.cvNewsCard
    val tvNewsTitle: MaterialTextView = binding.tvNewsTitle
    val tvNewsType: MaterialTextView = binding.tvNewsType
    val tvNewsSource: MaterialTextView = binding.tvNewsSource
    val tvNewsClickAmount: MaterialTextView = binding.tvNewsClickAmount
    val tvNewsPostDate: MaterialTextView = binding.tvNewsPostDate
}