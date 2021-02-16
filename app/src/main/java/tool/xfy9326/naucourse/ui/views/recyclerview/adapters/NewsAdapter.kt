package tool.xfy9326.naucourse.ui.views.recyclerview.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.constants.TimeConst
import tool.xfy9326.naucourse.databinding.ViewNewsItemBinding
import tool.xfy9326.naucourse.providers.beans.GeneralNews
import tool.xfy9326.naucourse.ui.views.recyclerview.adapters.base.ListRecyclerAdapter
import tool.xfy9326.naucourse.ui.views.recyclerview.viewholders.NewsViewHolder
import tool.xfy9326.naucourse.utils.views.I18NUtils
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

class NewsAdapter(context: Context, private val listener: OnNewsItemClickListener) :
    ListRecyclerAdapter<NewsViewHolder, GeneralNews>(context, DifferItemCallback()) {
    private val contextReference = WeakReference(context)

    companion object {
        private val DATE_FORMAT_YMD = SimpleDateFormat(TimeConst.FORMAT_YMD, Locale.CHINA)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        NewsViewHolder(ViewNewsItemBinding.inflate(layoutInflater, parent, false))

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int, element: GeneralNews) {
        contextReference.get()?.apply {
            holder.apply {
                val newsSource = getString(I18NUtils.getNewsPostSourceResId(element.postSource)!!)
                tvNewsType.text = getString(R.string.news_type, element.type ?: newsSource)
                tvNewsTitle.text = element.title
                tvNewsSource.text = newsSource
                if (element.clickAmount == null) {
                    tvNewsClickAmount.visibility = View.GONE
                } else {
                    tvNewsClickAmount.text = getString(R.string.news_click_amount, element.clickAmount)
                    tvNewsClickAmount.visibility = View.VISIBLE
                }
                tvNewsPostDate.text = DATE_FORMAT_YMD.format(element.postDate)
                cvNewsCard.setOnClickListener {
                    listener.onNewsItemClick(element)
                }
            }
        }
    }

    interface OnNewsItemClickListener {
        fun onNewsItemClick(news: GeneralNews)
    }

    private class DifferItemCallback : SimpleDifferItemCallBack<GeneralNews>() {
        override fun areContentsTheSame(oldItem: GeneralNews, newItem: GeneralNews): Boolean {
            return oldItem.title == newItem.title
        }
    }
}