package tool.xfy9326.naucourses.providers.info.methods

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.HttpUrl
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.io.dbHelpers.AppDBHelper
import tool.xfy9326.naucourses.providers.beans.GeneralNews
import tool.xfy9326.naucourses.providers.beans.GeneralNews.PostSource
import tool.xfy9326.naucourses.providers.contents.base.ContentErrorReason
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

    override fun onReadSimpleCache(data: List<GeneralNews>): List<GeneralNews> {
        return data.filterNot {
            it.postSource == PostSource.UNKNOWN
        }
    }

    override suspend fun getSimpleInfoContent(params: Set<PostSource>): ContentResult<List<GeneralNews>> {
        val resultList = HashSet<GeneralNews>(params.size * INIT_PER_TYPE_NEWS_SIZE)
        val resultDeferred = arrayOfNulls<Deferred<ContentResult<Set<GeneralNews>>>>(params.size)
        return coroutineScope {
            for ((i, param) in params.withIndex()) {
                resultDeferred[i] =
                    async {
                        when (param) {
                            PostSource.UNKNOWN -> throw IllegalArgumentException("Unknown Post Source")
                            PostSource.JWC -> TopicList.getContentData()
                            PostSource.ALSTU -> DefaultMessage.getContentData()
                            PostSource.RSS_JW -> JwRSS.getContentData()
                            PostSource.RSS_TW -> TwRSS.getContentData()
                            PostSource.RSS_XGC -> XgcRSS.getContentData()
                            PostSource.RSS_XXB -> XxbRSS.getContentData()
                        }
                    }
            }

            var contentErrorReason: ContentErrorReason? = null
            for (deferred in resultDeferred) {
                val result = deferred!!.await()
                if (result.isSuccess) {
                    resultList.addAll(result.contentData!!)
                } else {
                    contentErrorReason = result.contentErrorResult
                    break
                }
            }
            when {
                contentErrorReason != null -> ContentResult(false, contentErrorResult = contentErrorReason)
                resultList.size != 0 -> {
                    val dateStamp = isNewsOutOfDateTimeStamp()
                    val list = resultList.toList().filterNot {
                        it.postDate.time < dateStamp
                    }
                    ContentResult(true, contentData = sortNewsList(list))
                }
                else -> ContentResult(false, contentErrorResult = ContentErrorReason.EMPTY_DATA)
            }
        }
    }

    fun isNewsOutOfDateTimeStamp() = System.currentTimeMillis() - Constants.News.NEWS_STORE_DAY_LENGTH * 24 * 60 * 60 * 1000L

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

    override fun saveSimpleInfo(info: List<GeneralNews>) {
        AppDBHelper.clearGeneralNewsSet()
        AppDBHelper.putGeneralNewsSet(info)
    }

    override fun clearSimpleStoredInfo() = AppDBHelper.clearGeneralNewsSet()

    private fun sortNewsList(list: List<GeneralNews>): List<GeneralNews> =
        list.sortedWith(Comparator { o1, o2 ->
            val result1 = o2.postDate.time.compareTo(o1.postDate.time)
            if (result1 == 0) {
                o2.title.compareTo(o1.title)
            } else {
                result1
            }
        })
}