package tool.xfy9326.naucourse.ui.views.recyclerview.adapters

import android.content.Context
import android.view.View
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.providers.beans.GeneralNews
import tool.xfy9326.naucourse.ui.views.recyclerview.adapters.base.ListRecyclerAdapter
import tool.xfy9326.naucourse.ui.views.recyclerview.viewholders.NewsViewHolder
import tool.xfy9326.naucourse.utils.views.I18NUtils
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

class NewsAdapter(context: Context, private val listener: OnNewsItemClickListener) : ListRecyclerAdapter<NewsViewHolder, GeneralNews>(context) {
    private val contextReference = WeakReference(context)

    companion object {
        private val DATE_FORMAT_YMD = SimpleDateFormat(Constants.Time.FORMAT_YMD, Locale.CHINA)
    }

    override fun onBindLayout(): Int = R.layout.view_news_item

    override fun onCreateViewHolder(view: View): NewsViewHolder = NewsViewHolder(view)

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int, element: GeneralNews) {
        contextReference.get()?.apply {
            holder.apply {
                val newsSource = getString(I18NUtils.getNewsPostSourceResId(element.postSource)!!)
                if (element.type != null) {
                    tvNewsType.text = getString(R.string.news_type, element.type)
                } else {
                    tvNewsType.text = getString(R.string.news_type, newsSource + getString(R.string.news))
                }
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
}