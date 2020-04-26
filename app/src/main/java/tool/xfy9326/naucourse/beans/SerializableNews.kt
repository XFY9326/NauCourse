package tool.xfy9326.naucourse.beans

import tool.xfy9326.naucourse.providers.beans.GeneralNews
import java.io.Serializable
import java.net.URL
import java.util.*

// 序列化的通知信息，用于点击查看通知详情时的数据传递
data class SerializableNews(
    // 通知标题
    val title: String,
    // 发布时间
    val postDate: Date,
    // 详情地址
    val detailUrl: URL,
    // 通知类型
    val type: String?,
    // 通知来源
    val postSource: GeneralNews.PostSource,
    // 点击量
    val clickAmount: Int?
) : Serializable {
    companion object {
        // 将GeneralNews转为SerializableNews
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