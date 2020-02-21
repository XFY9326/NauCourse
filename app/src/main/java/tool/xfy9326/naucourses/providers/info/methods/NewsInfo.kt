package tool.xfy9326.naucourses.providers.info.methods

import okhttp3.HttpUrl
import tool.xfy9326.naucourses.io.dbHelpers.AppDBHelper
import tool.xfy9326.naucourses.providers.beans.GeneralNews
import tool.xfy9326.naucourses.providers.beans.GeneralNews.PostSource
import tool.xfy9326.naucourses.providers.contents.base.ContentResult
import tool.xfy9326.naucourses.providers.contents.methods.alstu.DefaultMessage
import tool.xfy9326.naucourses.providers.contents.methods.jwc.TopicList
import tool.xfy9326.naucourses.providers.contents.methods.rss.JwRSS
import tool.xfy9326.naucourses.providers.contents.methods.rss.TwRSS
import tool.xfy9326.naucourses.providers.contents.methods.rss.XgcRSS
import tool.xfy9326.naucourses.providers.contents.methods.rss.XxbRSS
import tool.xfy9326.naucourses.providers.info.base.BaseSimpleContentInfo

object NewsInfo : BaseSimpleContentInfo<List<GeneralNews>, PostSource>() {
    private const val INIT_PER_TYPE_NEWS_SIZE = 10

    override fun loadSimpleStoredInfo(): List<GeneralNews>? =
        sortNewsList(AppDBHelper.getGeneralNewsArray())

    override fun getSimpleInfoContent(params: Set<PostSource>): ContentResult<List<GeneralNews>> {
        val resultList = HashSet<GeneralNews>(params.size * INIT_PER_TYPE_NEWS_SIZE)
        for (param in params) {
            val result = when (param) {
                PostSource.UNKNOWN -> throw IllegalArgumentException("Unknown Post Source")
                PostSource.JWC -> TopicList.getContentData()
                PostSource.ALSTU -> DefaultMessage.getContentData()
                PostSource.RSS_JW -> JwRSS.getContentData()
                PostSource.RSS_TW -> TwRSS.getContentData()
                PostSource.RSS_XGC -> XgcRSS.getContentData()
                PostSource.RSS_XXB -> XxbRSS.getContentData()
            }
            if (result.isSuccess) {
                resultList.addAll(result.contentData!!)
            } else {
                return ContentResult(false, contentErrorResult = result.contentErrorResult)
            }
        }
        return ContentResult(
            true, contentData = sortNewsList(
                resultList.toList()
            )
        )
    }

    fun getDetailNewsInfo(url: HttpUrl, newsType: PostSource) =
        when (newsType) {
            PostSource.UNKNOWN -> throw IllegalArgumentException("Unknown Post Source")
            PostSource.JWC -> TopicList.getContentDetailData(url)
            PostSource.ALSTU -> DefaultMessage.getContentDetailData(url)
            PostSource.RSS_JW -> JwRSS.getContentDetailData(url)
            PostSource.RSS_TW -> TwRSS.getContentDetailData(url)
            PostSource.RSS_XGC -> XgcRSS.getContentDetailData(url)
            PostSource.RSS_XXB -> XxbRSS.getContentDetailData(url)
        }

    override fun saveSimpleInfo(info: List<GeneralNews>) = AppDBHelper.putGeneralNewsSet(info)

    override fun clearSimpleStoredInfo() = AppDBHelper.clearGeneralNewsSet()

    private fun sortNewsList(list: List<GeneralNews>): List<GeneralNews> =
        list.sortedByDescending {
            it.postDate.time
        }
}