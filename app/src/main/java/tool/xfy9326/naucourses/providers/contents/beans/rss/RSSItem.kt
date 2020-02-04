package tool.xfy9326.naucourses.providers.contents.beans.rss

import okhttp3.HttpUrl
import java.util.*

data class RSSItem(
    val title: String,
    val link: HttpUrl,
    val date: Date,
    val type: String?
)