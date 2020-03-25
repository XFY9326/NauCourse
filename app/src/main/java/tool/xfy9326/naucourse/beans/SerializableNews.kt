package tool.xfy9326.naucourse.beans

import android.content.Context
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.providers.beans.GeneralNews
import java.io.Serializable
import java.net.URL
import java.util.*

data class SerializableNews(
    val title: String,
    val postDate: Date,
    val detailUrl: URL,
    val type: String?,
    val postSource: GeneralNews.PostSource,
    val clickAmount: Int?
) : Serializable {
    companion object {
        fun parse(news: GeneralNews) =
            SerializableNews(
                news.title,
                news.postDate,
                news.detailUrl.toUrl(),
                news.type,
                news.postSource,
                news.clickAmount
            )
    }

    fun getShareText(context: Context) =
        if (type == null) {
            context.getString(R.string.news_share_text, title, detailUrl.toString())
        } else {
            context.getString(R.string.news_share_text_with_type, type, title, detailUrl.toString())
        }
}