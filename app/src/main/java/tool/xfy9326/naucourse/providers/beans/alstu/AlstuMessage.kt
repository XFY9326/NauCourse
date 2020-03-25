package tool.xfy9326.naucourse.providers.beans.alstu

import okhttp3.HttpUrl
import java.util.*

data class AlstuMessage(
    val title: String,
    val url: HttpUrl,
    val date: Date
)