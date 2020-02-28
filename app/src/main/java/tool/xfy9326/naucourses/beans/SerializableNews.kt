package tool.xfy9326.naucourses.beans

import tool.xfy9326.naucourses.providers.beans.GeneralNews
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
}