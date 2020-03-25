package tool.xfy9326.naucourse.ui.views.recyclerview.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.synthetic.main.view_news_item.view.*

class NewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val cvNewsCard: MaterialCardView = view.cv_newsCard
    val tvNewsTitle: MaterialTextView = view.tv_newsTitle
    val tvNewsType: MaterialTextView = view.tv_newsType
    val tvNewsSource: MaterialTextView = view.tv_newsSource
    val tvNewsClickAmount: MaterialTextView = view.tv_newsClickAmount
    val tvNewsPostDate: MaterialTextView = view.tv_newsPostDate
}