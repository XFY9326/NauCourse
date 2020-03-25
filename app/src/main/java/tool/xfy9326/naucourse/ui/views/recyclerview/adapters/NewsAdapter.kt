package tool.xfy9326.naucourse.ui.views.recyclerview.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.providers.beans.GeneralNews
import tool.xfy9326.naucourse.ui.views.recyclerview.viewholders.NewsViewHolder
import tool.xfy9326.naucourse.utils.views.I18NUtils
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class NewsAdapter(context: Context, private var newsList: List<GeneralNews>, private val listener: OnNewsItemClickListener) :
    RecyclerView.Adapter<NewsViewHolder>() {
    constructor(context: Context, listener: OnNewsItemClickListener) : this(context, ArrayList(), listener)

    private val isOperationEnabledLock = Any()
    private val inflater = LayoutInflater.from(context)
    private val contextReference = WeakReference(context)

    companion object {
        private val DATE_FORMAT_YMD = SimpleDateFormat(Constants.Time.FORMAT_YMD, Locale.CHINA)
    }

    fun updateNewsList(newsList: List<GeneralNews>) = synchronized(isOperationEnabledLock) {
        this.newsList = newsList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = newsList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder =
        NewsViewHolder(inflater.inflate(R.layout.view_news_item, parent, false))

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        if (position < itemCount) {
            val news = newsList[position]
            contextReference.get()?.apply {
                holder.apply {
                    val newsSource = getString(I18NUtils.getNewsPostSourceResId(news.postSource)!!)
                    if (news.type != null) {
                        tvNewsType.text = getString(R.string.news_type, news.type)
                    } else {
                        tvNewsType.text = getString(R.string.news_type, newsSource + getString(R.string.news))
                    }
                    tvNewsTitle.text = news.title
                    tvNewsSource.text = newsSource
                    if (news.clickAmount == null) {
                        tvNewsClickAmount.visibility = View.GONE
                    } else {
                        tvNewsClickAmount.text = getString(R.string.news_click_amount, news.clickAmount)
                        tvNewsClickAmount.visibility = View.VISIBLE
                    }
                    tvNewsPostDate.text = DATE_FORMAT_YMD.format(news.postDate)
                    cvNewsCard.setOnClickListener {
                        synchronized(isOperationEnabledLock) {
                            listener.onNewsItemClick(newsList[position])
                        }
                    }
                }
            }
        }
    }

    interface OnNewsItemClickListener {
        fun onNewsItemClick(news: GeneralNews)
    }
}