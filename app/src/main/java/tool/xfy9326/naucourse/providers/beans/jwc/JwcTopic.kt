package tool.xfy9326.naucourse.providers.beans.jwc

import okhttp3.HttpUrl
import java.util.*

data class JwcTopic(
    val title: String,
    val postDate: Date,
    val detailUrl: HttpUrl,
    val type: String,
    val clickAmount: Int
)