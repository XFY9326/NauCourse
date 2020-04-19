package tool.xfy9326.naucourse.update.beans

import java.io.Serializable

data class DownloadSource(
    val sourceName: String,
    val url: String,
    val isDirectLink: Boolean
) : Serializable