package tool.xfy9326.naucourse.providers.beans

import java.util.*

data class GeneralNewsDetail(
    val title: String,
    val postAdmin: String?,
    val postDate: Date,
    val clickAmount: Int,
    val htmlContent: String
)